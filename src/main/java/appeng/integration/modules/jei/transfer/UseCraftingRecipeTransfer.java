package appeng.integration.modules.jei.transfer;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.FillCraftingGridFromRecipePacket;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.util.CraftingRecipeUtil;

/**
 * Recipe transfer implementation with the intended purpose of actually crafting an item. Most of the work is done
 * server-side because permission-checks and inventory extraction cannot be done client-side.
 */
public class UseCraftingRecipeTransfer<T extends CraftingTermMenu> extends AbstractTransferHandler<T> {
    public UseCraftingRecipeTransfer(Class<T> containerClass) {
        super(containerClass);
    }

    @Override
    protected boolean isOnlyCraftingSupported() {
        return true;
    }

    protected void performTransfer(T menu,
            @Nullable Recipe<?> recipe,
            List<List<GenericStack>> genericIngredients,
            List<GenericStack> genericResults, boolean forCraftingTable) {

        // We send the items shown by REI in any case to serve as a fallback in case the recipe is unresolvable
        var flatIngredients = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int i = 0; i < genericIngredients.size(); i++) {
            var inputIngredient = genericIngredients.get(i);
            if (inputIngredient.isEmpty()) {
                continue;
            }
            if (i < flatIngredients.size()) {
                // Just use the first *item* in the list (it might alternate with fluids)
                for (var entryStack : inputIngredient) {
                    if (entryStack.what() instanceof AEItemKey itemKey) {
                        flatIngredients.set(i, itemKey.toStack());
                        break;
                    }
                }
            }
        }

        var recipeId = recipe != null ? recipe.getId() : null;

        NetworkHandler.instance().sendToServer(new FillCraftingGridFromRecipePacket(recipeId, flatIngredients));
    }

    @Override
    protected Set<Integer> findMissingSlots(T menu,
            @Nullable Recipe<?> recipe,
            List<List<GenericStack>> genericIngredients) {

        var slots = new HashMap<Integer, Ingredient>(9);

        // Prefer checking recipe ingredients if available since we'll also prefer searching by ingredient if possible
        if (recipe != null) {
            var recipeIngredients = CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe);
            for (int i = 0; i < recipeIngredients.size(); i++) {
                var ingredient = recipeIngredients.get(i);
                if (!ingredient.isEmpty()) {
                    slots.put(i, ingredient);
                }
            }
        } else {
            // Fall back to searching for direct matches of REI stacks
            for (int i = 0; i < genericIngredients.size(); i++) {
                var stacks = genericIngredients.get(i);
                var ingredient = Ingredient.of(stacks.stream()
                        .filter(stack -> stack.what() instanceof AEItemKey)
                        // Note that since we're trying to craft something, we'll use stack size 1
                        .map(stack -> ((AEItemKey) stack.what()).toStack()));

                if (!ingredient.isEmpty()) {
                    slots.put(i, ingredient);
                }
            }
        }

        return menu.findMissingIngredients(slots);
    }
}
