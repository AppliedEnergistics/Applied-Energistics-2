package appeng.libs.mdast;

import appeng.libs.mdast.model.MdAstNode;
import appeng.libs.mdast.model.MdAstParent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.bind.JsonTreeWriter;
import org.intellij.lang.annotations.Language;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractMdAstTest {
    protected static final Gson GSON = new GsonBuilder()
            .setLenient()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    protected static String toJson(String markdown) {
        return toJson(markdown, new MdastOptions());
    }

    protected static String toJsonFirstNode(String markdown) {
        return toJsonFirstNode(markdown, new MdastOptions());
    }

    protected static String toJson(String markdown, MdastOptions options) {
        var tree = MdAst.fromMarkdown(markdown, options);
        return toJson(tree);
    }

    protected static String toJson(MdAstNode node) {
        try {
            var jsonWriter = new JsonTreeWriter();
            jsonWriter.setHtmlSafe(false);
            jsonWriter.setIndent("  ");
            node.toJson(jsonWriter);
            return GSON.toJson(normalizeTree(jsonWriter.get()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static String toJsonFirstNode(String markdown, MdastOptions options) {
        var tree = (MdAstNode) MdAst.fromMarkdown(markdown, options).children().get(0);

        try {
            var jsonWriter = new JsonTreeWriter();
            tree.toJson(jsonWriter);
            return GSON.toJson(normalizeTree(jsonWriter.get()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static void assertJsonEquals(String markdown, @Language("json") String astJson) {
        var actualJson = toJson(markdown);
        var expectedJson = normalizeJson(astJson);
        assertEquals(expectedJson, actualJson);
    }

    protected static void assertJsonEquals(MdAstNode node, @Language("json") String astJson) {
        var actualJson = toJson(node);
        var expectedJson = normalizeJson(astJson);
        assertEquals(expectedJson, actualJson);
    }

    protected static void assertJsonEqualsOnFirstNode(String markdown, @Language("json") String astJson) {
        var actualJson = toJsonFirstNode(markdown);
        var expectedJson = normalizeJson(astJson);
        assertEquals(expectedJson, actualJson);
    }

    protected static String normalizeJson(@Language("json") String json) {
        var el = GSON.fromJson(json, JsonElement.class);
        el = normalizeTree(el);
        return GSON.toJson(el);
    }

    protected static JsonElement normalizeTree(JsonElement element) {
        if (element.isJsonArray()) {
            var normalizedArray = new JsonArray();
            for (var jsonElement : element.getAsJsonArray()) {
                normalizedArray.add(normalizeTree(jsonElement));
            }
            return normalizedArray;
        } else if (element.isJsonObject()) {
            var props = new ArrayList<>(element.getAsJsonObject().entrySet());
            props.sort(
                    // Sort type always first
                    Comparator.<Map.Entry<String, JsonElement>, Boolean>comparing(e -> e.getKey().equals("type")).reversed()
                            // Children always last
                            .thenComparing(e -> e.getKey().equals("children"))
                            // The rest alphabetically in-between
                            .thenComparing(Map.Entry::getKey, Comparator.naturalOrder())
            );
            var sortedObj = new JsonObject();
            for (var prop : props) {
                sortedObj.add(prop.getKey(), normalizeTree(prop.getValue()));
            }
            return sortedObj;
        } else {
            return element;
        }
    }

    protected static MdAstNode removePosition(MdAstNode node) {
        node.position = null;
        if (node instanceof MdAstParent<?> parent) {
            for (var child : parent.children()) {
                if (child instanceof MdAstNode childNode) {
                    removePosition(childNode);
                }
            }
        }
        return node;
    }
}
