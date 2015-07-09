package io.coronet.slug;

import java.util.Map;

/**
 *
 */
public interface Example extends Slug<Example> {

    String getFoo();

    Example withFoo(String value);

    static final class Impl extends AbstractSlug<Example> implements Example {

        public Impl(Map<String, Object> map) {
            super(Example.class, map);
        }

        @Override
        public String getFoo() {
            return (String) super.get("Foo");
        }

        @Override
        public Example withFoo(String value) {
            return super.with("Foo", value);
        }

        public static final class Factory implements SlugFactory<Example> {

            @Override
            public Example create(Map<String, Object> map) {
                return new Impl(map);
            }
        }
    }
}
