/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.implementations.IUpgradeableObject;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.helpers.DualityItemInterface;
import appeng.helpers.IItemInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.me.helpers.BlockEntityNodeListener;

public class ItemInterfaceBlockEntity extends AENetworkInvBlockEntity
        implements IItemInterfaceHost, IPriorityHost, IUpgradeableObject, IConfigurableObject {

    private static final IGridNodeListener<ItemInterfaceBlockEntity> NODE_LISTENER = new BlockEntityNodeListener<>() {
        @Override
        public void onGridChanged(ItemInterfaceBlockEntity nodeOwner, IGridNode node) {
            nodeOwner.duality.gridChanged();
        }
    };

    private final DualityItemInterface duality = new DualityItemInterface(this.getMainNode(), this,
            getItemFromBlockEntity());

    public ItemInterfaceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    protected IManagedGridNode createMainNode() {
        return GridHelper.createManagedNode(this, NODE_LISTENER);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.duality.notifyNeighbors();
    }

    @Override
    public void getDrops(final Level level, final BlockPos pos, final List<ItemStack> drops) {
        this.duality.addDrops(drops);
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
    public InternalInventory getInternalInventory() {
        return this.duality.getInternalInventory();
    }

    @Override
    public void onChangeInventory(final InternalInventory inv, final int slot,
            final ItemStack removed, final ItemStack added) {
        this.duality.onChangeInventory(inv, slot, removed, added);
    }

    @Override
    public DualityItemInterface getInterfaceDuality() {
        return this.duality;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return AEBlocks.ITEM_INTERFACE.stack();
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        var inv = getInterfaceDuality().getSubInventory(id);
        return inv != null ? inv : super.getSubInventory(id);
    }
}
