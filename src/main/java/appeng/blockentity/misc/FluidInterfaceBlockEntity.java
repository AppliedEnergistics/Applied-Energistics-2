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

package appeng.blockentity.misc;

import java.util.EnumSet;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.helpers.DualityFluidInterface;
import appeng.helpers.IConfigurableFluidInventory;
import appeng.helpers.IFluidInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.menu.implementations.FluidInterfaceMenu;

public class FluidInterfaceBlockEntity extends AENetworkBlockEntity
        implements IFluidInterfaceHost, IPriorityHost, IConfigurableFluidInventory {

    private static final IGridNodeListener<FluidInterfaceBlockEntity> NODE_LISTENER = new BlockEntityNodeListener<>() {
        @Override
        public void onGridChanged(FluidInterfaceBlockEntity nodeOwner, IGridNode node) {
            nodeOwner.duality.gridChanged();
        }
    };

    private final DualityFluidInterface duality = new DualityFluidInterface(this.getMainNode(), this);

    public FluidInterfaceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    protected IManagedGridNode createMainNode() {
        return AEApi.grid().createManagedNode(this, NODE_LISTENER);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.duality.notifyNeighbors();
    }

    @Override
    public DualityFluidInterface getDualityFluidInterface() {
        return this.duality;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public CompoundTag save(final CompoundTag data) {
        super.save(data);
        this.duality.writeToNBT(data);
        return data;
    }

    @Override
    public void load(final CompoundTag data) {
        super.load(data);
        this.duality.readFromNBT(data);
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return this.duality.getCableConnectionType(dir);
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
    public MenuType<?> getMenuType() {
        return FluidInterfaceMenu.TYPE;
    }
}
