package appeng.integration.modules.jei.transfer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRenderer;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import net.minecraft.network.chat.TranslatableComponent;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;

import appeng.api.stacks.AEItemKey;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.localization.ItemModText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.FillCraftingGridFromRecipePacket;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.util.CraftingRecipeUtil;

/**
 * Recipe transfer implementation with the intended purpose of actually crafting an item. Most of the work is done
 * server-side because permission-checks and inventory extraction cannot be done client-side.
 */
public class UseCraftingRecipeTransfer<T extends CraftingTermMenu> extends AbstractTransferHandler<T> {

    private static final Comparator<GridInventoryEntry> ENTRY_COMPARATOR = Comparator
            .comparing(GridInventoryEntry::getStoredAmount);

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

        // Find missing ingredients and highlight the slots which have these
        if (!doTransfer) {
            var missingSlots = menu.findMissingIngredients(getGuiSlotToIngredientMap(recipe));
            if (!missingSlots.isEmpty()) {
                return Result.createFailed(new TranslatableComponent("error.rei.not.enough.materials"))
                        .renderer(getMissingRenderer(missingSlots));
            }
        } else {
            performTransfer(menu, recipe);
        }

        // No error
        return Result.createSuccessful().blocksFurtherHandling();
    }

    private Recipe<?> createFakeRecipe(Display display) {
        var ingredients = NonNullList.withSize(CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT,
                Ingredient.EMPTY);

        for (var input : ((DefaultCraftingDisplay<?>) display).getInputIngredients(CRAFTING_GRID_WIDTH, CRAFTING_GRID_HEIGHT)) {
            var ingredient = Ingredient.of(input.get().stream()
                    .filter(es -> es.getType() == VanillaEntryTypes.ITEM)
                    .map(es -> (ItemStack) es.castValue()));
            ingredients.set(input.getIndex(), ingredient);
        }

        return new ShapedRecipe(AppEng.makeId("__fake_recipe"), "", CRAFTING_GRID_WIDTH, CRAFTING_GRID_HEIGHT,
                ingredients, ItemStack.EMPTY);
    }

    protected void performTransfer(T menu, Recipe<?> recipe) {

        // We send the items in the recipe in any case to serve as a fallback in case the recipe is transient
        var templateItems = findGoodTemplateItems(recipe, menu);

        var recipeId = recipe.getId();
        // Don't transmit a recipe id to the server in case the recipe is not actually resolvable
        // this is the case for recipes synthetically generated for JEI
        if (menu.getPlayer().level.getRecipeManager().byKey(recipe.getId()).isEmpty()) {
            AELog.debug("Cannot send recipe id %s to server because it's transient", recipeId);
            recipeId = null;
        }

        NetworkHandler.instance().sendToServer(new FillCraftingGridFromRecipePacket(recipeId, templateItems));
    }

    private NonNullList<ItemStack> findGoodTemplateItems(Recipe<?> recipe, MEStorageMenu menu) {
        var ingredientPriorities = getIngredientPriorities(menu, ENTRY_COMPARATOR);

        var templateItems = NonNullList.withSize(9, ItemStack.EMPTY);
        var ingredients = CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe);
        for (int i = 0; i < ingredients.size(); i++) {
            var ingredient = ingredients.get(i);
            if (!ingredient.isEmpty()) {
                // Try to find the best item. In case the ingredient is a tag, it might contain versions the
                // player doesn't actually have
                var stack = ingredientPriorities.entrySet()
                        .stream()
                        .filter(e -> e.getKey() instanceof AEItemKey itemKey && ingredient.test(itemKey.toStack()))
                        .max(Comparator.comparingInt(Map.Entry::getValue))
                        .map(e -> ((AEItemKey) e.getKey()).toStack())
                        .orElse(ingredient.getItems()[0]);

                templateItems.set(i, stack);
            }
        }
        return templateItems;
    }

    private Map<Integer, Ingredient> getGuiSlotToIngredientMap(Recipe<?> recipe) {
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
    @SuppressWarnings("UnstableApiUsage")
    public TransferHandlerRenderer getMissingRenderer(Set<Integer> indices) {
        return (matrices, mouseX, mouseY, delta, widgets, bounds, display) -> {
            int i = 0;
            for (Widget widget : widgets) {
                if (widget instanceof Slot && ((Slot) widget).getNoticeMark() == Slot.INPUT) {
                    if (indices.contains(i++)) {
                        matrices.pushPose();
                        matrices.translate(0, 0, 400);
                        Rectangle innerBounds = ((Slot) widget).getInnerBounds();
                        GuiComponent.fill(matrices, innerBounds.x, innerBounds.y, innerBounds.getMaxX(),
                                innerBounds.getMaxY(), 0x40ff0000);
                        matrices.popPose();
                    }
                }
            }
        };
    }
}
