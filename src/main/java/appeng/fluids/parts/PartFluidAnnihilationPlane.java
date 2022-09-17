package appeng.fluids.parts;


import appeng.api.AEApi;
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
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.core.sync.packets.PacketTransitionEffect;
import appeng.fluids.util.AEFluidStack;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.parts.PartBasicState;
import appeng.parts.automation.PlaneConnections;
import appeng.parts.automation.PlaneModels;
import appeng.util.Platform;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.List;


public class PartFluidAnnihilationPlane extends PartBasicState implements IGridTickable {
    private static final PlaneModels MODELS = new PlaneModels("part/fluid_annihilation_plane_", "part/fluid_annihilation_plane_on_");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private final IActionSource mySrc = new MachineSource(this);

    public PartFluidAnnihilationPlane(final ItemStack is) {
        super(is);
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        int minX = 1;
        int minY = 1;
        int maxX = 15;
        int maxY = 15;

        final IPartHost host = this.getHost();
        if (host != null) {
            final TileEntity te = host.getTile();

            final BlockPos pos = te.getPos();

            final EnumFacing e = bch.getWorldX();
            final EnumFacing u = bch.getWorldY();

            if (this.isAnnihilationPlane(te.getWorld().getTileEntity(pos.offset(e.getOpposite())), this.getSide())) {
                minX = 0;
            }

            if (this.isAnnihilationPlane(te.getWorld().getTileEntity(pos.offset(e)), this.getSide())) {
                maxX = 16;
            }

            if (this.isAnnihilationPlane(te.getWorld().getTileEntity(pos.offset(u.getOpposite())), this.getSide())) {
                minY = 0;
            }

            if (this.isAnnihilationPlane(te.getWorld().getTileEntity(pos.offset(e)), this.getSide())) {
                maxY = 16;
            }
        }

        bch.addBox(5, 5, 14, 11, 11, 15);
        bch.addBox(minX, minY, 15, maxX, maxY, 16);
    }

    public PlaneConnections getConnections() {

        final EnumFacing facingRight, facingUp;
        AEPartLocation location = this.getSide();
        switch (location) {
            case UP:
                facingRight = EnumFacing.EAST;
                facingUp = EnumFacing.NORTH;
                break;
            case DOWN:
                facingRight = EnumFacing.WEST;
                facingUp = EnumFacing.NORTH;
                break;
            case NORTH:
                facingRight = EnumFacing.WEST;
                facingUp = EnumFacing.UP;
                break;
            case SOUTH:
                facingRight = EnumFacing.EAST;
                facingUp = EnumFacing.UP;
                break;
            case WEST:
                facingRight = EnumFacing.SOUTH;
                facingUp = EnumFacing.UP;
                break;
            case EAST:
                facingRight = EnumFacing.NORTH;
                facingUp = EnumFacing.UP;
                break;
            default:
            case INTERNAL:
                return PlaneConnections.of(false, false, false, false);
        }

        boolean left = false, right = false, down = false, up = false;

        final IPartHost host = this.getHost();
        if (host != null) {
            final TileEntity te = host.getTile();

            final BlockPos pos = te.getPos();

            if (this.isAnnihilationPlane(te.getWorld().getTileEntity(pos.offset(facingRight.getOpposite())), this.getSide())) {
                left = true;
            }

            if (this.isAnnihilationPlane(te.getWorld().getTileEntity(pos.offset(facingRight)), this.getSide())) {
                right = true;
            }

            if (this.isAnnihilationPlane(te.getWorld().getTileEntity(pos.offset(facingUp.getOpposite())), this.getSide())) {
                down = true;
            }

            if (this.isAnnihilationPlane(te.getWorld().getTileEntity(pos.offset(facingUp)), this.getSide())) {
                up = true;
            }
        }

        return PlaneConnections.of(up, right, down, left);
    }

    @Override
    public void onNeighborChanged(IBlockAccess w, BlockPos pos, BlockPos neighbor) {
        if (pos.offset(this.getSide().getFacing()).equals(neighbor)) {
            this.refresh();
        }
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1;
    }

    private boolean isAnnihilationPlane(final TileEntity blockTileEntity, final AEPartLocation side) {
        if (blockTileEntity instanceof IPartHost) {
            final IPart p = ((IPartHost) blockTileEntity).getPart(side);
            return p != null && p.getClass() == this.getClass();
        }
        return false;
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
        final IBlockState state = w.getBlockState(pos);
        final Block block = state.getBlock();

        if (block instanceof IFluidBlock || block instanceof BlockLiquid) {
            final IFluidHandler fh = FluidUtil.getFluidHandler(w, pos, null);
            final IAEFluidStack blockFluid = AEFluidStack.fromFluidStack(fh.drain(Integer.MAX_VALUE, false));

            if (blockFluid != null) {
                if (this.storeFluid(blockFluid, false)) {
                    this.storeFluid(AEFluidStack.fromFluidStack(fh.drain(Integer.MAX_VALUE, true)), true);

                    AppEng.proxy.sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 64, w,
                            new PacketTransitionEffect(pos.getX(), pos.getY(), pos.getZ(), this.getSide(), true));

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
        return new TickingRequest(TickRates.AnnihilationPlane.getMin(), TickRates.AnnihilationPlane.getMax(), false, true);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        return this.pickupFluid();
    }

    private boolean storeFluid(IAEFluidStack stack, boolean modulate) {
        try {
            final IStorageGrid storage = this.getProxy().getStorage();
            final IMEInventory<IAEFluidStack> inv = storage.getInventory(AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class));

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
        return MODELS.getModel(this.getConnections(), this.isPowered(), this.isActive());
    }
}
