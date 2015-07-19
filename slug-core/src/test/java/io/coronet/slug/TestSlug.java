package io.coronet.slug;

import io.coronet.bytes.Bytes;


/**
 *
 */
public interface TestSlug extends Slug<TestSlug> {
    String getFoo();
    TestSlug setFoo(String value);

    Integer getBar();
    TestSlug setBar(Integer value);

    Bytes getBinary();
    void setBinary(Bytes value);
}
