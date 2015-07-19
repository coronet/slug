package io.coronet.slug;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * A {@code SlugBox} creates implementations of slug interfaces at runtime.
 * It serves as a cache of the generated types, so you should generally create
 * and share one per process to avoid repeated reflection/class generation.
 */
public final class SlugBox {

    private static final String ABSTRACT_SLUG =
            Type.getInternalName(AbstractSlug.class);

    private static final String SLUG_FACTORY =
            Type.getInternalName(SlugFactory.class);

    private static final String OBJECT =
            Type.getInternalName(Object.class);


    private final SlugCache cache = new SlugCache();

    /**
     * Creates a new SlugBox with a default configuration.
     *
     * TODO: Is there a non-default configuration?
     */
    public SlugBox() {
    }


    /**
     * Creates a new, mutable slug with no members set.
     *
     * @param type the interface type of the slug to create
     * @return a new implementation of the given slug interface
     */
    public <T extends Slug<?>> T create(Class<T> type) {
        SlugFactory<T> factory = factoryFor(type);
        return factory.create();
    }

    /**
     * Creates a new, mutable slug wrapping a copy of the given map as its
     * initial state.
     *
     * @param type the interface type of the slug to create
     * @param map the map of initial member values for the slug
     * @return a new implementation of the given slug interface
     */
    public <T extends Slug<?>> T create(Class<T> type, Map<String, ?> map) {
        return wrap(type, new HashMap<>(map));
    }

    /**
     * Creates a new, mutable slug directly wrapping the given map.
     *
     * @param type the interface type of the slug to create
     * @param map the map of values to wrap
     * @return a new implementation of the given slug interface
     */
    public <T extends Slug<?>> T wrap(Class<T> type, Map<String, Object> map) {
        SlugFactory<T> factory = factoryFor(type);
        return factory.create(map);
    }

    /**
     * Creates a mutable copy of the given slug.
     *
     * @param slug the slug to copy
     * @return a new, independent copy of the slug
     */
    public <T extends Slug<T>> T copy(T slug) {
        return copy(slug.type(), slug);
    }

    /**
     * Creates a mutable copy of the given slug with a possibly-different
     * interface type.
     *
     * @param type the new interface type to cast the copy to
     * @param slug the slug to copy
     * @return a new, independent copy of the slug implementing the given type
     */
    public <T extends Slug<T>> T copy(Class<T> type, Slug<?> slug) {
        return create(type, slug.asMap());
    }

    /**
     * Creates a view of the given slug with a possibly-different interface
     * type.
     *
     * @param type the new interface type to cast the slug to
     * @param slug the slug to cast
     * @return a view of the slug implementing the given type
     */
    public <T extends Slug<T>> T cast(Class<T> type, Slug<?> slug) {
        return wrap(type, slug.asMap());
    }

    /**
     * Retrieves or creates a {@code SlugFactory} for the given slug type.
     *
     * @param type the type of slug
     * @return a factory for instances of the given slug type
     */
    public <T extends Slug<?>> SlugFactory<T> factoryFor(Class<T> type) {
        return getOrCreateEntry(type).factory;
    }

    /**
     * Gets an immutable map containing the "known" members of the given slug
     * type (as defined by reflecting on the setter methods that the interface
     * exposes when initially creating the implementation).
     *
     * @param type the slug type to introspect about
     * @return a map from member name to expected member type
     */
    public <T extends Slug<?>> Map<String, java.lang.reflect.Type> getMembers(
            Class<T> type) {

        return getOrCreateEntry(type).members;
    }

    private <T extends Slug<?>> CacheEntry<T> getOrCreateEntry(Class<T> type) {
        if (type == null) {
            throw new NullPointerException("type");
        }

        CacheEntry<T> entry = cache.get(type);
        if (entry != null) {
            return entry;
        }

        synchronized (type) {
            entry = cache.get(type);
            if (entry == null) {
                entry = createEntry(type);
                cache.put(type, entry);
            }
            return entry;
        }
    }

    private <T extends Slug<?>> CacheEntry<T> createEntry(Class<T> type) {
        if (!type.isInterface()) {
            throw new IllegalArgumentException(type + " is not an interface");
        }
        if ((type.getModifiers() & Modifier.PUBLIC) == 0) {
            throw new IllegalArgumentException(type + " is not public");
        }

        DirectLoader loader = new DirectLoader(type.getClassLoader());

        Map<String, java.lang.reflect.Type> members = new HashMap<>();

        Class<?> implType = createSlugImpl(loader, type, members);
        Class<?> factoryType = createFactory(loader, implType);

        members = Collections.unmodifiableMap(members);

        try {

            @SuppressWarnings("unchecked")
            SlugFactory<T> factory = (SlugFactory<T>) factoryType.newInstance();
            return new CacheEntry<T>(factory, members);

        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Error creating factory", e);
        }
    }

    private Class<?> createSlugImpl(
            DirectLoader loader,
            Class<?> iface,
            Map<String, java.lang.reflect.Type> members) {

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        String ifaceName = Type.getInternalName(iface);
        String implName = ifaceName + "$$Impl";

        // public static final class $Impl extends AbstractSlug<${Iface}>
        //         implements ${Iface} {
        writer.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER,
                implName,
                null,
                ABSTRACT_SLUG,
                new String[] { ifaceName });

        writeConstructor(writer, ifaceName);
        writeMethods(writer, iface, members);

        // }
        writer.visitEnd();

        return loader.loadClass(writer.toByteArray());
    }

    private void writeConstructor(
            ClassWriter writer,
            String ifaceName) {

        {
            // private ${IfaceType}$$Impl(Map<String, Object> map) {
            MethodVisitor visitor = writer.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "<init>",
                    "(Ljava/util/Map;)V",
                    null, // TODO: generics stuff?
                    null);

            visitor.visitCode();

            // super(map, ${IfaceType}.class);
            visitor.visitVarInsn(Opcodes.ALOAD, 0);
            visitor.visitLdcInsn(Type.getObjectType(ifaceName));
            visitor.visitVarInsn(Opcodes.ALOAD, 1);
            visitor.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    ABSTRACT_SLUG,
                    "<init>",
                    "(Ljava/lang/Class;Ljava/util/Map;)V",
                    false);

            // }
            visitor.visitInsn(Opcodes.RETURN);
            visitor.visitMaxs(3, 0);
            visitor.visitEnd();
        }
    }

    private void writeMethods(
            ClassWriter writer,
            Class<?> iface,
            Map<String, java.lang.reflect.Type> members) {

        for (Method method : iface.getMethods()) {
            if (method.getDeclaringClass() == Slug.class) {
                continue;
            }
            if (method.isDefault()) {
                continue;
            }
            // TODO: do we need to explicitly skip synthetic/bridge methods?

            String name = method.getName();

            if (name.startsWith("get")) {
                writeGetter(writer, method, name.substring(3));
            } else if (name.startsWith("set")) {
                writeSetter(writer, method, name.substring(3), iface, members);
            } else {
                throw new IllegalStateException(
                        "Unimplementable method: " + method);
            }
        }
    }

    private void writeGetter(
            ClassWriter writer,
            Method method,
            String name) {

        if (method.getParameterCount() != 0) {
            throw new IllegalStateException(
                    "Cannot implement method with parameters: " + method);
        }

        if (method.getReturnType() == void.class) {
            throw new IllegalStateException(
                    "Cannot implement void method: " + method);
        }

        // ${ReturnType} get${name}() {
        MethodVisitor visitor = writer.visitMethod(
                Opcodes.ACC_PUBLIC,
                method.getName(),
                Type.getMethodDescriptor(method),
                null, // TODO: Generics stuff?
                null);

        visitor.visitCode();

        // temp0 = super.get("${name}");
        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitLdcInsn(name);
        visitor.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                ABSTRACT_SLUG,
                "get",
                "(Ljava/lang/String;)Ljava/lang/Object;",
                false);

        // temp1 = (${ReturnType}) temp0;
        visitor.visitTypeInsn(
                Opcodes.CHECKCAST,
                Type.getReturnType(method).getInternalName());

        // return temp1;
        visitor.visitInsn(Opcodes.ARETURN);

        // }
        visitor.visitMaxs(2, 0);
        visitor.visitEnd();
    }

    private void writeSetter(
            ClassWriter writer,
            Method method,
            String name,
            Class<?> iface,
            Map<String, java.lang.reflect.Type> members) {

        if (method.getParameterCount() != 1) {
            throw new IllegalStateException(
                    "Setter must have exactly one parameter: " + method);
        }

        if (method.getReturnType() != void.class
                && method.getReturnType() != iface) {

            throw new IllegalStateException(
                    "Setter must return void or the interface type: "
                    + method);
        }

        members.put(name, method.getGenericParameterTypes()[0]);

        // ${Iface} set${name}(${ParamType} value) {
        MethodVisitor visitor = writer.visitMethod(
                Opcodes.ACC_PUBLIC,
                method.getName(),
                Type.getMethodDescriptor(method),
                null, // TODO: Generics stuff?
                null);

        visitor.visitCode();

        // temp0 = super.set("${name}", value);
        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitLdcInsn(name);
        visitor.visitVarInsn(Opcodes.ALOAD, 1);
        visitor.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                ABSTRACT_SLUG,
                "set",
                "(Ljava/lang/String;Ljava/lang/Object;)Lio/coronet/slug/Slug;",
                false);

        if (method.getReturnType() == iface) {
            // temp1 = (${IFace}) temp0;
            visitor.visitTypeInsn(
                    Opcodes.CHECKCAST,
                    Type.getReturnType(method).getInternalName());

            // return temp1;
            visitor.visitInsn(Opcodes.ARETURN);
        } else {
            // return;
            visitor.visitInsn(Opcodes.RETURN);
        }

        // }
        visitor.visitMaxs(3, 0);
        visitor.visitEnd();
    }

    private Class<?> createFactory(DirectLoader loader, Class<?> impl) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        String implName = Type.getInternalName(impl);
        String factoryName = implName + "$$Factory";

        // public static final class $Factory implements SlugFactory<${Iface}> {
        writer.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER,
                factoryName,
                null,
                OBJECT,
                new String[] { SLUG_FACTORY });

        {
            // public $Factory() {
            MethodVisitor visitor = writer.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "<init>",
                    "()V",
                    null,
                    null);

            visitor.visitCode();

            // super();
            visitor.visitIntInsn(Opcodes.ALOAD, 0);
            visitor.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    OBJECT,
                    "<init>",
                    "()V",
                    false);

            // }
            visitor.visitInsn(Opcodes.RETURN);
            visitor.visitMaxs(1, 0);
            visitor.visitEnd();
        }

        {
            // public Slug create(Map<String, Object> map) {
            MethodVisitor visitor = writer.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "create",
                    "(Ljava/util/Map;)Lio/coronet/slug/Slug;",
                    null, // TODO: generics stuff?
                    null);

            visitor.visitCode();

            // temp0 = new ${Impl}(map);
            visitor.visitTypeInsn(Opcodes.NEW, implName);
            visitor.visitInsn(Opcodes.DUP);
            visitor.visitIntInsn(Opcodes.ALOAD, 1);
            visitor.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    implName,
                    "<init>",
                    "(Ljava/util/Map;)V",
                    false);

            // return temp0;
            // }
            visitor.visitInsn(Opcodes.ARETURN);
            visitor.visitMaxs(3, 0);
            visitor.visitEnd();
        }

        // }
        writer.visitEnd();

        return loader.loadClass(writer.toByteArray());
    }

    private static final class DirectLoader extends ClassLoader {

        public DirectLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> loadClass(byte[] data) {
            return defineClass(null, data, 0, data.length, null);
        }
    }

    private static final class CacheEntry<T extends Slug<?>> {

        public final SlugFactory<T> factory;
        public final Map<String, java.lang.reflect.Type> members;

        public CacheEntry(
                SlugFactory<T> factory,
                Map<String, java.lang.reflect.Type> members) {

            this.factory = factory;
            this.members = members;
        }
    }

    private static final class SlugCache {

        private final Map<Class<?>, CacheEntry<?>> cache;

        public SlugCache() {
            cache = new ConcurrentHashMap<>();
        }

        @SuppressWarnings("unchecked")
        public <T extends Slug<?>> CacheEntry<T> get(Class<T> key) {
            return (CacheEntry<T>) cache.get(key);
        }

        public <T extends Slug<?>> void put(
                Class<T> key,
                CacheEntry<T> value) {

            cache.put(key, value);
        }
    }
}
