package io.coronet.slug;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class SlugTest {

    @Test
    public void testSlug() {
        SlugBox slugs = new SlugBox();
        TestSlug slug = slugs.create(TestSlug.class);

        Assert.assertSame(TestSlug.class, slug.type());
        Assert.assertFalse(slug.isImmutable());

        Assert.assertNull(slug.getFoo());
        Assert.assertNull(slug.getBar());

        Assert.assertNull(slug.get("Foo"));
        Assert.assertNull(slug.get("Bogus"));

        Assert.assertTrue(slug.entrySet().isEmpty());
        Assert.assertTrue(slug.asMap().isEmpty());

        Assert.assertSame(slug, slug.setFoo("Hello World"));

        Assert.assertEquals("Hello World", slug.getFoo());
        Assert.assertNull(slug.getBar());

        Assert.assertEquals("Hello World", slug.get("Foo"));
        Assert.assertNull(slug.get("foo"));

        Assert.assertEquals(1, slug.entrySet().size());
        for (Map.Entry<String, Object> entry : slug.entrySet()) {
            Assert.assertEquals("Foo", entry.getKey());
            Assert.assertEquals("Hello World", entry.getValue());
        }

        Assert.assertEquals(1, slug.asMap().size());
        Assert.assertEquals("Hello World", slug.asMap().get("Foo"));

        Assert.assertSame(slug, slug.set("Foo", null));

        Assert.assertNull(slug.getFoo());
        Assert.assertNull(slug.get("Foo"));
        Assert.assertEquals(0, slug.entrySet().size());
        Assert.assertEquals(0, slug.asMap().size());
    }
}
