package io.coronet.slug;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class SlugObjectWriterTest {

    private final SlugBox box = new SlugBox();
    private final SlugWriter<?> writer = box.writer();

    @Test
    public void testString() {
        Object result = writer.writeValue("Hello World").finish();
        Assert.assertEquals("Hello World", result);
    }

    @Test
    public void testNumber() {
        Object result = writer.writeValue(123).finish();
        Assert.assertEquals(Integer.valueOf(123), result);
    }

    @Test
    public void testBoolean() {
        Object result = writer.writeValue(true).finish();
        Assert.assertEquals(true, result);
    }

    @Test
    public void testList() {
        Object result = writer.writeStartList()
            .writeValue("Hello World")
            .writeValue(123)
            .writeValue(true)
            .writeEndList()
            .finish();

        Assert.assertEquals(Arrays.asList("Hello World", 123, true), result);
    }

    @Test
    public void testMap() {
        Object result = writer.writeStartMap()
                .writeName("String")
                .writeValue("Hello World")
                .writeName("Number")
                .writeValue(123)
                .writeName("Boolean")
                .writeValue(true)
                .writeEndMap()
                .finish();

        @SuppressWarnings("serial")
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("String", "Hello World");
            put("Number", 123);
            put("Boolean", true);
        }};

        Assert.assertEquals(map, result);
    }

    @Test
    public void testSlug() {
        Object result = writer.writeStartSlug(TestSlug.class)
                .writeName("Foo")
                .writeValue("Hello World")
                .writeName("Bar")
                .writeValue(123)
                .writeName("Baz")
                .writeValue(true)
                .writeEndSlug()
                .finish();

        Assert.assertTrue(result instanceof TestSlug);
        TestSlug slug = (TestSlug) result;

        Assert.assertEquals("Hello World", slug.getFoo());
        Assert.assertEquals(Integer.valueOf(123), slug.getBar());
        Assert.assertEquals(true, slug.get("Baz"));
    }
}
