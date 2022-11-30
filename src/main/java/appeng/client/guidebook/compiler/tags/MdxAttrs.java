package appeng.client.guidebook.compiler.tags;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.LytErrorSink;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;
import appeng.libs.mdast.model.MdAstNode;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/**
 * utilities for dealing with attributes of {@link MdxJsxElementFields}.
 */
public final class MdxAttrs {

    private MdxAttrs() {
    }

    @Nullable
    public static Item getRequiredItem(PageCompiler compiler, LytErrorSink errorSink, MdxJsxElementFields el,
                                       String attribute) {
        var id = el.getAttributeString(attribute, null);
        if (id == null) {
            errorSink.appendError(compiler, "Missing " + attribute + " attribute.", (MdAstNode) el);
            return null;
        }

        ResourceLocation itemId;
        try {
            itemId = compiler.resolveId(id);
        } catch (ResourceLocationException e) {
            errorSink.appendError(compiler, "Malformed item id " + id + ": " + e.getMessage(), (MdAstNode) el);
            return null;
        }

        var resultItem = Registry.ITEM.getOptional(itemId).orElse(null);
        if (resultItem == null) {
            errorSink.appendError(compiler, "Missing item: " + itemId, (MdAstNode) el);
            return null;
        }
        return resultItem;
    }
}
