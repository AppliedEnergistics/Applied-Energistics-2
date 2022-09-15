package appeng.integration.modules.rei.transfer;

import static appeng.integration.modules.jeirei.TransferHelper.BLUE_PLUS_BUTTON_COLOR;
import static appeng.integration.modules.jeirei.TransferHelper.ORANGE_PLUS_BUTTON_COLOR;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRenderer;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;

import appeng.core.AppEng;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.jeirei.CraftingHelper;
import appeng.integration.modules.jeirei.TransferHelper;
import appeng.menu.me.items.CraftingTermMenu;

/**
 * Recipe transfer implementation with the intended purpose of actually crafting an item. Most of the work is done
 * server-side because permission-checks and inventory extraction cannot be done client-side.
 * <p>
 * Here is how it works, depending on the various cases. In any case, we highlight missing entries in red and craftable
 * entries in blue. How the {@code +} button is rendered and what it does depends on various cases:
 * <ul>
 * <li><b>All items are present:</b> normal gray, can click + to move.</li>
 * <li><b>All items are missing:</b> red, can't click.</li>
 * <li><b>Some items are missing, all craftable:</b> blue, can click to move what's available or ctrl + click to
 * additionally schedule autocrafting of what's craftable.</li>
 * <li><b>Some items are missing, some not craftable:</b> orange, same action as above.</li>
 * </ul>
 */
public class UseCraftingRecipeTransfer<T extends CraftingTermMenu> extends AbstractTransferHandler<T> {

    public UseCraftingRecipeTransfer(Class<T> containerClass) {
        super(containerClass);
    }

    @Override
    protected Result transferRecipe(T menu, Recipe<?> recipe, Display display, boolean doTransfer) {

        boolean craftingRecipe = isCraftingRecipe(recipe, display);
        if (!craftingRecipe) {
            return Result.createNotApplicable();
        }

        if (!fitsIn3x3Grid(recipe, display)) {
            return Result.createFailed(ItemModText.RECIPE_TOO_LARGE.text());
        }

        if (recipe == null) {
            recipe = createFakeRecipe(display);
        }

        // Thank you RS for pioneering this amazing feature! :)
        boolean craftMissing = AbstractContainerScreen.hasControlDown();
        // Find missing ingredient
        var slotToIngredientMap = getGuiSlotToIngredientMap(recipe);
        var missingSlots = menu.findMissingIngredients(getGuiSlotToIngredientMap(recipe));

        if (missingSlots.missingSlots().size() == slotToIngredientMap.size()) {
            // All missing, can't do much...
            return Result.createFailed(ItemModText.NO_ITEMS.text()).renderer(createErrorRenderer(missingSlots));
        }

        if (!doTransfer) {
            if (missingSlots.totalSize() != 0) {
                // Highlight the slots with missing ingredients
                int color = missingSlots.anyMissing() ? ORANGE_PLUS_BUTTON_COLOR : BLUE_PLUS_BUTTON_COLOR;
                var result = Result.createSuccessful()
                        .color(color)
                        .renderer(createErrorRenderer(missingSlots));

                var tooltip = TransferHelper.createCraftingTooltip(missingSlots, craftMissing);
                result.overrideTooltipRenderer((point, sink) -> sink.accept(Tooltip.create(tooltip)));

                return result;
            }
        } else {
            CraftingHelper.performTransfer(menu, recipe, craftMissing);
        }

        // No error
        return Result.createSuccessful().blocksFurtherHandling();
    }

    private Recipe<?> createFakeRecipe(Display display) {
        var ingredients = NonNullList.withSize(CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT,
                Ingredient.EMPTY);

        for (int i = 0; i < Math.min(display.getInputEntries().size(), ingredients.size()); i++) {
            var ingredient = Ingredient.of(display.getInputEntries().get(i).stream()
                    .filter(es -> es.getType() == VanillaEntryTypes.ITEM)
                    .map(es -> (ItemStack) es.castValue()));
            ingredients.set(i, ingredient);
        }

        return new ShapedRecipe(AppEng.makeId("__fake_recipe"), "", CraftingBookCategory.MISC, CRAFTING_GRID_WIDTH,
                CRAFTING_GRID_HEIGHT,
                ingredients, ItemStack.EMPTY);
    }

    public static Map<Integer, Ingredient> getGuiSlotToIngredientMap(Recipe<?> recipe) {
        var ingredients = recipe.getIngredients();

        // JEI will align non-shaped recipes smaller than 3x3 in the grid. It'll center them horizontally, and
        // some will be aligned to the bottom. (i.e. slab recipes).
        int width;
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            width = shapedRecipe.getWidth();
        } else {
            width = CRAFTING_GRID_WIDTH;
        }

        var result = new HashMap<Integer, Ingredient>(ingredients.size());
        for (int i = 0; i < ingredients.size(); i++) {
            var guiSlot = (i / width) * CRAFTING_GRID_WIDTH + (i % width);
            var ingredient = ingredients.get(i);
            if (!ingredient.isEmpty()) {
                result.put(guiSlot, ingredient);
            }
        }
        return result;
    }

    /**
     * Draw missing slots.
     */
    private static TransferHandlerRenderer createErrorRenderer(CraftingTermMenu.MissingIngredientSlots indices) {
        return (matrices, mouseX, mouseY, delta, widgets, bounds, display) -> {
            int i = 0;
            for (Widget widget : widgets) {
                // TODO 1.19.3 if (widget instanceof Slot slot && slot.getNoticeMark() == Slot.INPUT) {
                // TODO 1.19.3 boolean missing = indices.missingSlots().contains(i);
                // TODO 1.19.3 boolean craftable = indices.craftableSlots().contains(i);
                // TODO 1.19.3 i++;
                // TODO 1.19.3 if (missing || craftable) {
                // TODO 1.19.3 matrices.pushPose();
                // TODO 1.19.3 matrices.translate(0, 0, 400);
                // TODO 1.19.3 Rectangle innerBounds = slot.getInnerBounds();
                // TODO 1.19.3 GuiComponent.fill(matrices, innerBounds.x, innerBounds.y, innerBounds.getMaxX(),
                // TODO 1.19.3 innerBounds.getMaxY(), missing ? RED_SLOT_HIGHLIGHT_COLOR : BLUE_SLOT_HIGHLIGHT_COLOR);
                // TODO 1.19.3 matrices.popPose();
                // TODO 1.19.3 }
                // TODO 1.19.3 }
            }
        };
    }
}
