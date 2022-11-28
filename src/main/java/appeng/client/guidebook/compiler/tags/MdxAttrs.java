package appeng.client.guidebook.compiler.tags;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.flow.LytFlowParent;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;
import appeng.libs.mdast.model.MdAstNode;

/**
 * utilities for dealing with attributes of {@link MdxJsxElementFields}.
 */
public final class MdxAttrs {

    private MdxAttrs() {
    }

    @Nullable
    public static Item getRequiredItem(PageCompiler compiler, LytFlowParent parent, MdxJsxElementFields el,
            String attribute) {
        var id = el.getAttributeString(attribute, null);
        if (id == null) {
            parent.append(compiler.createErrorFlowContent("Missing " + attribute + " attribute.", (MdAstNode) el));
            return null;
        }

        ResourceLocation itemId;
        try {
            itemId = compiler.resolveId(id);
        } catch (ResourceLocationException e) {
            parent.append(
                    compiler.createErrorFlowContent("Malformed item id " + id + ": " + e.getMessage(), (MdAstNode) el));
            return null;
        }

        var resultItem = Registry.ITEM.getOptional(itemId).orElse(null);
        if (resultItem == null) {
            parent.append(compiler.createErrorFlowContent("Missing item: " + itemId, (MdAstNode) el));
            return null;
        }
        return resultItem;
    }
}
