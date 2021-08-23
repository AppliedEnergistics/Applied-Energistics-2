package appeng.crafting.execution;

import java.util.*;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.CraftingEvent;
import appeng.crafting.inv.ICraftingInventory;
import appeng.crafting.inv.ListCraftingInventory;
import appeng.menu.NullMenu;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

/**
 * Helper functions used by the CPU.
 */
public class CraftingCpuHelper {
    public static boolean tryExtractInitialItems(ICraftingPlan plan, IGrid grid,
            ListCraftingInventory<IAEItemStack> cpuInventory, IActionSource src) {
        IMEMonitor<IAEItemStack> networkMonitor = grid.getStorageService().getInventory(StorageChannels.items());

        for (IAEItemStack toExtract : plan.usedItems()) {
            IAEItemStack extracted = networkMonitor.extractItems(toExtract, Actionable.MODULATE, src);
            cpuInventory.injectItems(extracted, Actionable.MODULATE);

            if (extracted == null || extracted.getStackSize() < toExtract.getStackSize()) {
                // Failed to extract everything, reinject and hope for the best.
                // TODO: maybe voiding items that fail to re-insert is not the best thing to do?
                for (IAEItemStack stored : cpuInventory.list) {
                    networkMonitor.injectItems(stored, Actionable.MODULATE, src);
                    stored.reset();
                }

                return false;
            }
        }

        return true;
    }

    public static CompoundTag generateLinkData(final String craftingID, final boolean standalone, final boolean req) {
        final CompoundTag tag = new CompoundTag();

        tag.putString("CraftID", craftingID);
        tag.putBoolean("canceled", false);
        tag.putBoolean("done", false);
        tag.putBoolean("standalone", standalone);
        tag.putBoolean("req", req);

        return tag;
    }

    /**
     * Check if the passed crafting inventory has enough inputs for the passed pattern.
     * 
     * @return True if the inventory has all the required inputs, false otherwise.
     */
    public static boolean hasInputs(ICraftingInventory<IAEItemStack> inv, ICraftingPatternDetails details) {
        if (!details.isCraftable()) {
            // Processing patterns are relatively easy
            for (IAEItemStack input : details.getInputs()) {
                final IAEItemStack ais = inv.extractItems(input.copy(), Actionable.SIMULATE);

                if (ais == null || ais.getStackSize() < input.getStackSize()) {
                    return false;
                }
            }
        } else if (details.canSubstitute()) {
            // When substitutions are allowed, we have to keep track of which items we've reserved
            IAEItemStack[] sparseInputs = details.getSparseInputs();
            Map<IAEItemStack, Integer> consumedCount = new HashMap<>();
            for (int i = 0; i < sparseInputs.length; i++) {
                List<IAEItemStack> substitutes = details.getSubstituteInputs(i);
                if (substitutes.isEmpty()) {
                    continue;
                }

                boolean found = false;
                for (IAEItemStack substitute : substitutes) {
                    for (IAEItemStack fuzz : inv.findFuzzyTemplates(substitute)) {
                        int alreadyConsumed = consumedCount.getOrDefault(fuzz, 0);
                        if (fuzz.getStackSize() - alreadyConsumed <= 0) {
                            continue; // Already fully consumed by a previous slot of this recipe
                        }

                        fuzz = fuzz.copy();
                        fuzz.setStackSize(1); // We're iterating over SPARSE inputs which means there's 1 of each needed
                        final IAEItemStack ais = inv.extractItems(fuzz, Actionable.SIMULATE);

                        if (ais != null && ais.getStackSize() > 0) {
                            // Mark 1 of the stack as consumed
                            consumedCount.merge(fuzz, 1, Integer::sum);
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }

                if (!found) {
                    return false;
                }
            }

        } else {
            // When no substitutions can occur, we can simply check that all items are accounted since
            // each type of item should only occur once
            for (IAEItemStack g : details.getInputs()) {
                boolean found = false;

                for (IAEItemStack fuzz : inv.findFuzzyTemplates(g)) {
                    fuzz = fuzz.copy();
                    fuzz.setStackSize(g.getStackSize());
                    final IAEItemStack ais = inv.extractItems(fuzz, Actionable.SIMULATE);

                    if (ais != null && ais.getStackSize() >= g.getStackSize()) {
                        found = true;
                        break;
                    } else if (ais != null) {
                        g = g.copy();
                        g.decStackSize(ais.getStackSize());
                    }
                }

                if (!found) {
                    return false;
                }
            }

        }

        return true;
    }

    @Nullable
    public static CraftingContainer extractPatternInputs(
            ICraftingPatternDetails details,
            ICraftingInventory<IAEItemStack> sourceInv,
            IEnergyService energyService,
            Level level) {
        final IAEItemStack[] input = details.getSparseInputs();
        // Consume power.
        double sum = 0;

        for (final IAEItemStack anInput : input) {
            if (anInput != null) {
                sum += anInput.getStackSize();
            }
        }

        if (energyService.extractAEPower(sum, Actionable.MODULATE, PowerMultiplier.CONFIG) < sum - 0.01) {
            // Not enough power.
            return null;
        }

        // Extract inputs into the container.
        CraftingContainer craftingContainer = new CraftingContainer(new NullMenu(), 3, 3);
        boolean found = false;

        for (int x = 0; x < input.length; x++) {
            if (input[x] != null) {
                found = false;

                if (details.isCraftable()) {
                    final Collection<IAEItemStack> itemList;

                    if (details.canSubstitute()) {
                        final List<IAEItemStack> substitutes = details.getSubstituteInputs(x);
                        itemList = new ArrayList<>(substitutes.size());

                        for (IAEItemStack stack : substitutes) {
                            itemList.addAll(sourceInv.findFuzzyTemplates(stack));
                        }
                    } else {
                        itemList = List.of(input[x]);
                    }

                    for (IAEItemStack fuzz : itemList) {
                        fuzz = fuzz.copy();
                        fuzz.setStackSize(input[x].getStackSize());

                        if (details.isValidItemForSlot(x, fuzz.createItemStack(), level)) {
                            final IAEItemStack ais = sourceInv.extractItems(fuzz, Actionable.MODULATE);
                            final ItemStack is = ais == null ? ItemStack.EMPTY : ais.createItemStack();

                            if (!is.isEmpty()) {
                                craftingContainer.setItem(x, is);
                                found = true;
                                break;
                            }
                        }
                    }
                } else {
                    final IAEItemStack ais = sourceInv.extractItems(input[x].copy(), Actionable.MODULATE);
                    final ItemStack is = ais == null ? ItemStack.EMPTY : ais.createItemStack();

                    if (!is.isEmpty()) {
                        craftingContainer.setItem(x, is);
                        if (is.getCount() == input[x].getStackSize()) {
                            found = true;
                            continue;
                        }
                    }
                }

                if (!found) {
                    break;
                }
            }
        }

        // Failed to extract everything, put it back!
        if (!found) {
            // put stuff back..
            reinjectPatternInputs(sourceInv, craftingContainer);
            return null;
        }

        return craftingContainer;
    }

    public static void reinjectPatternInputs(ICraftingInventory<IAEItemStack> sourceInv,
            CraftingContainer craftingContainer) {
        for (int x = 0; x < craftingContainer.getContainerSize(); x++) {
            final ItemStack is = craftingContainer.getItem(x);
            if (!is.isEmpty()) {
                sourceInv.injectItems(AEItemStack.fromItemStack(is), Actionable.MODULATE);
            }
        }
    }

    public static Iterable<IAEItemStack> getExpectedOutputs(ICraftingPatternDetails details,
            CraftingContainer craftingContainer, Level level) {
        var outputs = new ArrayList<>(details.getOutputs());

        if (details.isCraftable()) {
            CraftingEvent.fireAutoCraftingEvent(level, details, craftingContainer);

            for (int x = 0; x < craftingContainer.getContainerSize(); x++) {
                final ItemStack output = Platform.getContainerItem(craftingContainer.getItem(x));
                if (!output.isEmpty()) {
                    outputs.add(AEItemStack.fromItemStack(output));
                }
            }
        }

        return outputs;
    }

    private CraftingCpuHelper() {
    }
}
