package appeng.parts.automation;

import com.google.common.collect.ImmutableSet;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.SchedulingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.storage.data.AEKey;
import appeng.core.settings.TickRates;
import appeng.helpers.MultiCraftingTracker;
import appeng.parts.PartAdjacentApi;
import appeng.util.Platform;

/**
 * Generalized base class for export buses that move stacks from network storage to an adjacent block using a non-AE
 * API.
 */
public abstract class ExportBusPart<T extends AEKey, A> extends IOBusPart<T> implements ICraftingRequester {
    protected final PartAdjacentApi<A> adjacentExternalApi;
    private final MultiCraftingTracker craftingTracker = new MultiCraftingTracker(this, 9);
    private int nextSlot = 0;

    public ExportBusPart(TickRates tickRates, ItemStack is, BlockApiLookup<A, Direction> apiLookup) {
        super(tickRates, is);
        this.adjacentExternalApi = new PartAdjacentApi<>(this, apiLookup);
        getMainNode().addService(ICraftingRequester.class, this);

        this.getConfigManager().registerSetting(Settings.CRAFT_ONLY, YesNo.NO);
        this.getConfigManager().registerSetting(Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT);
    }

    @Override
    public void readFromNBT(final CompoundTag extra) {
        super.readFromNBT(extra);
        this.craftingTracker.readFromNBT(extra);
        this.nextSlot = extra.getInt("nextSlot");
    }

    @Override
    public void writeToNBT(final CompoundTag extra) {
        super.writeToNBT(extra);
        this.craftingTracker.writeToNBT(extra);
        extra.putInt("nextSlot", this.nextSlot);
    }

    protected final boolean requestCrafting(ICraftingService cg, int configSlot, T what, long amount) {
        return this.craftingTracker.handleCrafting(configSlot, what, amount,
                this.getBlockEntity().getLevel(), cg, this.source);
    }

    @Override
    public void jobStateChange(final ICraftingLink link) {
        this.craftingTracker.jobStateChange(link);
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.craftingTracker.getRequestedJobs();
    }

    protected int getStartingSlot(final SchedulingMode schedulingMode, final int x) {
        if (schedulingMode == SchedulingMode.RANDOM) {
            return Platform.getRandom().nextInt(this.availableSlots());
        }

        if (schedulingMode == SchedulingMode.ROUNDROBIN) {
            return (this.nextSlot + x) % this.availableSlots();
        }

        return x;
    }

    protected void updateSchedulingMode(final SchedulingMode schedulingMode, final int x) {
        if (schedulingMode == SchedulingMode.ROUNDROBIN) {
            this.nextSlot = (this.nextSlot + x) % this.availableSlots();
        }
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(4, 4, 12, 12, 12, 14);
        bch.addBox(5, 5, 14, 11, 11, 15);
        bch.addBox(6, 6, 15, 10, 10, 16);
        bch.addBox(6, 6, 11, 10, 10, 12);
    }
}
