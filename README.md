# Slug
Slug is a framework for generating flexible data types.

## slug-core

Slug allows you to define a loose schema for a set of data by writing an
interface for it:

```java
public interface Example extends Slug<Example> {

    String getName();
    void setName(String name);

    Integer getAge();
    Example setAge(Integer age);
}
```

A `SlugBox` will generate an implementation of this interface at
runtime that's backed by a `Map`, allowing easy access to the data in
the map via the getters and setters defined in the interface, but also
allowing you to store and access additional members directly via
`Slug.get(String)` and `Slug.set(String, Object)`, yielding
flexibility as schemas evolve over time.

```java
SlugBox box = ...;
Example example = box.create(Example.class);

example.setName("David Murray");
example.set("Job", "Cat Dad");

example.getName();   // "David Murray"
example.get("Name"); // "David Murray"

for (Map.Entry<String, String> entry : example.entrySet()) {
    ...
}
```

Implementations of the `SlugModule` interface provide strategies for
serializing and deserializing slugs from various wire formats. Slug's
loosely-typed model allows it to represent and round-trip data that wasn't
part of the schema at the time it was baked, yielding greater flexibility
when rolling out upgrades across a distributed system.

```java
SlugModule module = ...;

Object obj = module.deserializeTo(input, Example.class);
if (!(obj instanceof Example)) {
    log.warn("Unexpected data format: " + obj);
    return;
}
Example example = (Example) obj;

if (example.getAge() == null) {
    example.setAge(1);
} else {
    example.setAge(example.getAge() + 1);
}

module.serializeTo(example, output);
```

## slug-json

The `JsonSlugModule` class is an implementation of `SlugModule` that serializes
to an deserializes from JSON (and similar formats) using 
[Jackson](https://github.com/FasterXML/jackson-core).

```java
JsonFactory factory = ...;
SlugModule module = JsonSlugModule.builder()
    .withJsonFactory(factory)
    .build();

Example example = (Example) module.deserializeTo(data, Example.class);
```
