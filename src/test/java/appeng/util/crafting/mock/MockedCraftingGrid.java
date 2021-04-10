package appeng.util.crafting.mock;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.Future;

/**
 * Mocking a crafting grid to test the crafting jobs.
 * Only supports pattern queries and emitable items.
 * Use {@link #addPattern} and {@link #setEmitable}.
 */
public class MockedCraftingGrid implements ICraftingGrid {
    private final Map<IAEItemStack, ImmutableList<ICraftingPatternDetails>> craftableItems = new HashMap<>();
    private final Set<IAEItemStack> emitableItems = new HashSet<>();

    public void addPattern(ICraftingPatternDetails details) {
        for (IAEItemStack out : details.getOutputs()) {
            out = out.copy();
            // FIXME CRAFTING no clue why this is done in CraftingGridCache#updatePatterns
            out.reset();
            out.setCraftable(true);

            // Read existing
            ImmutableList<ICraftingPatternDetails> oldMethods = craftableItems.getOrDefault(out, ImmutableList.of());
            // Copy and add new pattern
            ImmutableList.Builder<ICraftingPatternDetails> newMethodsBuilder = new ImmutableList.Builder<>();
            newMethodsBuilder.addAll(oldMethods);
            newMethodsBuilder.add(details);
            // Save new collection
            craftableItems.put(out, newMethodsBuilder.build());
        }
    }

    public void setEmitable(IAEItemStack what) {
        this.emitableItems.add(what);
    }

    @Override
    public ImmutableCollection<ICraftingPatternDetails> getCraftingFor(IAEItemStack whatToCraft, ICraftingPatternDetails details, int slot, World world) {
        // copy-paste of the CraftingGridCache method
        final ImmutableList<ICraftingPatternDetails> res = this.craftableItems.get(whatToCraft);

        if (res == null) {
            if (details != null && details.isCraftable()) {
                for (final IAEItemStack ais : this.craftableItems.keySet()) {
                    if (ais.getItem() == whatToCraft.getItem()
                            && (!ais.getItem().isDamageable() || ais.getItemDamage() == whatToCraft.getItemDamage())) {
                        // TODO: check if OK
                        // TODO: this is slightly hacky, but fine as long as we only deal with
                        // itemstacks
                        if (details.isValidItemForSlot(slot, ais.asItemStackRepresentation(), world)) {
                            return this.craftableItems.get(ais);
                        }
                    }
                }
            }

            return ImmutableSet.of();
        }

        return res;
    }

    @Override
    public boolean canEmitFor(IAEItemStack what) {
        return emitableItems.contains(what);
    }

    // Everything below just throws an exception

    @Override
    public Future<ICraftingJob> beginCraftingJob(World world, IGrid grid, IActionSource actionSrc, IAEItemStack craftWhat, ICraftingCallback callback) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public ICraftingLink submitJob(ICraftingJob job, ICraftingRequester requestingMachine, ICraftingCPU target, boolean prioritizePower, IActionSource src) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public ImmutableSet<ICraftingCPU> getCpus() {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public boolean isRequesting(IAEItemStack what) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public long requesting(IAEItemStack what) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public void onUpdateTick() {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public void removeNode(IGridNode gridNode, IGridHost machine) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public void addNode(IGridNode gridNode, IGridHost machine) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public void onSplit(IGridStorage destinationStorage) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public void onJoin(IGridStorage sourceStorage) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public void populateGridStorage(IGridStorage destinationStorage) {
        throw new UnsupportedOperationException("mock");
    }
}
