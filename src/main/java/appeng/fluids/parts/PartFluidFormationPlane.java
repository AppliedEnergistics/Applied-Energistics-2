package appeng.fluids.parts;


import appeng.api.AEApi;
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
import appeng.core.sync.GuiBridge;
import appeng.fluids.helper.IConfigurableFluidInventory;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.IAEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.storage.MEInventoryHandler;
import appeng.parts.automation.PartAbstractFormationPlane;
import appeng.parts.automation.PlaneModels;
import appeng.util.Platform;
import appeng.util.prioritylist.PrecisePriorityList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PartFluidFormationPlane extends PartAbstractFormationPlane<IAEFluidStack> implements IAEFluidInventory, IConfigurableFluidInventory {
    private static final PlaneModels MODELS = new PlaneModels("part/fluid_formation_plane_", "part/fluid_formation_plane_on_");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private final MEInventoryHandler<IAEFluidStack> myHandler = new MEInventoryHandler<>(this, AEApi.instance()
            .storage()
            .getStorageChannel(IFluidStorageChannel.class));
    private final AEFluidInventory config = new AEFluidInventory(this, 63);

    public PartFluidFormationPlane(final ItemStack is) {
        super(is);
        this.updateHandler();
    }

    @Override
    protected void updateHandler() {
        this.myHandler.setBaseAccess(AccessRestriction.WRITE);
        this.myHandler.setWhitelist(this.getInstalledUpgrades(Upgrades.INVERTER) > 0 ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST);
        this.myHandler.setPriority(this.getPriority());

        final IItemList<IAEFluidStack> priorityList = AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class).createList();

        final int slotsToUse = 18 + this.getInstalledUpgrades(Upgrades.CAPACITY) * 9;
        for (int x = 0; x < this.config.getSlots() && x < slotsToUse; x++) {
            final IAEFluidStack is = this.config.getFluidInSlot(x);
            if (is != null) {
                priorityList.add(is);
            }
        }
        this.myHandler.setPartitionList(new PrecisePriorityList<IAEFluidStack>(priorityList));

        try {
            this.getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
        } catch (final GridAccessException e) {
            // :P
        }
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable type, IActionSource src) {
        if (this.blocked || input == null || input.getStackSize() < Fluid.BUCKET_VOLUME) {
            // need a full bucket
            return input;
        }

        final TileEntity te = this.getHost().getTile();
        final World w = te.getWorld();
        final AEPartLocation side = this.getSide();
        final BlockPos pos = te.getPos().offset(side.getFacing());
        final IBlockState state = w.getBlockState(pos);

        if (this.canReplace(w, state, state.getBlock(), pos)) {
            if (type == Actionable.MODULATE) {
                final FluidStack fs = input.getFluidStack();
                fs.amount = Fluid.BUCKET_VOLUME;

                final FluidTank tank = new FluidTank(fs, Fluid.BUCKET_VOLUME);
                if (!FluidUtil.tryPlaceFluid(null, w, pos, tank, fs)) {
                    return input;
                }
            }
            final IAEFluidStack ret = input.copy();
            ret.setStackSize(input.getStackSize() - Fluid.BUCKET_VOLUME);
            return ret.getStackSize() == 0 ? null : ret;
        }
        this.blocked = true;
        return input;
    }

    private boolean canReplace(World w, IBlockState state, Block block, BlockPos pos) {
        return block.isReplaceable(w, pos) && !(block instanceof IFluidBlock) && !(block instanceof BlockLiquid) && !state.getMaterial().isLiquid();
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
        if (inv == this.config) {
            this.updateHandler();
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.config.readFromNBT(data, "config");
        this.updateHandler();
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        this.config.writeToNBT(data, "config");
    }

    @Override
    @MENetworkEventSubscribe
    public void powerRender(final MENetworkPowerStatusChange c) {
        this.stateChanged();
    }

    @Override
    @MENetworkEventSubscribe
    public void chanRender(final MENetworkChannelsChanged changedChannels) {
        this.stateChanged();
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        if (Platform.isServer()) {
            Platform.openGUI(player, this.getHost().getTile(), this.getSide(), GuiBridge.GUI_FLUID_FORMATION_PLANE);
        }

        return true;
    }

    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(final IStorageChannel channel) {
        if (channel == AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class)) {
            final List<IMEInventoryHandler> handler = new ArrayList<>(1);
            handler.add(this.myHandler);
            return handler;
        }
        return Collections.emptyList();
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.getConnections(), this.isPowered(), this.isActive());
    }

    public IAEFluidTank getConfig() {
        return this.config;
    }

    @Override
    public IFluidHandler getFluidInventoryByName(final String name) {
        if (name.equals("config")) {
            return this.config;
        }
        return null;
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return AEApi.instance().definitions().parts().fluidFormationnPlane().maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Override
    public GuiBridge getGuiBridge() {
        return GuiBridge.GUI_FLUID_FORMATION_PLANE;
    }
}
