package appeng.client.guidebook.compiler;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import appeng.client.guidebook.compiler.tags.ATagCompiler;
import appeng.client.guidebook.compiler.tags.DivTagCompiler;
import appeng.client.guidebook.compiler.tags.ItemLinkCompiler;

/**
 * Maintains a mapping between MDX Tag-Names to handlers for compiling these tags.
 */
public final class TagCompilers {
    private static final Map<String, TagCompiler> handlers = new HashMap<>();

    static {
        register("div", new DivTagCompiler());
        register("a", new ATagCompiler());
        register("ItemLink", new ItemLinkCompiler());
    }

    public static void register(String tagName, TagCompiler handler) {
        tagName = normalizeTagName(tagName);
        if (handlers.containsKey(tagName)) {
            throw new IllegalStateException("MDX handler for tag " + tagName + " is already registered");
        }
        handlers.put(tagName, handler);
    }

    public static TagCompiler get(String tagName) {
        return handlers.get(normalizeTagName(tagName));
    }

    public static void remove(String tagName) {
        handlers.remove(normalizeTagName(tagName));
    }

    private static String normalizeTagName(String tagName) {
        return tagName.toLowerCase(Locale.ROOT);
    }
}
