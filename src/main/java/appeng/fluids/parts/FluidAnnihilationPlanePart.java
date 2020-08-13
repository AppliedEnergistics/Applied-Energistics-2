
package appeng.fluids.parts;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.core.sync.packets.BlockTransitionEffectPacket;
import appeng.fluids.util.AEFluidStack;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.parts.BasicStatePart;
import appeng.parts.automation.PlaneConnectionHelper;
import appeng.parts.automation.PlaneConnections;
import appeng.parts.automation.PlaneModelData;
import appeng.parts.automation.PlaneModels;
import appeng.util.Platform;

public class FluidAnnihilationPlanePart extends BasicStatePart implements IGridTickable {

    public static final ITag.INamedTag<Fluid> TAG_BLACKLIST = FluidTags
            .makeWrapperTag(AppEng.makeId("blacklisted/fluid_annihilation_plane").toString());

    private static final PlaneModels MODELS = new PlaneModels("part/fluid_annihilation_plane",
            "part/fluid_annihilation_plane_on");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private final IActionSource mySrc = new MachineSource(this);

    private final PlaneConnectionHelper connectionHelper = new PlaneConnectionHelper(this);

    public FluidAnnihilationPlanePart(final ItemStack is) {
        super(is);
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        connectionHelper.getBoxes(bch);
    }

    public PlaneConnections getConnections() {
        return connectionHelper.getConnections();
    }

    @Override
    public void onNeighborChanged(IBlockReader w, BlockPos pos, BlockPos neighbor) {
        if (pos.offset(this.getSide().getFacing()).equals(neighbor)) {
            this.refresh();
        } else {
            connectionHelper.updateConnections();
        }
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1;
    }

    private void refresh() {
        try {
            this.getProxy().getTick().alertDevice(this.getProxy().getNode());
        } catch (final GridAccessException e) {
            // :P
        }
    }

    @Override
    @MENetworkEventSubscribe
    public void chanRender(final MENetworkChannelsChanged c) {
        this.refresh();
        this.getHost().markForUpdate();
    }

    @Override
    @MENetworkEventSubscribe
    public void powerRender(final MENetworkPowerStatusChange c) {
        this.refresh();
        this.getHost().markForUpdate();
    }

    private TickRateModulation pickupFluid() {
        if (!this.getProxy().isActive()) {
            return TickRateModulation.SLEEP;
        }

        final TileEntity te = this.getTile();
        final World w = te.getWorld();
        final BlockPos pos = te.getPos().offset(this.getSide().getFacing());

        BlockState blockstate = w.getBlockState(pos);
        if (blockstate.getBlock() instanceof IBucketPickupHandler) {
            FluidState fluidState = blockstate.getFluidState();

            Fluid fluid = fluidState.getFluid();
            if (isFluidBlacklisted(fluid)) {
                return TickRateModulation.SLEEP;
            }

            if (fluid != Fluids.EMPTY && fluidState.isSource()) {
                // Attempt to store the fluid in the network
                final IAEFluidStack blockFluid = AEFluidStack
                        .fromFluidStack(new FluidStack(fluidState.getFluid(), FluidAttributes.BUCKET_VOLUME));
                if (this.storeFluid(blockFluid, false)) {
                    // If that would succeed, actually slurp up the liquid as if we were using a
                    // bucket
                    // This _MIGHT_ change the liquid, and if it does, and we dont have enough
                    // space, tough luck. you loose the source block.
                    fluid = ((IBucketPickupHandler) blockstate.getBlock()).pickupFluid(w, pos, blockstate);
                    this.storeFluid(AEFluidStack.fromFluidStack(new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME)),
                            true);

                    AppEng.proxy.sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 64, w,
                            new BlockTransitionEffectPacket(pos, blockstate, this.getSide().getOpposite(),
                                    BlockTransitionEffectPacket.SoundMode.FLUID));

                    return TickRateModulation.URGENT;
                }
                return TickRateModulation.IDLE;
            }
        }

        // nothing to do here :)
        return TickRateModulation.SLEEP;
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(TickRates.AnnihilationPlane.getMin(), TickRates.AnnihilationPlane.getMax(), false,
                true);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        return this.pickupFluid();
    }

    private boolean storeFluid(IAEFluidStack stack, boolean modulate) {
        try {
            final IStorageGrid storage = this.getProxy().getStorage();
            final IMEInventory<IAEFluidStack> inv = storage
                    .getInventory(Api.instance().storage().getStorageChannel(IFluidStorageChannel.class));

            if (modulate) {
                final IEnergyGrid energy = this.getProxy().getEnergy();
                return Platform.poweredInsert(energy, inv, stack, this.mySrc) == null;
            } else {
                final float requiredPower = stack.getStackSize() / Math.min(1.0f, stack.getChannel().transferFactor());
                final IEnergyGrid energy = this.getProxy().getEnergy();

                if (energy.extractAEPower(requiredPower, Actionable.SIMULATE, PowerMultiplier.CONFIG) < requiredPower) {
                    return false;
                }
                final IAEFluidStack leftOver = inv.injectItems(stack, Actionable.SIMULATE, this.mySrc);
                return leftOver == null || leftOver.getStackSize() == 0;
            }
        } catch (final GridAccessException e) {
            // :P
        }
        return false;
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new PlaneModelData(getConnections());
    }

    private boolean isFluidBlacklisted(Fluid fluid) {
        return TAG_BLACKLIST.contains(fluid);
    }

}
