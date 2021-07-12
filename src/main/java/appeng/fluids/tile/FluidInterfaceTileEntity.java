/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.tile;

import java.util.EnumSet;

import javax.annotation.Nullable;

import appeng.api.networking.IGridNodeListener;
import appeng.me.helpers.ManagedGridNode;
import appeng.me.helpers.TileEntityNodeListener;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalBlockPos;
import appeng.api.util.IConfigManager;
import appeng.core.definitions.AEBlocks;
import appeng.fluids.container.FluidInterfaceContainer;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IConfigurableFluidInventory;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.tile.grid.AENetworkTileEntity;

public class FluidInterfaceTileEntity extends AENetworkTileEntity
        implements IFluidInterfaceHost, IPriorityHost, IConfigurableFluidInventory {

    private static final IGridNodeListener<FluidInterfaceTileEntity> NODE_LISTENER = new TileEntityNodeListener<>() {
        @Override
        public void onGridChanged(FluidInterfaceTileEntity nodeOwner, IGridNode node) {
            nodeOwner.duality.gridChanged();
        }
    };

    private final DualityFluidInterface duality = new DualityFluidInterface(this.getMainNode(), this);

    public FluidInterfaceTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    protected ManagedGridNode createMainNode() {
        return new ManagedGridNode(this, NODE_LISTENER);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.ActiveChangeReason reason) {
        this.duality.notifyNeighbors();
    }

    @Override
    public DualityFluidInterface getDualityFluidInterface() {
        return this.duality;
    }

    @Override
    public TileEntity getTileEntity() {
        return this;
    }

    @Override
    public CompoundNBT write(final CompoundNBT data) {
        super.write(data);
        this.duality.writeToNBT(data);
        return data;
    }

    @Override
    public void read(BlockState state, final CompoundNBT data) {
        super.read(state, data);
        this.duality.readFromNBT(data);
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return this.duality.getCableConnectionType(dir);
    }

    @Override
    public DimensionalBlockPos getLocation() {
        return this.duality.getLocation();
    }

    @Override
    public EnumSet<Direction> getTargets() {
        return EnumSet.allOf(Direction.class);
    }

    @Override
    public int getPriority() {
        return this.duality.getPriority();
    }

    @Override
    public void setPriority(final int newValue) {
        this.duality.setPriority(newValue);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        LazyOptional<T> result = this.duality.getCapability(capability, facing);
        if (result.isPresent()) {
            return result;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public int getInstalledUpgrades(Upgrades u) {
        return this.duality.getInstalledUpgrades(u);
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.duality.getConfigManager();
    }

    @Override
    public IItemHandler getInventoryByName(String name) {
        return this.duality.getInventoryByName(name);
    }

    @Override
    public IFluidHandler getFluidInventoryByName(final String name) {
        return this.duality.getFluidInventoryByName(name);
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return AEBlocks.FLUID_INTERFACE.stack();
    }

    @Override
    public ContainerType<?> getContainerType() {
        return FluidInterfaceContainer.TYPE;
    }
}
