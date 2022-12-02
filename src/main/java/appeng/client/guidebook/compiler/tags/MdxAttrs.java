package appeng.client.guidebook.compiler.tags;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
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
    public static Pair<ResourceLocation, Item> getRequiredItemAndId(PageCompiler compiler, LytErrorSink errorSink,
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
            itemId = compiler.resolveId(id);
        } catch (ResourceLocationException e) {
            errorSink.appendError(compiler, "Malformed item id " + id + ": " + e.getMessage(), el);
            return null;
        }

        var resultItem = Registry.ITEM.getOptional(itemId).orElse(null);
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
