package appeng.integration.modules.jei.transfer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRenderer;
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
                var result = Result.createSuccessful()
                        .color(missingSlots.anyMissing() ? 0x80FFA500 : 0x804545FF)
                        .renderer(createErrorRenderer(missingSlots));

                // Redo this once REI allows appending below the tooltip instead of overriding it...
                List<Component> extraTooltip = new ArrayList<>();
                if (missingSlots.anyCraftable()) {
                    if (craftMissing) {
                        extraTooltip.add(ItemModText.WILL_CRAFT.text().withStyle(ChatFormatting.BLUE));
                    } else {
                        extraTooltip.add(ItemModText.CTRL_CLICK_TO_CRAFT.text().withStyle(ChatFormatting.BLUE));
                    }
                }
                if (missingSlots.anyMissing()) {
                    extraTooltip.add(ItemModText.MISSING_ITEMS.text().withStyle(ChatFormatting.RED));
                }
                if (!extraTooltip.isEmpty()) {
                    result.overrideTooltipRenderer((point, sink) -> sink.accept(Tooltip.create(extraTooltip)));
                }

                return result;
            }
        } else {
            performTransfer(menu, recipe, craftMissing);
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

        return new ShapedRecipe(AppEng.makeId("__fake_recipe"), "", CRAFTING_GRID_WIDTH, CRAFTING_GRID_HEIGHT,
                ingredients, ItemStack.EMPTY);
    }

    protected void performTransfer(T menu, Recipe<?> recipe, boolean craftMissing) {

        // We send the items in the recipe in any case to serve as a fallback in case the recipe is transient
        var templateItems = findGoodTemplateItems(recipe, menu);

        var recipeId = recipe.getId();
        // Don't transmit a recipe id to the server in case the recipe is not actually resolvable
        // this is the case for recipes synthetically generated for JEI
        if (menu.getPlayer().level.getRecipeManager().byKey(recipe.getId()).isEmpty()) {
            AELog.debug("Cannot send recipe id %s to server because it's transient", recipeId);
            recipeId = null;
        }

        NetworkHandler.instance()
                .sendToServer(new FillCraftingGridFromRecipePacket(recipeId, templateItems, craftMissing));
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
    private static TransferHandlerRenderer createErrorRenderer(CraftingTermMenu.MissingIngredientSlots indices) {
        return (matrices, mouseX, mouseY, delta, widgets, bounds, display) -> {
            int i = 0;
            for (Widget widget : widgets) {
                if (widget instanceof Slot && ((Slot) widget).getNoticeMark() == Slot.INPUT) {
                    boolean missing = indices.missingSlots().contains(i);
                    boolean craftable = indices.craftableSlots().contains(i);
                    i++;
                    if (missing || craftable) {
                        matrices.pushPose();
                        matrices.translate(0, 0, 400);
                        Rectangle innerBounds = ((Slot) widget).getInnerBounds();
                        GuiComponent.fill(matrices, innerBounds.x, innerBounds.y, innerBounds.getMaxX(),
                                innerBounds.getMaxY(), missing ? 0x40ff0000 : 0x400000ff);
                        matrices.popPose();
                    }
                }
            }
        };
    }
}
