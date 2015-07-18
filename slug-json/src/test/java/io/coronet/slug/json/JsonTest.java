package io.coronet.slug.json;

import io.coronet.bytes.Bytes;
import io.coronet.slug.SlugModule;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class JsonTest {

    private static final SlugModule module = JsonSlugModule.builder().build();

    @Test
    public void testSerializeBoolean() {
        Bytes b = module.serialize(true);
        Assert.assertEquals("true", b.toString());
    }

    @Test
    public void testSerializeString() {
        Bytes b = module.serialize("Hello World");
        Assert.assertEquals("\"Hello World\"", b.toString());
    }

    @Test
    public void testSerializeByte() {
        Bytes b = module.serialize((byte) 123);
        Assert.assertEquals("123", b.toString());
    }

    @Test
    public void testSerializeShort() {
        Bytes b = module.serialize((short) 123);
        Assert.assertEquals("123", b.toString());
    }

    @Test
    public void testSerializeInteger() {
        Bytes b = module.serialize(123);
        Assert.assertEquals("123", b.toString());
    }

    @Test
    public void testSerializeLong() {
        Bytes b = module.serialize((long) 123);
        Assert.assertEquals("123", b.toString());

    }

    @Test
    public void testSerializeBigInteger() {
        Bytes b = module.serialize(BigInteger.valueOf(123));
        Assert.assertEquals("123", b.toString());
    }

    @Test
    public void testSerializeFloat() {
        Bytes b = module.serialize((float) 123);
        Assert.assertEquals("123.0", b.toString());
    }

    @Test
    public void testSerializeDouble() {
        Bytes b = module.serialize((double) 123);
        Assert.assertEquals("123.0", b.toString());
    }

    @Test
    public void testSerializeBigDecimal() {
        Bytes b = module.serialize(BigDecimal.valueOf(123.0));
        Assert.assertEquals("123.0", b.toString());
    }

    @Test
    public void testSerializeBinary() {
        Bytes b = module.serialize(Bytes.from("Hello World"));
        Assert.assertEquals("\"SGVsbG8gV29ybGQ=\"", b.toString());
    }

    @Test
    public void testSerializeList() {
        Bytes b = module.serialize(Arrays.asList(
                true,
                "Hello World",
                123));

        Assert.assertEquals(
                "[true,\"Hello World\",123]",
                b.toString());
    }

    @Test
    public void testSerializeMap() {
        @SuppressWarnings("serial")
        Bytes b = module.serialize(new LinkedHashMap<String, Object>() {{
            put("boolean", true);
            put("string", "Hello World");
            put("number", 123);
        }});

        Assert.assertEquals(
                "{\"boolean\":true,\"string\":\"Hello World\",\"number\":123}",
                b.toString());
    }

    @Test
    public void testSerializeSlug() throws IOException {
        TestSlug child = module.getSlugBox().create(TestSlug.class)
                .setBoolean(true)
                .setString("Hello World")
                .setNumber(123)
                .setBinary(Bytes.from("abc"));

        TestSlug slug = module.getSlugBox().create(TestSlug.class)
                .setList(Arrays.asList("a", "b", "c"))
                .setMap(Collections.singletonMap("a", 1))
                .setSlug(child)
                .setSlugList(Arrays.asList(child, child))
                .setSlugMap(Collections.singletonMap("child", child));

        Bytes b = module.serialize(slug);
        System.out.println(b);
    }
}
