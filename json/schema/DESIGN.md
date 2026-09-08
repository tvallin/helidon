# JSON Schema Default Metadata API

## Status

This document describes the JSON Schema `default` metadata API introduced by
[helidon-io/helidon#12478](https://github.com/helidon-io/helidon/pull/12478) to resolve
[helidon-io/helidon#12476](https://github.com/helidon-io/helidon/issues/12476).

## Context

JSON Schema 2020-12 defines `default` as an annotation keyword. It associates a suggested value with a schema, but it
does not require a validator to insert that value when instance data omits a property. The value can be any JSON value,
including a string, number, boolean, `null`, array, or object. The specification recommends that the value validate
against its schema, but does not require it.

Helidon's imperative JSON Schema API represents this keyword as `JsonValue` metadata on `SchemaItem`. This representation
can preserve every JSON value type and can round-trip defaults when parsing and generating schemas. The declarative
annotation API also needs to expose this capability so generated schemas have the same metadata as imperatively built
schemas.

## Problem

An initial declarative design used one annotation whose Java `String` value was interpreted as serialized JSON:

```java
@JsonSchema.Default("0")       // JSON number 0
@JsonSchema.Default("\"0\"")   // JSON string "0"
```

This design has two problems.

First, the Java source does not make the generated JSON type clear. A Java string literal that looks like a number,
boolean, or `null` does not generate a JSON string. Actual string defaults require a second JSON-encoding layer, which is
particularly difficult to read for quotes, backslashes, and newlines. Because annotations are public API, changing this
interpretation after release could silently change generated schemas without causing a source or binary compatibility
failure.

Second, the general-purpose Helidon JSON parser accepts some input forms that are useful outside strict document
validation. For example, it accepts `01` as a number and normalizes it to `1`. Using that behavior for annotation input
would allow malformed JSON to compile and would silently change the value placed in the generated schema.

## Goals

- Make the JSON type of common declarative defaults evident at the annotation call site.
- Avoid nested JSON encoding for string defaults.
- Preserve support for every JSON value type.
- Reject malformed raw JSON during code generation instead of normalizing it.
- Keep declarative and imperative schema generation behavior consistent.
- Preserve the JSON Schema meaning of `default` as metadata only.
- Follow the established typed-default convention used by `Option.Default*` annotations.

## Non-goals

- Populate missing properties in JSON instance data.
- Change whether a schema property is required.
- Validate that a default satisfies the schema that contains it.
- Change the parsing rules of the general-purpose `JsonParser` API.
- Add new JSON Schema validation behavior or select a different schema dialect.

## API design

The declarative API uses a family of mutually exclusive annotations. Each schema element can declare at most one default
annotation. The annotations can be applied to schema types and to properties represented by methods or fields, including
record components whose annotations are propagated to those elements by the Java compiler.

| Annotation | Java value type | Generated JSON type | Intended use |
| --- | --- | --- | --- |
| `@JsonSchema.Default` | `String` | string | Normal string defaults |
| `@JsonSchema.DefaultInt` | `int` | number | Integer defaults in the Java `int` range |
| `@JsonSchema.DefaultLong` | `long` | number | Integer defaults in the Java `long` range |
| `@JsonSchema.DefaultDouble` | `double` | number | Fractional and floating-point defaults |
| `@JsonSchema.DefaultBoolean` | `boolean` | boolean | Boolean defaults |
| `@JsonSchema.DefaultJson` | JSON text in a `String` | any | `null`, arrays, objects, or numbers not covered by the typed annotations |

For example:

```java
@JsonSchema.Schema
record Defaults(
        @JsonSchema.Default("0") String text,
        @JsonSchema.DefaultInt(0) int count,
        @JsonSchema.DefaultLong(9_007_199_254_740_993L) long identifier,
        @JsonSchema.DefaultDouble(0.5) double ratio,
        @JsonSchema.DefaultBoolean(true) boolean enabled) {
}
```

The generated defaults are, respectively, the JSON string `"0"`, the numbers `0`, `9007199254740993`, and `0.5`, and the
boolean `true`.

`DefaultJson` makes the additional encoding step explicit when a Java annotation value cannot directly represent the
desired JSON value:

```java
@JsonSchema.Schema
@JsonSchema.DefaultJson("{\"enabled\":true,\"roles\":[\"reader\"]}")
final class Configuration {
}
```

The raw form remains an escape hatch rather than the normal representation for primitive defaults. A string should use
`Default`, and values that fit the typed numeric or boolean annotations should use those annotations.

## Code generation behavior

The code generator performs the following steps for every annotated schema element:

1. Find all annotations from the default annotation family.
2. Fail code generation if more than one default annotation is present.
3. Convert a typed annotation directly to the corresponding `JsonValue` representation.
4. For `DefaultJson`, require exactly one JSON value and reject malformed JSON number tokens, including leading-zero forms
   such as `01`.
5. Add the resulting value under the `default` keyword without changing `type`, `required`, or any validation keyword.

Invalid annotation input is a compile-time code-generation error. In particular, the generator does not normalize invalid
input into a different valid value.

The generated provider serializes the resulting schema using the normal Helidon JSON generator. When the provider is
loaded, the default is parsed back into the same `JsonValue` type exposed by `SchemaItem.defaultValue()`.

## Imperative API

The imperative API remains based on `JsonValue`:

```java
Schema schema = Schema.builder()
        .rootObject(root -> root.addIntegerProperty("count", property -> property
                .defaultValue(JsonNumber.create(0))))
        .build();
```

`SchemaItem.defaultValue()` returns `Optional<JsonValue>`. The default implementation returns an empty optional so
existing third-party `SchemaItem` implementations remain compatible. Schema parsing reads the complete JSON value of the
`default` keyword, and schema generation writes it without changing its JSON type.

## Scope coverage

| Problem scope | Design coverage |
| --- | --- |
| Numeric-looking Java strings were ambiguous | `Default` always means a JSON string; numeric annotations have explicit names |
| String defaults required escaped JSON quotes | `Default` accepts the string value directly |
| Primitive defaults should be concise | Dedicated int, long, double, and boolean annotations avoid raw JSON text |
| JSON Schema allows `null`, arrays, objects, and arbitrary numbers | `DefaultJson` preserves the full JSON value space |
| Malformed values such as `01` were normalized | Raw JSON validation rejects invalid number grammar during code generation |
| Multiple annotations could define competing defaults | Code generation rejects more than one default annotation per element |
| Parsed and generated schemas must retain the value type | The shared `JsonValue` model is used by parsing, builders, codegen, and generation |
| A default must not make a property required or fill missing data | The implementation only emits metadata under the `default` keyword |

## Compatibility

The annotation API is new in this change, so the ambiguous serialized-JSON interpretation can be replaced before it
becomes a released contract. The typed family follows an existing Helidon naming pattern and makes future call sites
source-stable: `@JsonSchema.Default("0")` will continue to mean the JSON string `"0"`.

The imperative addition is backward compatible. `defaultValue()` is a default method returning `Optional.empty()`, so an
existing implementation of `SchemaItem` does not need to add a method. Schemas without a default generate exactly as they
did before this change.

## Validation strategy

The change is covered at three levels:

- Schema model tests verify imperative construction, parsing, generation, all JSON value types, and compatibility with an
  existing `SchemaItem` implementation.
- Code-generation tests verify that trailing values, invalid numbers such as `01`, and competing default annotations fail
  compilation, while trailing whitespace remains valid.
- Generated-provider tests verify the JSON types and values produced by all typed annotations and by `DefaultJson`,
  including Java string escaping and integers that cannot be represented exactly as JavaScript numbers.

## Alternatives considered

### One annotation containing serialized JSON

This supports every JSON value with a small implementation, but makes common source code ambiguous and requires strings to
be encoded twice. It was rejected because this ambiguity would become part of the public API contract.

### One annotation with a type discriminator

An annotation such as `@Default(type = STRING, value = "0")` would identify the type, but every call site would be more
verbose and the value would still need conversion from a string. Typed annotation members provide Java compiler checking
and follow an established Helidon convention.

### Typed primitive annotations only

This gives clear call sites but cannot represent `null`, arrays, objects, or arbitrary-precision numbers. `DefaultJson`
retains that necessary escape hatch while making raw JSON use explicit.

### Make `JsonParser` globally strict

Changing the general-purpose parser would affect unrelated callers and could introduce compatibility problems outside JSON
Schema code generation. Local validation of `DefaultJson` keeps the behavior change within this feature's scope.
