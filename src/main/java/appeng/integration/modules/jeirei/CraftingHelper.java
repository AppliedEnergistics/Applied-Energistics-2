package appeng.integration.modules.jeirei;

import java.util.Comparator;
import java.util.Map;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import appeng.api.stacks.AEItemKey;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.FillCraftingGridFromRecipePacket;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.util.CraftingRecipeUtil;

public final class CraftingHelper {
    private static final Comparator<GridInventoryEntry> ENTRY_COMPARATOR = Comparator
            .comparing(GridInventoryEntry::getStoredAmount);

    private CraftingHelper() {
    }

    public static void performTransfer(CraftingTermMenu menu, Recipe<?> recipe, boolean craftMissing) {

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

    private static NonNullList<ItemStack> findGoodTemplateItems(Recipe<?> recipe, MEStorageMenu menu) {
        var ingredientPriorities = EncodingHelper.getIngredientPriorities(menu, ENTRY_COMPARATOR);

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

}
