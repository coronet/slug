package io.coronet.slug.json;

import io.coronet.bytes.Bytes;
import io.coronet.slug.Slug;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface TestSlug extends Slug<TestSlug> {

    Boolean getBoolean();
    TestSlug setBoolean(Boolean value);

    String getString();
    TestSlug setString(String value);

    Integer getNumber();
    TestSlug setNumber(Integer value);

    Bytes getBinary();
    TestSlug setBinary(Bytes value);

    List<String> getList();
    TestSlug setList(List<String> value);

    Map<String, Integer> getMap();
    TestSlug setMap(Map<String, Integer> value);

    TestSlug getSlug();
    TestSlug setSlug(TestSlug value);

    List<TestSlug> getSlugList();
    TestSlug setSlugList(List<TestSlug> value);

    Map<String, TestSlug> getSlugMap();
    TestSlug setSlugMap(Map<String, TestSlug> value);
}
