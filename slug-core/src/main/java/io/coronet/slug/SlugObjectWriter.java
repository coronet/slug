package io.coronet.slug;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
final class SlugObjectWriter<T> implements SlugWriter<T> {

    private final Deque<Context> stack = new ArrayDeque<>();
    private final SlugBox slugBox;

    private String name;
    private Object result;

    public SlugObjectWriter(SlugBox slugBox) {
        if (slugBox == null) {
            throw new NullPointerException("slugBox");
        }
        this.slugBox = slugBox;
    }

    @Override
    public SlugWriter<T> writeName(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (result != null) {
            throw new IllegalStateException("already finished!");
        }
        if (this.name != null) {
            throw new IllegalStateException("name already set!");
        }
        if (!isInNamingContext()) {
            throw new IllegalStateException(
                    "must be in a map or slug to write names");
        }

        this.name = name;
        return this;
    }

    @Override
    public SlugWriter<T> writeValue(Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        if (result != null) {
            throw new IllegalStateException("already finished!");
        }

        writeValue(getName(), value);

        return this;
    }

    @Override
    public SlugWriter<T> writeStartList() {
        String listName = getName();
        stack.push(new ListContext(listName));
        return this;
    }

    @Override
    public SlugWriter<T> writeEndList() {
        Context top = stack.peek();
        if (!(top instanceof ListContext)) {
            throw new IllegalStateException("not writing a list");
        }

        stack.pop();
        writeValue(top.getName(), top.getValue());
        return this;
    }

    @Override
    public SlugWriter<T> writeStartMap() {
        String mapName = getName();
        stack.push(new MapContext(mapName));
        return this;
    }

    @Override
    public SlugWriter<T> writeEndMap() {
        Context top = stack.peek();
        if (!(top instanceof MapContext)) {
            throw new IllegalStateException("not writing a map");
        }

        stack.pop();
        writeValue(top.getName(), top.getValue());
        return this;
    }

    @Override
    public <S extends Slug<S>> SlugWriter<T> writeStartSlug(Class<S> type) {
        String slugName = getName();
        Slug<?> slug = slugBox.create(type);
        stack.push(new SlugContext(slugName, slug));
        return this;
    }

    @Override
    public SlugWriter<T> writeEndSlug() {
        Context top = stack.peek();
        if (!(top instanceof SlugContext)) {
            throw new IllegalStateException("not writing a slug");
        }

        stack.pop();
        writeValue(top.getName(), top.getValue());
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T finish() {
        return (T) result;
    }

    private boolean isInNamingContext() {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.peek().isList()) {
            return false;
        }
        return true;
    }

    private String getName() {
        String localName = name;

        if (localName == null) {
            if (isInNamingContext()) {
                throw new IllegalStateException(
                        "name required in map and slug");
            }
        } else {
            name = null;
        }

        return localName;
    }

    private void writeValue(String name, Object value) {
        Context top = stack.peek();
        if (top == null) {
            result = value;
        } else {
            top.write(name, value);
        }
    }

    private static abstract class Context {

        private final String name;

        protected Context(String name) {
            this.name = name;
        }

        public boolean isList() {
            return false;
        }

        public final String getName() {
            return name;
        }

        public abstract Object getValue();

        abstract void write(String name, Object value);
    }

    private static final class ListContext extends Context {

        private final List<Object> list;

        public ListContext(String name) {
            super(name);
            this.list = new ArrayList<>();
        }

        @Override
        public boolean isList() {
            return true;
        }

        @Override
        public Object getValue() {
            return list;
        }

        @Override
        public void write(String name, Object value) {
            assert (name == null);
            list.add(value);
        }
    }

    private static final class MapContext extends Context {

        private final Map<String, Object> map;

        public MapContext(String name) {
            super(name);
            this.map = new HashMap<>();
        }

        @Override
        public Object getValue() {
            return map;
        }

        @Override
        public void write(String name, Object value) {
            if (name == null) {
                throw new IllegalStateException(
                        "name must be set before writing into a map");
            }
            map.put(name, value);
        }
    }

    private static final class SlugContext extends Context {

        private Slug<?> slug;

        public SlugContext(String name, Slug<?> slug) {
            super(name);
            this.slug = slug;
        }

        @Override
        public Object getValue() {
            return slug;
        }

        @Override
        public void write(String name, Object value) {
            if (name == null) {
                throw new IllegalStateException(
                        "name must be set before writing into a slug");
            }
            slug = slug.with(name, value);
        }
    }
}
