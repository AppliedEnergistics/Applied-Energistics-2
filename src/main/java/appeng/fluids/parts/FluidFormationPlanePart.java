
package appeng.fluids.parts;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import appeng.core.Api;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Upgrades;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.core.Api;
import appeng.fluids.container.FluidFormationPlaneContainer;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.IAEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.storage.MEInventoryHandler;
import appeng.parts.automation.AbstractFormationPlanePart;
import appeng.parts.automation.PlaneModels;
import appeng.util.Platform;
import appeng.util.prioritylist.PrecisePriorityList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import javax.annotation.Nonnull;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FluidFormationPlanePart extends AbstractFormationPlanePart<IAEFluidStack> implements IAEFluidInventory {
    private static final PlaneModels MODELS = new PlaneModels("part/fluid_formation_plane",
            "part/fluid_formation_plane_on");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private final MEInventoryHandler<IAEFluidStack> myHandler = new MEInventoryHandler<>(this,
            Api.instance().storage().getStorageChannel(IFluidStorageChannel.class));
    private final AEFluidInventory config = new AEFluidInventory(this, 63);

    public FluidFormationPlanePart(final ItemStack is) {
        super(is);
        this.updateHandler();
    }

    @Override
    protected void updateHandler() {
        this.myHandler.setBaseAccess(AccessRestriction.WRITE);
        this.myHandler.setWhitelist(
                this.getInstalledUpgrades(Upgrades.INVERTER) > 0 ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST);
        this.myHandler.setPriority(this.getPriority());

        final IItemList<IAEFluidStack> priorityList = Api.instance().storage()
                .getStorageChannel(IFluidStorageChannel.class).createList();

        final int slotsToUse = 18 + this.getInstalledUpgrades(Upgrades.CAPACITY) * 9;
        for (int x = 0; x < this.config.getSlots() && x < slotsToUse; x++) {
            final IAEFluidStack is = this.config.getFluidInSlot(x);
            if (is != null) {
                priorityList.add(is);
            }
        }
        this.myHandler.setPartitionList(new PrecisePriorityList<>(priorityList));

        try {
            this.getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
        } catch (final GridAccessException e) {
            // :P
        }
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable type, IActionSource src) {
        if (this.blocked || input == null || input.getAmount().isLessThan(FluidAmount.BUCKET)) {
            // need a full bucket
            return input;
        }

        final BlockEntity te = this.getHost().getTile();
        final World w = te.getWorld();
        final AEPartLocation side = this.getSide();
        final BlockPos pos = te.getPos().offset(side.getFacing());

        FluidVolume volume = input.getFluidStack();
        FluidVolume remainder = fill(w, pos, volume, type == Actionable.MODULATE ? Simulation.ACTION : Simulation.SIMULATE);
        if (remainder.getAmount_F().isLessThan(volume.getAmount_F())) {
            // calculate the effective amount consumed
            // round UP here because we might otherwise duplicate fluids
            if (remainder.isEmpty()) {
                return null;
            } else {
                return AEFluidStack.fromFluidVolume(remainder, RoundingMode.DOWN);
            }
        } else {
            // Filling failed
            this.blocked = true;
            return input;
        }
    }

    // FIXME https://github.com/AlexIIL/LibBlockAttributes/pull/21
    /**
     * Attempts to fill the given block with a bucket's worth of fluid
     * @return The remainder of the given fluid volume after placing a bucket
     */
    public static FluidVolume fill(WorldAccess world, BlockPos pos, FluidVolume volume, Simulation simulation) {

        if (volume.getAmount_F().isLessThan(FluidAmount.BUCKET)) {
            return volume; // Need at least a buckets worth
        }

        Fluid fluid = volume.getRawFluid();
        if (fluid == null) {
            return volume; // Can't be placed if it doesn't have an associated vanilla fluid
        }

        // This code assumes that placing a fluid in the world will always consume a bucket's worth
        boolean success = false;

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (state.isAir()) {
            // The easiest case, probably
            if (simulation == Simulation.ACTION) {
                BlockState fluidStillState = fluid.getDefaultState().getBlockState();
                world.setBlockState(pos, fluidStillState, 3);
            }
            success = true;

        } else if (block instanceof FluidFillable) {
            // FluidFillable includes waterloggable blocks, but not cauldrons, etc.
            FluidFillable fillable = (FluidFillable) block;
            if (simulation == Simulation.SIMULATE) {
                success = fillable.canFillWithFluid(world, pos, state, fluid);
            } else {
                success = fillable.tryFillWithFluid(world, pos, state, fluid.getDefaultState());
            }
        } else if (block instanceof FluidBlock) {
            FluidState fluidState = world.getFluidState(pos);
            // Top up a non-still fluid block, but this consumes a full bucket regardless of the level
            if (!fluidState.isStill() && fluidState.getFluid() == fluid) {
                if (simulation == Simulation.ACTION) {
                    world.setBlockState(pos, fluid.getDefaultState().getBlockState(), 3);
                }
                success = true;
            }
        }

        if (success) {
            // Reduce by one bucket
            return volume.copy().withAmount(volume.getAmount_F().roundedSub(FluidAmount.ONE, RoundingMode.DOWN));
        } else {
            return volume;
        }
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
        if (inv == this.config) {
            this.updateHandler();
        }
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);
        this.config.readFromNBT(data, "config");
        this.updateHandler();
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);
        this.config.writeToNBT(data, "config");
    }

    @Override
    @MENetworkEventSubscribe
    public void powerRender(final MENetworkPowerStatusChange c) {
        this.stateChanged();
    }

    @MENetworkEventSubscribe
    public void updateChannels(final MENetworkChannelsChanged changedChannels) {
        this.stateChanged();
    }

    @Override
    public boolean onPartActivate(final PlayerEntity player, final Hand hand, final Vec3d pos) {
        if (Platform.isServer()) {
            ContainerOpener.openContainer(FluidFormationPlaneContainer.TYPE, player, ContainerLocator.forPart(this));
        }

        return true;
    }

    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return Api.instance().storage().getStorageChannel(IFluidStorageChannel.class);
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(final IStorageChannel channel) {
        if (this.getProxy().isActive()
                && channel == Api.instance().storage().getStorageChannel(IFluidStorageChannel.class)) {
            final List<IMEInventoryHandler> handler = new ArrayList<>(1);
            handler.add(this.myHandler);
            return handler;
        }
        return Collections.emptyList();
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Nonnull
    @Override
    public Object getModelData() {
        return getConnections();
    }

    public IAEFluidTank getConfig() {
        return this.config;
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return Api.instance().definitions().parts().fluidFormationnPlane().maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Override
    public ScreenHandlerType<?> getContainerType() {
        return FluidFormationPlaneContainer.TYPE;
    }
}
