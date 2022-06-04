package appeng.integration.modules.jei.transfer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.crafting.IShapedRecipe;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

import appeng.api.stacks.AEItemKey;
import appeng.core.AELog;
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
public class UseCraftingRecipeTransfer<T extends CraftingTermMenu>
        extends AbstractTransferHandler
        implements IRecipeTransferHandler<T, CraftingRecipe> {
    private static final Comparator<GridInventoryEntry> ENTRY_COMPARATOR = Comparator
            .comparing(GridInventoryEntry::getStoredAmount);
    private final Class<T> containerClass;
    private final IRecipeTransferHandlerHelper helper;

    public UseCraftingRecipeTransfer(Class<T> containerClass, IRecipeTransferHandlerHelper helper) {
        this.containerClass = containerClass;
        this.helper = helper;
    }

    @Override
    public IRecipeTransferError transferRecipe(T menu, CraftingRecipe recipe, IRecipeLayout display, Player player,
            boolean maxTransfer, boolean doTransfer) {
        if (recipe.getType() != RecipeType.CRAFTING) {
            return helper.createInternalError();
        }

        if (recipe.getIngredients().isEmpty()) {
            return helper.createUserErrorWithTooltip(ItemModText.INCOMPATIBLE_RECIPE.text());
        }

        if (!recipe.canCraftInDimensions(CRAFTING_GRID_WIDTH, CRAFTING_GRID_HEIGHT)) {
            return helper.createUserErrorWithTooltip(ItemModText.RECIPE_TOO_LARGE.text());
        }

        // Find missing ingredients and highlight the slots which have these
        if (!doTransfer) {
            var missingSlots = menu.findMissingIngredients(getGuiSlotToIngredientMap(recipe));
            if (!missingSlots.isEmpty()) {
                return new TransferWarning(
                        helper.createUserErrorForSlots(ItemModText.MISSING_ITEMS.text(), missingSlots));
            }
        } else {
            performTransfer(menu, recipe);
        }
        // No error
        return null;
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
        int width, height;
        if (recipe instanceof IShapedRecipe<?>shapedRecipe) {
            width = shapedRecipe.getRecipeWidth();
            height = shapedRecipe.getRecipeHeight();
        } else {
            if (ingredients.size() > 4) {
                width = height = 3;
            } else if (ingredients.size() > 1) {
                width = height = 2;
            } else {
                width = height = 1;
            }
        }

        var result = new HashMap<Integer, Ingredient>(ingredients.size());
        for (int i = 0; i < ingredients.size(); i++) {
            // JEI uses slot 0 for the output by default, shifting all input slots by 1
            var guiSlot = 1 + getCraftingIndex(i, width, height);
            var ingredient = ingredients.get(i);
            if (!ingredient.isEmpty()) {
                result.put(guiSlot, ingredient);
            }
        }
        return result;
    }

    private static int getCraftingIndex(int i, int width, int height) {
        int index;
        if (width == 1) {
            if (height == 3) {
                index = (i * 3) + 1;
            } else if (height == 2) {
                index = (i * 3) + 1;
            } else {
                index = 4;
            }
        } else if (height == 1) {
            index = i + 3;
        } else if (width == 2) {
            index = i;
            if (i > 1) {
                index++;
                if (i > 3) {
                    index++;
                }
            }
        } else if (height == 2) {
            index = i + 3;
        } else {
            index = i;
        }
        return index;
    }

    @Override
    public Class<T> getContainerClass() {
        return containerClass;
    }

    @Override
    public Class<CraftingRecipe> getRecipeClass() {
        return CraftingRecipe.class;
    }
}
