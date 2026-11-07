package appeng.integration.modules.emi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeManager;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.runtime.EmiFavorite;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import appeng.api.stacks.GenericStack;
import appeng.menu.me.common.IClientRepo;

/**
 * Keeps the craftables list EMI derives from a {@link NetworkEmiPlayerInventory} up to date incrementally.
 * <p>
 * {@link EmiPlayerInventory#getCraftables()} rebuilds the list from scratch: one recipe lookup per inventory key, a
 * craftability check and an {@link EmiFavorite} allocation per candidate, then a sort. EMI calls it whenever the
 * inventory it was given differs from the previous one, which with a whole network is on every network update, up to 20
 * times per second with the craftables sidebar showing.
 * <p>
 * Only what can have changed is redone: the repository reports which keys changed, so just the recipes using them are
 * re-checked and the sorted list is patched instead of re-sorted. A changed player inventory, another repository, a
 * repository that can't report its changes and an EMI recipe reload force a full rebuild.
 * <p>
 * It reproduces EMI's filtering and ordering instead of calling it, so it depends on {@link EmiFavorite} and
 * {@link EmiStackList}; a mismatch with the compiled against EMI version shows up as a wrong order or a
 * {@code NoSuchMethodError}, not as wrong recipes.
 */
final class CraftablesCache {
    /**
     * EMI's order: index of the output stack, then amount.
     */
    private static final Comparator<EmiIngredient> EMI_ORDER = (a, b) -> {
        int byIndex = Integer.compare(EmiStackList.getIndex(outputOf(a)), EmiStackList.getIndex(outputOf(b)));
        return byIndex != 0 ? byIndex : Long.compare(a.getAmount(), b.getAmount());
    };

    private static EmiIngredient outputOf(EmiIngredient ingredient) {
        return ingredient instanceof EmiFavorite favorite ? favorite.getStack() : ingredient;
    }

    private @Nullable EmiRecipeManager recipeManager;
    private @Nullable IClientRepo repo;
    private long lastRevision = Long.MIN_VALUE;
    private int lastSlotFingerprint;
    private final Object2LongMap<EmiStack> lastSlotAmounts = new Object2LongOpenHashMap<>();
    private final Set<EmiRecipe> candidates = new HashSet<>();
    private final Map<EmiRecipe, EmiFavorite.Craftable> craftable = new HashMap<>();
    private @Nullable List<EmiIngredient> sorted;

    List<EmiIngredient> getCraftables(NetworkEmiPlayerInventory inventory) {
        var manager = EmiApi.getRecipeManager();
        var repo = inventory.getRepo();
        if (manager != recipeManager || repo != this.repo) {
            // Everything derived from other recipes or another network is stale
            reset(manager, repo);
        }

        if (sorted != null
                && inventory.getRevision() == lastRevision
                && inventory.getSlotFingerprint() == lastSlotFingerprint) {
            return sorted;
        }

        try {
            return rebuild(inventory, manager);
        } catch (RuntimeException | Error e) {
            // The repository's changed keys are consumed before the list is patched, so without a reset a failure would
            // leave a half-updated list behind that the next call would keep patching.
            reset(manager, repo);
            throw e;
        }
    }

    private List<EmiIngredient> rebuild(NetworkEmiPlayerInventory inventory, EmiRecipeManager manager) {
        var predicate = inventory.getPredicate();
        if (predicate == null) {
            // Same check EMI makes first, so nothing is craftable
            clear();
            return List.of();
        }

        // Slot contents are part of this inventory but not of the repository, and there are only a few dozen
        var slotAmounts = new Object2LongOpenHashMap<EmiStack>();
        for (var stack : inventory.getSlotStacks()) {
            slotAmounts.addTo(stack, stack.getAmount());
        }

        // Recipes no handler supports can't be craftable and are most of a pack's candidates, so skip them before the
        // metadata calls. They stay in the candidate set, which has to match EMI's for the tie order to match.
        var supported = supportedRecipes();

        var changedFromRepo = repo != null ? repo.drainChangedKeys() : null;

        Set<EmiStack> changedStacks = null;
        var fullRebuild = sorted == null || changedFromRepo == null;
        if (!fullRebuild) {
            changedStacks = new HashSet<>();
            for (var key : changedFromRepo) {
                if (key == null) {
                    continue;
                }
                var stack = EmiStackHelper.toEmiStack(new GenericStack(key, 1));
                if (stack != null) {
                    // Can't have been in the inventory
                    changedStacks.add(stack);
                }
            }

            for (var entry : slotAmounts.object2LongEntrySet()) {
                if (lastSlotAmounts.getLong(entry.getKey()) != entry.getLongValue()
                        || !lastSlotAmounts.containsKey(entry.getKey())) {
                    changedStacks.add(entry.getKey());
                }
            }
            for (var stack : lastSlotAmounts.keySet()) {
                if (!slotAmounts.containsKey(stack)) {
                    // A slot item was taken out or replaced
                    changedStacks.add(stack);
                }
            }
        }

        lastSlotAmounts.clear();
        lastSlotAmounts.putAll(slotAmounts);

        Set<EmiRecipe> recheck = null;
        if (!fullRebuild) {
            recheck = new HashSet<>();
            for (var stack : changedStacks) {
                // Also covers a key that left the network: its recipes can still be looked up
                var recipes = manager.getRecipesByInput(stack);
                recheck.addAll(recipes);
                candidates.addAll(recipes);
            }
        }

        if (fullRebuild) {
            candidates.clear();
            for (var stack : inventory.inventory.keySet()) {
                candidates.addAll(manager.getRecipesByInput(stack));
            }
            // These removals aren't in `removed`, so the list has to be rebuilt instead of patched
            craftable.keySet().retainAll(candidates);
            sorted = null;
            recheck = candidates;
        }

        var added = new ArrayList<EmiFavorite.Craftable>();
        var removed = new ArrayList<EmiFavorite.Craftable>();
        for (var recipe : recheck) {
            if (supported != null && !supported.test(recipe)) {
                continue;
            }
            var isCraftable = !recipe.hideCraftable()
                    && !recipe.getOutputs().isEmpty()
                    && predicate.test(recipe);
            if (isCraftable) {
                var favorite = craftable.get(recipe);
                if (favorite == null) {
                    favorite = new EmiFavorite.Craftable(recipe);
                    craftable.put(recipe, favorite);
                    added.add(favorite);
                }
            } else {
                var favorite = craftable.remove(recipe);
                if (favorite != null) {
                    removed.add(favorite);
                }
            }
        }

        if (sorted == null) {
            // Iterate candidates so ties keep EMI's order
            var list = new ArrayList<EmiIngredient>(craftable.size());
            for (var recipe : candidates) {
                var favorite = craftable.get(recipe);
                if (favorite != null) {
                    list.add(favorite);
                }
            }
            list.sort(EMI_ORDER);
            sorted = list;
        } else if (!added.isEmpty() || !removed.isEmpty()) {
            // Re-sorting a few thousand entries costs ~20 ms, so patch the sorted list
            var list = new ArrayList<EmiIngredient>(sorted);
            for (var favorite : removed) {
                list.remove(favorite);
            }
            for (var favorite : added) {
                var index = Collections.binarySearch(list, favorite, EMI_ORDER);
                list.add(index < 0 ? -index - 1 : index, favorite);
            }
            sorted = list;
        }

        lastRevision = inventory.getRevision();
        lastSlotFingerprint = inventory.getSlotFingerprint();
        return sorted;
    }

    /**
     * The handlers EMI's {@link EmiPlayerInventory#getPredicate()} uses, so filtering by them can't change the result.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static @Nullable Predicate<EmiRecipe> supportedRecipes() {
        var screen = EmiApi.getHandledScreen();
        if (screen == null) {
            return null;
        }
        List<EmiRecipeHandler> handlers = EmiRecipeFiller.getAllHandlers((AbstractContainerScreen) screen);
        if (handlers.isEmpty()) {
            return null;
        }
        return recipe -> {
            for (var handler : handlers) {
                if (handler.supportsRecipe(recipe)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Drops everything derived from the current screen or network.
     */
    void clear() {
        reset(null, null);
    }

    private void reset(@Nullable EmiRecipeManager manager, @Nullable IClientRepo repo) {
        recipeManager = manager;
        this.repo = repo;
        lastRevision = Long.MIN_VALUE;
        lastSlotFingerprint = 0;
        lastSlotAmounts.clear();
        candidates.clear();
        craftable.clear();
        sorted = null;
    }
}
