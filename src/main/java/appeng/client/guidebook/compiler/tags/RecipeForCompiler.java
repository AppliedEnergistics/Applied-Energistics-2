package appeng.client.guidebook.compiler.tags;

import net.minecraft.client.Minecraft;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.flow.LytFlowParent;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;
import appeng.libs.mdast.model.MdAstNode;

/**
 * Shows a Recipe-Book-Like representation of the recipe needed to craft a given item.
 */
public class RecipeForCompiler extends FlowTagCompiler {
    @Override
    protected void compile(PageCompiler compiler, LytFlowParent parent, MdxJsxElementFields el) {
        var item = MdxAttrs.getRequiredItem(compiler, parent, el, "id");
        if (item == null) {
            return;
        }

        var id = item.builtInRegistryHolder().key().location();

        // Find the recipe
        var level = Minecraft.getInstance().level;
        if (level == null) {
            parent.append(compiler.createErrorFlowContent("Cannot show recipe for " + id + " while not in-game",
                    (MdAstNode) el));
            return;
        }
    }

}
