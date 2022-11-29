package appeng.client.guidebook.compiler.tags;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.block.LytCraftingRecipe;
import appeng.client.guidebook.document.flow.LytFlowInlineBlock;
import appeng.client.guidebook.document.flow.LytFlowParent;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;
import appeng.libs.mdast.model.MdAstNode;
import appeng.util.Platform;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

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
        var recipeManager = Platform.getClientRecipeManager();
        if (recipeManager == null) {
            parent.append(compiler.createErrorFlowContent("Cannot show recipe for " + id + " while not in-game",
                    (MdAstNode) el));
            return;
        }

        CraftingRecipe foundRecipe = null;
        for (var recipe : recipeManager.getAllRecipesFor(RecipeType.CRAFTING)) {
            if (recipe.getResultItem().getItem() == item) {
                foundRecipe = recipe;
                break;
            }
        }

        if (foundRecipe == null) {
            // TODO This *can* be legit if there's no recipe due to datapacks
            parent.append(compiler.createErrorFlowContent("Couldn't find recipe for " + id, (MdAstNode) el));
            return;
        }

        var inlineBlock = new LytFlowInlineBlock();
        inlineBlock.setBlock(new LytCraftingRecipe(foundRecipe));
        parent.append(inlineBlock);
    }

}
