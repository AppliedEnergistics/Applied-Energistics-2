package appeng.integration.modules.itemlists;

import appeng.api.stacks.AEItemKey;
import appeng.core.network.ServerboundPacket;
import appeng.core.network.serverbound.FillCraftingGridFromRecipePacket;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.util.CraftingRecipeUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;

public final class CraftingHelper {
    private static final Comparator<GridInventoryEntry> ENTRY_COMPARATOR = Comparator
            .comparing(GridInventoryEntry::getStoredAmount);

    private CraftingHelper() {
    }

    public static void performTransfer(CraftingTermMenu menu,
                                       @Nullable ResourceKey<Recipe<?>> recipeId,
                                       Recipe<?> recipe,
                                       boolean craftMissing) {

        // We send the items in the recipe in any case to serve as a fallback in case the recipe is transient
        var templateItems = findGoodTemplateItems(recipe, menu);

        ServerboundPacket message = new FillCraftingGridFromRecipePacket(recipeId, templateItems, craftMissing);
        PacketDistributor.sendToServer(message);
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
                        .filter(e -> e.getKey() instanceof AEItemKey itemKey && itemKey.matches(ingredient))
                        .max(Comparator.comparingInt(Map.Entry::getValue))
                        .map(e -> ((AEItemKey) e.getKey()).toStack())
                        // TODO 1.21.4 Can't stay like this
                        .orElse(ingredient.items().map(Holder::value).map(Item::getDefaultInstance).findFirst().orElse(ItemStack.EMPTY));

                templateItems.set(i, stack);
            }
        }
        return templateItems;
    }

}
