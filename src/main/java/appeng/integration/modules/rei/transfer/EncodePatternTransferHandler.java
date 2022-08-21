package appeng.integration.modules.rei.transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;

import appeng.api.stacks.GenericStack;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.integration.modules.rei.GenericEntryStackHelper;
import appeng.menu.me.items.PatternEncodingTermMenu;

/**
 * Handles encoding patterns in the {@link PatternEncodingTermMenu} by clicking the + button on recipes shown in REI (or
 * JEI).
 */
public class EncodePatternTransferHandler<T extends PatternEncodingTermMenu> extends AbstractTransferHandler<T> {

    private final IngredientVisibility ingredientVisibility = new IngredientVisibility();

    public EncodePatternTransferHandler(Class<T> containerClass) {
        super(containerClass);
    }

    @Override
    protected Result transferRecipe(T menu, Recipe<?> recipe, Display display, boolean doTransfer) {

        // Crafting recipe slots are not grouped, hence they must fit into the 3x3 grid.
        boolean craftingRecipe = isCraftingRecipe(recipe, display);
        if (craftingRecipe && !fitsIn3x3Grid(recipe, display)) {
            return Result.createFailed(ItemModText.RECIPE_TOO_LARGE.text());
        }

        if (doTransfer) {
            if (craftingRecipe) {
                EncodingHelper.encodeCraftingRecipe(menu,
                        recipe,
                        getGuiIngredientsForCrafting(display),
                        this::isIngredientVisible);
            } else {
                EncodingHelper.encodeProcessingRecipe(menu,
                        GenericEntryStackHelper.ofInputs(display),
                        GenericEntryStackHelper.ofOutputs(display));
            }
        }

        return Result.createSuccessful().blocksFurtherHandling();
    }

    private boolean isIngredientVisible(ItemStack itemStack) {
        return ingredientVisibility.isVisible(itemStack);
    }

    /**
     * In case the recipe does not report inputs, we will use the inputs shown on the JEI GUI instead.
     */
    private List<List<GenericStack>> getGuiIngredientsForCrafting(Display recipeLayout) {
        var result = new ArrayList<List<GenericStack>>(CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT);
        for (int i = 0; i < CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT; i++) {
            var stacks = new ArrayList<GenericStack>();

            if (i < recipeLayout.getInputEntries().size()) {
                for (EntryStack<?> entryStack : recipeLayout.getInputEntries().get(i)) {
                    if (entryStack.getType() == VanillaEntryTypes.ITEM) {
                        stacks.add(GenericStack.fromItemStack(entryStack.castValue()));
                    }
                }
            }

            result.add(stacks);
        }

        return result;
    }

    private static class IngredientVisibility {

        private final EntryRegistry registry;
        private final Map<ItemStack, Boolean> cache = new HashMap<>();

        private IngredientVisibility() {
            this.registry = EntryRegistry.getInstance();
        }

        private boolean isVisible(ItemStack stack) {
            if (cache.containsKey(stack)) {
                return cache.get(stack);
            }

            var entryStack = EntryStacks.of(stack);
            if (!registry.alreadyContain(entryStack)) {
                cache.put(stack, false);
                return false;
            }

            var entryStacks = registry.refilterNew(false, Collections.singleton(entryStack));
            var visible = !entryStacks.isEmpty();
            cache.put(stack, visible);
            return visible;
        }
    }
}
