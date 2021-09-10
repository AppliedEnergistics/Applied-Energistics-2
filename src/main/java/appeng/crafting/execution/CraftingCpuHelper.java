package appeng.crafting.execution;

import java.util.*;

import javax.annotation.Nullable;

import appeng.api.networking.crafting.IPatternDetails;
import appeng.api.storage.data.IItemList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
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

    public static boolean extractPatternPower(
            IPatternDetails details,
            IEnergyService energyService,
            Actionable type) {
        // Consume power.
        double sum = 0;

        for (var anInput : details.getInputs()) {
            if (anInput != null) {
                sum += anInput.getMultiplier();
            }
        }

        return energyService.extractAEPower(sum, type, PowerMultiplier.CONFIG) >= sum - 0.01;
    }

    @Nullable
    public static IItemList<IAEItemStack>[] extractPatternInputs(
            IPatternDetails details,
            ICraftingInventory<IAEItemStack> sourceInv,
            IEnergyService energyService,
            Level level) {
        // Check energy first.
        if (!extractPatternPower(details, energyService, Actionable.SIMULATE)) return null;

        // Extract inputs into the container.
        var inputs = details.getInputs();
        @SuppressWarnings("unchecked")
        IItemList<IAEItemStack>[] inputHolder = new IItemList[inputs.length];
        boolean found = true;

        for (int x = 0; x < inputs.length; x++) {
            IItemList<IAEItemStack> list = inputHolder[x] = StorageChannels.items().createList();
            long remainingMultiplier = inputs[x].getMultiplier();
            for (IAEItemStack template : getValidItemTemplates(sourceInv, inputs[x], level)) {
                long extracted = extractTemplates(sourceInv, template, remainingMultiplier);
                list.add(template.copyWithStackSize(template.getStackSize() * extracted));
                remainingMultiplier -= extracted;
                if (remainingMultiplier == 0) break;
            }

            if (remainingMultiplier > 0) {
                found = false;
                break;
            }
        }

        // Failed to extract everything, put it back!
        if (!found) {
            // put stuff back..
            reinjectPatternInputs(sourceInv, inputHolder);
            return null;
        }

        return inputHolder;
    }

    public static void reinjectPatternInputs(ICraftingInventory<IAEItemStack> sourceInv,
            IItemList<IAEItemStack>[] inputHolder) {
        for (var list : inputHolder) {
            for (IAEItemStack stack : list) {
                sourceInv.injectItems(stack, Actionable.MODULATE);
            }
        }
    }

    public static Iterable<IAEItemStack> getExpectedOutputs(IPatternDetails details) {
        var outputs = Arrays.asList(details.getOutputs());

        // TODO: fire event?
        // TODO: add back container items.
        /*if (details.isCraftable()) {
            CraftingEvent.fireAutoCraftingEvent(level, details, craftingContainer);

            for (int x = 0; x < craftingContainer.getContainerSize(); x++) {
                final ItemStack output = Platform.getContainerItem(craftingContainer.getItem(x));
                if (!output.isEmpty()) {
                    outputs.add(AEItemStack.fromItemStack(output));
                }
            }
        }*/

        return outputs;
    }

    /**
     * Get all stack templates that can be used for this pattern's input.
     */
    public static Iterable<IAEItemStack> getValidItemTemplates(ICraftingInventory<IAEItemStack> inv, IPatternDetails.IInput input, Level level) {
        IAEItemStack[] possibleInputs = input.getPossibleInputs();
        List<IAEItemStack> substitutes;
        if (input.allowFuzzyMatch()) {
            substitutes = new ArrayList<>(possibleInputs.length);

            for (IAEItemStack stack : possibleInputs) {
                for (IAEItemStack fuzz : inv.findFuzzyTemplates(stack)) {
                    // Set the correct amount, it has to match that of the template!
                    substitutes.add(fuzz.copyWithStackSize(stack.getStackSize()));
                }
            }
        } else {
            substitutes = Arrays.asList(possibleInputs);
        }

        return Iterables.filter(substitutes, stack -> input.isValid(stack, level));
    }

    /**
     * Extract a whole number of templates, and return how many were extracted.
     */
    public static long extractTemplates(ICraftingInventory<IAEItemStack> inv, IAEItemStack template, long multiplier) {
        long maxTotal = template.getStackSize() * multiplier;
        // Extract as much as possible.
        IAEItemStack extracted = inv.extractItems(template.copyWithStackSize(maxTotal), Actionable.SIMULATE);
        if (extracted == null) return 0;
        // Adjust to have a whole number of templates.
        multiplier = extracted.getStackSize() / template.getStackSize();
        maxTotal = template.getStackSize() * multiplier;
        if (maxTotal == 0) return 0;
        extracted = inv.extractItems(template.copyWithStackSize(maxTotal), Actionable.MODULATE);
        if (extracted == null || extracted.getStackSize() != maxTotal) {
            throw new IllegalStateException("Failed to correctly extract whole number. Invalid simulation!");
        }
        return multiplier;
    }

    private CraftingCpuHelper() {
    }
}
