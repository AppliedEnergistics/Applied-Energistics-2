package appeng.integration.modules.emi.transfer;

import appeng.core.localization.ItemModText;
import appeng.integration.modules.jeirei.CraftingHelper;
import appeng.integration.modules.jeirei.TransferHelper;
import appeng.menu.me.items.CraftingTermMenu;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static appeng.integration.modules.jeirei.TransferHelper.BLUE_PLUS_BUTTON_COLOR;
import static appeng.integration.modules.jeirei.TransferHelper.ORANGE_PLUS_BUTTON_COLOR;

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
public class EmiUseCraftingRecipeHandler<T extends CraftingTermMenu> extends AbstractRecipeHandler<T> {

    public EmiUseCraftingRecipeHandler(Class<T> containerClass) {
        super(containerClass);
    }

    @Override
    protected Result transferRecipe(T menu, RecipeHolder<?> holder, EmiRecipe emiRecipe, boolean doTransfer) {

        var recipeId = holder != null ? holder.id() : null;
        var recipe = holder != null ? holder.value() : null;

        boolean craftingRecipe = isCraftingRecipe(recipe, emiRecipe);
        if (!craftingRecipe) {
            return Result.createNotApplicable();
        }

        if (!fitsIn3x3Grid(recipe, emiRecipe)) {
            return Result.createFailed(ItemModText.RECIPE_TOO_LARGE.text());
        }

        if (recipe == null) {
            recipe = createFakeRecipe(emiRecipe);
        }

        // Thank you RS for pioneering this amazing feature! :)
        boolean craftMissing = AbstractContainerScreen.hasControlDown();
        // Find missing ingredient
        var slotToIngredientMap = getGuiSlotToIngredientMap(recipe);
        var missingSlots = menu.findMissingIngredients(getGuiSlotToIngredientMap(recipe));

        if (missingSlots.missingSlots().size() == slotToIngredientMap.size()) {
            // All missing, can't do much...
            return Result.createFailed(ItemModText.NO_ITEMS.text()); // TODO .renderer(createErrorRenderer(missingSlots));
        }

        if (!doTransfer) {
            if (missingSlots.totalSize() != 0) {
                // Highlight the slots with missing ingredients
                int color = missingSlots.anyMissing() ? ORANGE_PLUS_BUTTON_COLOR : BLUE_PLUS_BUTTON_COLOR;
                var result = Result.createSuccessful();
                // TODO .color(color)
                // TODO .renderer(createErrorRenderer(missingSlots));

                var tooltip = TransferHelper.createCraftingTooltip(missingSlots, craftMissing);
                // TODO result.overrideTooltipRenderer((point, sink) -> sink.accept(Tooltip.create(tooltip)));

                return result;
            }
        } else {
            CraftingHelper.performTransfer(menu, recipeId, recipe, craftMissing);
        }

        // No error
        return Result.createSuccessful();
    }

    private Recipe<?> createFakeRecipe(EmiRecipe display) {
        var ingredients = NonNullList.withSize(CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT,
                Ingredient.EMPTY);

        for (int i = 0; i < Math.min(display.getInputs().size(), ingredients.size()); i++) {
            var ingredient = Ingredient.of(display.getInputs().get(i).getEmiStacks().stream()
                    .map(EmiStack::getItemStack)
                    .filter(is -> !is.isEmpty())
            );
            ingredients.set(i, ingredient);
        }

        var pattern = new ShapedRecipePattern(CRAFTING_GRID_WIDTH, CRAFTING_GRID_HEIGHT, ingredients, Optional.empty());
        return new ShapedRecipe("", CraftingBookCategory.MISC, pattern, ItemStack.EMPTY);
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
    // TODO private static TransferHandlerRenderer createErrorRenderer(CraftingTermMenu.MissingIngredientSlots indices) {
    // TODO     return (guiGraphics, mouseX, mouseY, delta, widgets, bounds, display) -> {
    // TODO         int i = 0;
    // TODO         for (Widget widget : widgets) {
    // TODO             if (widget instanceof Slot slot && slot.getNoticeMark() == Slot.INPUT) {
    // TODO                 boolean missing = indices.missingSlots().contains(i);
    // TODO                 boolean craftable = indices.craftableSlots().contains(i);
    // TODO                 i++;
    // TODO                 if (missing || craftable) {
    // TODO                     var poseStack = guiGraphics.pose();
    // TODO                     poseStack.pushPose();
    // TODO                     poseStack.translate(0, 0, 400);
    // TODO                     Rectangle innerBounds = slot.getInnerBounds();
    // TODO                     guiGraphics.fill(innerBounds.x, innerBounds.y, innerBounds.getMaxX(),
    // TODO                             innerBounds.getMaxY(), missing ? RED_SLOT_HIGHLIGHT_COLOR : BLUE_SLOT_HIGHLIGHT_COLOR);
    // TODO                     poseStack.popPose();
    // TODO                 }
    // TODO             }
    // TODO         }
    // TODO     };
    // TODO }
}
