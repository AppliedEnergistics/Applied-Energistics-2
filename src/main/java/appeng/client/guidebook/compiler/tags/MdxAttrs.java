package appeng.client.guidebook.compiler.tags;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

/**
 * utilities for dealing with attributes of {@link MdxJsxElementFields}.
 */
public final class MdxAttrs {

    private MdxAttrs() {
    }

    @Nullable
    public static ResourceLocation getRequiredId(PageCompiler compiler, LytErrorSink errorSink,
            MdxJsxElementFields el,
            String attribute) {
        var id = el.getAttributeString(attribute, null);
        if (id == null) {
            errorSink.appendError(compiler, "Missing " + attribute + " attribute.", el);
            return null;
        }

        id = id.trim(); // Trim leading/trailing whitespace for easier use

        ResourceLocation itemId;
        try {
            return compiler.resolveId(id);
        } catch (ResourceLocationException e) {
            errorSink.appendError(compiler, "Malformed id " + id + ": " + e.getMessage(), el);
            return null;
        }
    }

    @Nullable
    public static Pair<ResourceLocation, Item> getRequiredItemAndId(PageCompiler compiler, LytErrorSink errorSink,
            MdxJsxElementFields el,
            String attribute) {
        var itemId = getRequiredId(compiler, errorSink, el, attribute);

        var resultItem = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
        if (resultItem == null) {
            errorSink.appendError(compiler, "Missing item: " + itemId, el);
            return null;
        }
        return Pair.of(itemId, resultItem);
    }

    public static Item getRequiredItem(PageCompiler compiler, LytErrorSink errorSink, MdxJsxElementFields el,
            String attribute) {
        var result = getRequiredItemAndId(compiler, errorSink, el, attribute);
        if (result != null) {
            return result.getRight();
        }
        return null;
    }
}
