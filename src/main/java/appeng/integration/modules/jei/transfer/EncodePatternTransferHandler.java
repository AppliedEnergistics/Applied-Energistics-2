package appeng.integration.modules.jei.transfer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.math.LongMath;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.OptionalFakeSlot;
import appeng.parts.encoding.EncodingMode;
import appeng.util.CraftingRecipeUtil;

/**
 * Handles encoding patterns in the {@link PatternEncodingTermMenu} by clicking the + button on recipes shown in REI (or
 * JEI).
 */
public class EncodePatternTransferHandler<T extends PatternEncodingTermMenu> extends AbstractTransferHandler<T> {

    /**
     * Order of priority: - Craftable Items - Undamaged Items - Items the player has the most of
     */
    private static final Comparator<GridInventoryEntry> ENTRY_COMPARATOR = Comparator
            .comparing(GridInventoryEntry::isCraftable)
            .thenComparing(EncodePatternTransferHandler::isUndamaged)
            .thenComparing(GridInventoryEntry::getStoredAmount);

    private static Boolean isUndamaged(GridInventoryEntry entry) {
        return !(entry.getWhat() instanceof AEItemKey itemKey) || !itemKey.isDamaged();
    }

    public EncodePatternTransferHandler(Class<T> containerClass) {
        super(containerClass);
    }

    @Override
    protected boolean isOnlyCraftingSupported() {
        return false;
    }

    @Override
    protected void performTransfer(T menu,
            @Nullable Recipe<?> recipe,
            List<List<GenericStack>> genericIngredients,
            List<GenericStack> genericResults,
            boolean forCraftingTable) {
        var suitableMode = forCraftingTable ? EncodingMode.CRAFTING : EncodingMode.PROCESSING;
        menu.setMode(suitableMode);

        if (forCraftingTable) {
            encodeCraftingRecipe(menu, recipe, genericIngredients);
        } else {
            encodeProcessingRecipe(menu, genericIngredients, genericResults);
        }

    }

    private void encodeProcessingRecipe(T menu, List<List<GenericStack>> genericIngredients,
            List<GenericStack> genericResults) {
        // Note that this runs on the client and getClientRepo() is guaranteed to be available there.
        var ingredientPriorities = getIngredientPriorities(menu);

        encodeBestMatchingStacksIntoSlots(
                genericIngredients,
                ingredientPriorities,
                menu.getCraftingGridSlots());
        encodeBestMatchingStacksIntoSlots(
                // For the outputs, it's only one possible item per slot
                genericResults.stream().map(List::of).toList(),
                ingredientPriorities,
                menu.getProcessingOutputSlots());
    }

    private void encodeBestMatchingStacksIntoSlots(List<List<GenericStack>> possibleInputsBySlot,
            Map<AEKey, Integer> ingredientPriorities,
            FakeSlot[] slots) {
        var encodedInputs = new ArrayList<GenericStack>();
        for (var genericIngredient : possibleInputsBySlot) {
            if (!genericIngredient.isEmpty()) {
                addOrMerge(encodedInputs, findBestIngredient(ingredientPriorities, genericIngredient));
            }
        }

        for (int i = 0; i < slots.length; i++) {
            var slot = slots[i];
            var stack = (i < encodedInputs.size()) ? GenericStack.wrapInItemStack(encodedInputs.get(i))
                    : ItemStack.EMPTY;
            NetworkHandler.instance().sendToServer(new InventoryActionPacket(
                    InventoryAction.SET_FILTER, slot.index, stack));
        }
    }

    private void encodeCraftingRecipe(T menu,
            @Nullable Recipe<?> recipe,
            List<List<GenericStack>> genericIngredients) {
        // Note that this runs on the client and getClientRepo() is guaranteed to be available there.
        var prioritizedNetworkInv = getIngredientPriorities(menu);

        var encodedInputs = NonNullList.withSize(menu.getCraftingGridSlots().length, ItemStack.EMPTY);

        if (recipe != null) {
            // When we have access to a crafting recipe, we'll switch modes and try to find suitable
            // ingredients based on the recipe ingredients, which allows for fuzzy-matching.
            var ingredients3x3 = CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe);

            // Find a good match for every ingredient
            for (int slot = 0; slot < ingredients3x3.size(); slot++) {
                var ingredient = ingredients3x3.get(slot);
                if (ingredient.isEmpty()) {
                    continue; // Skip empty slots
                }

                // Due to how some crafting recipes work, the ingredient can match more than just one item in the
                // network inventory. We'll find all network inventory entries that it matches and sort them
                // according to their suitability for encoding a pattern
                var bestNetworkIngredient = prioritizedNetworkInv.entrySet().stream()
                        .filter(ni -> ni.getKey() instanceof AEItemKey itemKey && ingredient.test(itemKey.toStack()))
                        .max(Comparator.comparingInt(Map.Entry::getValue))
                        .map(entry -> entry.getKey() instanceof AEItemKey itemKey ? itemKey.toStack() : null);

                var bestIngredient = bestNetworkIngredient.orElse(ingredient.getItems()[0]);

                encodedInputs.set(slot, bestIngredient);
            }
        } else {
            for (int slot = 0; slot < genericIngredients.size(); slot++) {
                var genericIngredient = genericIngredients.get(slot);
                if (genericIngredient.isEmpty()) {
                    continue; // Skip empty slots
                }

                var bestIngredient = findBestIngredient(prioritizedNetworkInv, genericIngredient).what();

                // Clamp amounts to 1 in crafting table mode
                if (bestIngredient instanceof AEItemKey itemKey) {
                    encodedInputs.set(slot, itemKey.toStack());
                } else {
                    encodedInputs.set(slot, GenericStack.wrapInItemStack(bestIngredient, 1));
                }
            }
        }

        for (int i = 0; i < encodedInputs.size(); i++) {
            ItemStack encodedInput = encodedInputs.get(i);
            NetworkHandler.instance().sendToServer(new InventoryActionPacket(
                    InventoryAction.SET_FILTER, menu.getCraftingGridSlots()[i].index, encodedInput));
        }

        // Clear out the processing outputs
        for (OptionalFakeSlot outputSlot : menu.getProcessingOutputSlots()) {
            NetworkHandler.instance().sendToServer(new InventoryActionPacket(
                    InventoryAction.SET_FILTER, outputSlot.index, ItemStack.EMPTY));
        }

    }

    // Given a set of possible ingredients, find the one that has the highest priority
    private static GenericStack findBestIngredient(Map<AEKey, Integer> ingredientPriorities,
            List<GenericStack> possibleIngredients) {
        return possibleIngredients.stream()
                .map(gi -> Pair.of(gi, ingredientPriorities.getOrDefault(gi.what(), Integer.MIN_VALUE)))
                .max(Comparator.comparingInt(Pair::getRight))
                .map(Pair::getLeft)
                .orElseThrow();
    }

    /**
     * In processing mode it makes sense to merge stacks of the same type together.
     */
    private void addOrMerge(List<GenericStack> stacks, GenericStack newStack) {
        for (int i = 0; i < stacks.size(); i++) {
            var existingStack = stacks.get(i);
            if (Objects.equals(existingStack.what(), newStack.what())) {
                // Add the new amount onto the existing amount
                long newAmount = LongMath.saturatedAdd(existingStack.amount(), newStack.amount());
                stacks.set(i, new GenericStack(newStack.what(), newAmount));

                // Determine if the addition overflowed. If it did, add the remainder as a new stack.
                long overflow = newStack.amount() - (newAmount - existingStack.amount());
                if (overflow > 0) {
                    stacks.add(new GenericStack(newStack.what(), overflow));
                }
                return;
            }
        }

        stacks.add(newStack);
    }

    /**
     * Compute a map from all keys in the network inventory to their position when sorted by priority. Also takes the
     * player inventory into account for any items that are not already in the grid.
     * <p/>
     * Higher means higher priority.
     */
    private Map<AEKey, Integer> getIngredientPriorities(T menu) {
        var orderedEntries = menu.getClientRepo().getAllEntries()
                .stream()
                .sorted(ENTRY_COMPARATOR)
                .map(GridInventoryEntry::getWhat)
                .toList();

        var result = new HashMap<AEKey, Integer>(orderedEntries.size());
        for (int i = 0; i < orderedEntries.size(); i++) {
            result.put(orderedEntries.get(i), i);
        }

        // Also consider the player inventory, but only as the last resort
        for (var item : menu.getPlayerInventory().items) {
            var key = AEItemKey.of(item);
            if (key != null) {
                // Use -1 as lower priority than the lowest network entry (which start at 0)
                result.putIfAbsent(key, -1);
            }
        }

        return result;
    }
}
