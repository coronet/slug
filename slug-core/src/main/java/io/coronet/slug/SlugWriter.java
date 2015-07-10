package io.coronet.slug;

/**
 *
 */
public interface SlugWriter<T> {

    SlugWriter<T> writeName(String name);

    SlugWriter<T> writeValue(Object value);

    SlugWriter<T> writeStartList();

    SlugWriter<T> writeEndList();

    SlugWriter<T> writeStartMap();

    SlugWriter<T> writeEndMap();

    <S extends Slug<S>> SlugWriter<T> writeStartSlug(Class<S> type);

    SlugWriter<T> writeEndSlug();

    T finish();
}
