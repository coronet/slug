package io.coronet.slug;


/**
 *
 */
public interface TestSlug extends Slug<TestSlug> {
    String getFoo();
    TestSlug setFoo(String value);

    Integer getBar();
    TestSlug setBar(Integer value);
}
