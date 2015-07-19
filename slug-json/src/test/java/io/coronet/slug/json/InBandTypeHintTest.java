package io.coronet.slug.json;

import io.coronet.bytes.Bytes;
import io.coronet.slug.SlugModule;
import io.coronet.slug.SlugTypeRegistry;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class InBandTypeHintTest {

    private static final SlugModule module = JsonSlugModule.builder()
            .withTypeRegistry(SlugTypeRegistry.builder()
                    .with("test@1.0", TestSlug.class)
                    .build())
            .build();

    @Test
    public void testInBandHint() throws IOException {
        Bytes b = Bytes.from(
                "{"
                + "\"__type\": \"test@1.0\","
                + "\"Boolean\": true,"
                + "\"String\": \"Hello World\","
                + "\"Number\": 123"
                + "}");

        Object o = module.deserialize(b);

        Assert.assertTrue(o instanceof TestSlug);
        TestSlug slug = (TestSlug) o;

        Assert.assertEquals(true, slug.getBoolean());
        Assert.assertEquals("Hello World", slug.getString());
        Assert.assertEquals(123, (int) slug.getNumber());
        Assert.assertEquals("test@1.0", slug.get("__type"));
    }

    @Test
    public void testBogusInBandHint() throws Exception {
        Bytes b = Bytes.from(
                "{"
                + "\"__type\": \"test@1.1\","
                + "\"Boolean\": true,"
                + "\"String\": \"Hello World\","
                + "\"Number\": 123"
                + "}");

        Object o = module.deserializeTo(b, TestSlug.class);

        Assert.assertTrue(o instanceof TestSlug);
        TestSlug slug = (TestSlug) o;

        Assert.assertEquals(true, slug.getBoolean());
        Assert.assertEquals("Hello World", slug.getString());
        Assert.assertEquals(123, (int) slug.getNumber());
        Assert.assertEquals("test@1.1", slug.get("__type"));
    }
}
