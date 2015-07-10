package io.coronet.slug;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 *
 */
public class SlugBox {

    private static final String ABSTRACT_SLUG =
            Type.getInternalName(AbstractSlug.class);

    private static final String SLUG_FACTORY =
            Type.getInternalName(SlugFactory.class);

    private static final String OBJECT =
            Type.getInternalName(Object.class);


    private final FactoryCache cache = new FactoryCache();

    public SlugBox() {
    }

    public SlugWriter<?> writer() {
        return new SlugObjectWriter<Object>(this);
    }

    public <T> SlugWriter<T> writerFor(Class<T> type) {
        return new SlugObjectWriter<T>(this);
    }

    public <T extends Slug<T>> T create(Class<T> type) {
        SlugFactory<T> factory = factoryFor(type);
        return factory.create();
    }

    public <T extends Slug<T>> T create(Class<T> type, Map<String, ?> map) {
        return wrap(type, new HashMap<>(map));
    }

    public <T extends Slug<T>> T wrap(Class<T> type, Map<String, Object> map) {
        SlugFactory<T> factory = factoryFor(type);
        return factory.create(map);
    }

    public <T extends Slug<T>> T copy(T slug) {
        return copy(slug.type(), slug);
    }

    public <T extends Slug<T>> T copy(Class<T> type, Slug<?> slug) {
        return create(type, slug.asMap());
    }

    public <T extends Slug<T>> T cast(Class<T> type, Slug<?> slug) {
        return wrap(type, slug.asMap());
    }

    public <T extends Slug<T>> SlugFactory<T> factoryFor(Class<T> type) {
        if (type == null) {
            throw new NullPointerException("type");
        }

        SlugFactory<T> factory = cache.get(type);
        if (factory != null) {
            return factory;
        }

        synchronized (type) {
            factory = cache.get(type);
            if (factory == null) {
                factory = createFactoryFor(type);
                cache.put(type, factory);
            }
            return factory;
        }
    }

    private <T extends Slug<T>> SlugFactory<T> createFactoryFor(
            Class<T> type) {

        if (!type.isInterface()) {
            throw new IllegalArgumentException(type + " is not an interface");
        }
        if ((type.getModifiers() & Modifier.PUBLIC) == 0) {
            throw new IllegalArgumentException(type + " is not public");
        }

        DirectLoader loader = new DirectLoader(type.getClassLoader());

        Class<?> impl = createSlugImpl(loader, type);
        Class<?> factory = createFactory(loader, impl);

        try {

            @SuppressWarnings("unchecked")
            SlugFactory<T> result = (SlugFactory<T>) factory.newInstance();
            return result;

        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Error creating factory", e);
        }
    }

    private Class<?> createSlugImpl(DirectLoader loader, Class<?> iface) {
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
        writeMethods(writer, iface);

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

    private void writeMethods(ClassWriter writer, Class<?> iface) {
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
            } else if (name.startsWith("is")) {
                writeGetter(writer, method, name.substring(2));
            } else if (name.startsWith("with")) {
                writeWither(writer, method, name.substring(4), iface);
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

    private void writeWither(
            ClassWriter writer,
            Method method,
            String name,
            Class<?> iface) {

        if (method.getParameterCount() != 1) {
            throw new IllegalStateException(
                    "Wither must have exactly one parameter: " + method);
        }

        if (method.getReturnType() != iface) {
            throw new IllegalStateException(
                    "Wither must return the interface type: " + method);
        }

        // ${Iface} with${name}(${ParamType} value) {
        MethodVisitor visitor = writer.visitMethod(
                Opcodes.ACC_PUBLIC,
                method.getName(),
                Type.getMethodDescriptor(method),
                null, // TODO: Generics stuff?
                null);

        visitor.visitCode();

        // temp0 = super.with("${name}", value);
        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitLdcInsn(name);
        visitor.visitVarInsn(Opcodes.ALOAD, 1);
        visitor.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                ABSTRACT_SLUG,
                "with",
                "(Ljava/lang/String;Ljava/lang/Object;)Lio/coronet/slug/Slug;",
                false);

        // temp1 = (${IFace}) temp0;
        visitor.visitTypeInsn(
                Opcodes.CHECKCAST,
                Type.getReturnType(method).getInternalName());

        // return temp1;
        visitor.visitInsn(Opcodes.ARETURN);

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

    private static final class FactoryCache {

        private final Map<Class<?>, SlugFactory<?>> cache;

        public FactoryCache() {
            cache = new ConcurrentHashMap<>();
        }

        @SuppressWarnings("unchecked")
        public <T extends Slug<T>> SlugFactory<T> get(Class<T> key) {
            return (SlugFactory<T>) cache.get(key);
        }

        public <T extends Slug<T>> void put(
                Class<T> key,
                SlugFactory<T> value) {

            cache.put(key, value);
        }
    }
}
