package appeng.recipes.entropy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class PropertyValueMatcherTest {

    @Test
    void testDecodeSingleValue() {
        var valueMatcher = decodeJson("""
                "xyz"
                """);
        assertEquals(new PropertyValueMatcher.SingleValue("xyz"), valueMatcher);
    }

    @Test
    void testEncodeSingleValue() {
        var json = encodeJson(new PropertyValueMatcher.SingleValue("xyz"));
        assertEquals("\"xyz\"", json);
    }

    @Test
    void testDecodeMultiValue() {
        var valueMatcher = decodeJson("""
                ["a", "b"]
                """);
        assertEquals(new PropertyValueMatcher.MultiValue(List.of("a", "b")), valueMatcher);
    }

    @Test
    void testEncodeMultiValue() {
        var json = encodeJson(new PropertyValueMatcher.MultiValue(List.of("a", "b")));
        assertEquals("[\"a\",\"b\"]", json);
    }

    @Test
    void testDecodeRange() {
        var valueMatcher = decodeJson("""
                {"min": "a", "max": "b"}
                """);
        assertEquals(new PropertyValueMatcher.Range("a", "b"), valueMatcher);
    }

    @Test
    void testEncodeRange() {
        var json = encodeJson(new PropertyValueMatcher.Range("a", "b"));
        assertEquals("{\"min\":\"a\",\"max\":\"b\"}", json);
    }

    @Test
    void testErrorReporting() {
        Assertions.assertThatThrownBy(() -> decodeJson("123"))
                .hasMessage("Property values need to be strings, list of strings, or objects with min/max properties");
        Assertions.assertThatThrownBy(() -> decodeJson("{\"a\":123}"))
                .hasMessage("Property values need to be strings, list of strings, or objects with min/max properties");
    }

    private String encodeJson(PropertyValueMatcher config) {
        var dataResult = PropertyValueMatcher.CODEC.encodeStart(JsonOps.INSTANCE, config);
        if (dataResult.result().isPresent()) {
            return dataResult.result().get().toString();
        }
        throw new RuntimeException(dataResult.error().get().message());
    }

    private PropertyValueMatcher decodeJson(String jsonText) {
        var tree = new Gson().fromJson(jsonText, JsonElement.class);
        var dataResult = PropertyValueMatcher.CODEC.decode(JsonOps.INSTANCE, tree);
        if (dataResult.result().isPresent()) {
            return dataResult.result().get().getFirst();
        }
        throw new RuntimeException(dataResult.error().get().message());
    }
}
