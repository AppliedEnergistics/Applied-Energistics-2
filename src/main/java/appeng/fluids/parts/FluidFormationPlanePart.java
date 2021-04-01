/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.fluids.parts;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import alexiil.mc.lib.attributes.fluid.world.FluidWorldUtil;

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
        this.myHandler.setPartitionList(new PrecisePriorityList<IAEFluidStack>(priorityList));

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

        final TileEntity te = this.getHost().getTile();
        final World w = te.getWorld();
        final AEPartLocation side = this.getSide();
        final BlockPos pos = te.getPos().offset(side.getFacing());

        FluidVolume volume = input.getFluidStack();
        FluidVolume remainder = FluidWorldUtil.fill(w, pos, volume,
                type == Actionable.MODULATE ? Simulation.ACTION : Simulation.SIMULATE);
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

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
        if (inv == this.config) {
            this.updateHandler();
        }
    }

    protected boolean isBlocking(IBlockReader w, BlockPos pos) {
        // Mirror the restrictions from the fill method
        BlockState state = w.getBlockState(pos);
        Block block = state.getBlock();
        return !state.isAir() && !(block instanceof ILiquidContainer) && !(block instanceof FlowingFluidBlock);
    }

    @Override
    public void readFromNBT(final CompoundNBT data) {
        super.readFromNBT(data);
        this.config.readFromNBT(data, "config");
        this.updateHandler();
    }

    @Override
    public void writeToNBT(final CompoundNBT data) {
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
    public boolean onPartActivate(final PlayerEntity player, final Hand hand, final Vector3d pos) {
        if (!isRemote()) {
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
    public ContainerType<?> getContainerType() {
        return FluidFormationPlaneContainer.TYPE;
    }
}
