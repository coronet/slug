package io.coronet.slug;


/**
 *
 */
public interface TestSlug extends Slug<TestSlug> {
    String getFoo();
    TestSlug withFoo(String value);

    Integer getBar();
    TestSlug withBar(Integer value);
}
