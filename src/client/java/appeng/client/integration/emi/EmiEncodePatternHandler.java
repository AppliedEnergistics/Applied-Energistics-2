package appeng.client.integration.emi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.world.item.crafting.RecipeHolder;

import dev.emi.emi.api.recipe.EmiRecipe;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.itemlists.EncodingHelper;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.items.PatternEncodingTermMenu;

/**
 * Handles encoding patterns in the {@link PatternEncodingTermMenu} by clicking the + button on recipes shown in REI (or
 * JEI).
 */
public class EmiEncodePatternHandler<T extends PatternEncodingTermMenu> extends AbstractRecipeHandler<T> {

    public EmiEncodePatternHandler(Class<T> containerClass) {
        super(containerClass);
    }

    @Override
    protected Result transferRecipe(T menu, RecipeHolder<?> holder, EmiRecipe emiRecipe, boolean doTransfer) {

        var recipeId = holder != null ? holder.id() : null;
        var recipe = holder != null ? holder.value() : null;

        // Crafting recipe slots are not grouped, hence they must fit into the 3x3 grid.
        boolean craftingRecipe = isCraftingRecipe(recipe, emiRecipe);
        if (craftingRecipe && !fitsIn3x3Grid(recipe, emiRecipe)) {
            return Result.createFailed(ItemModText.RECIPE_TOO_LARGE.text());
        }

        if (doTransfer) {
            if (craftingRecipe && recipeId != null) {
                EncodingHelper.encodeCraftingRecipe(menu,
                        new RecipeHolder<>(recipeId, recipe),
                        getGuiIngredientsForCrafting(emiRecipe),
                        stack -> true);
            } else {
                EncodingHelper.encodeProcessingRecipe(menu,
                        EmiStackHelper.ofInputs(emiRecipe),
                        EmiStackHelper.ofOutputs(emiRecipe));
            }
        } else {
            var repo = menu.getClientRepo();
            Set<AEKey> craftableKeys = repo != null ? repo.getAllEntries().stream()
                    .filter(GridInventoryEntry::isCraftable)
                    .map(GridInventoryEntry::getWhat)
                    .collect(Collectors.toSet()) : Set.of();

            return new Result.EncodeWithCraftables(craftableKeys);
        }

        return Result.createSuccessful();
    }

    /**
     * In case the recipe does not report inputs, we will use the inputs shown on the EMI GUI instead.
     */
    private List<List<GenericStack>> getGuiIngredientsForCrafting(EmiRecipe emiRecipe) {
        var result = new ArrayList<List<GenericStack>>(CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT);
        for (int i = 0; i < CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT; i++) {
            var stacks = new ArrayList<GenericStack>();

            if (i < emiRecipe.getInputs().size()) {
                for (var emiStack : emiRecipe.getInputs().get(i).getEmiStacks()) {
                    var genericStack = EmiStackHelper.toGenericStack(emiStack);
                    if (genericStack != null && genericStack.what() instanceof AEItemKey) {
                        stacks.add(genericStack);
                    }
                }
            }

            result.add(stacks);
        }

        return result;
    }
}
