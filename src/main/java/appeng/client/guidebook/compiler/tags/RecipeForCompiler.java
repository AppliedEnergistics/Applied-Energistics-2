package appeng.client.guidebook.compiler.tags;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.block.LytBlockContainer;
import appeng.client.guidebook.document.block.LytCraftingRecipe;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;
import appeng.libs.mdast.model.MdAstNode;
import appeng.util.Platform;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Shows a Recipe-Book-Like representation of the recipe needed to craft a given item.
 */
public class RecipeForCompiler extends BlockTagCompiler {
    @Override
    protected void compile(PageCompiler compiler, LytBlockContainer parent, MdxJsxElementFields el) {
        var item = MdxAttrs.getRequiredItem(compiler, parent, el, "id");
        if (item == null) {
            return;
        }

        var id = item.builtInRegistryHolder().key().location();

        // Find the recipe
        var recipeManager = Platform.getClientRecipeManager();
        if (recipeManager == null) {
            parent.appendError(compiler, "Cannot show recipe for " + id + " while not in-game",
                    (MdAstNode) el);
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
            parent.appendError(compiler, "Couldn't find recipe for " + id, (MdAstNode) el);
            return;
        }

        parent.append(new LytCraftingRecipe(foundRecipe));
    }

}
