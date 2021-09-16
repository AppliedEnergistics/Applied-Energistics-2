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

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import appeng.api.AEApi;
import appeng.api.implementations.IUpgradeableObject;
import appeng.api.inventories.InternalInventory;
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
import appeng.util.Platform;

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

    // Indicates that this interface has no specific direction set
    private boolean omniDirectional = true;

    public ItemInterfaceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
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

    public void setSide(final Direction facing) {
        if (isRemote()) {
            return;
        }

        Direction newForward = facing;

        if (!this.omniDirectional && this.getForward() == facing.getOpposite()) {
            newForward = facing;
        } else if (!this.omniDirectional
                && (this.getForward() == facing || this.getForward() == facing.getOpposite())) {
            this.omniDirectional = true;
        } else if (this.omniDirectional) {
            newForward = facing.getOpposite();
            this.omniDirectional = false;
        } else {
            newForward = Platform.rotateAround(this.getForward(), facing);
        }

        if (this.omniDirectional) {
            this.setOrientation(Direction.NORTH, Direction.UP);
        } else {
            Direction newUp = Direction.UP;
            if (newForward == Direction.UP || newForward == Direction.DOWN) {
                newUp = Direction.NORTH;
            }
            this.setOrientation(newForward, newUp);
        }

        this.configureNodeSides();
        this.markForUpdate();
        this.saveChanges();
    }

    private void configureNodeSides() {
        if (this.omniDirectional) {
            this.getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
        } else {
            this.getMainNode().setExposedOnSides(EnumSet.complementOf(EnumSet.of(this.getForward())));
        }
    }

    @Override
    public void getDrops(final Level level, final BlockPos pos, final List<ItemStack> drops) {
        this.duality.addDrops(drops);
    }

    @Override
    public void onReady() {
        this.configureNodeSides();

        super.onReady();
        this.duality.initialize();
    }

    @Override
    public CompoundTag save(final CompoundTag data) {
        super.save(data);
        data.putBoolean("omniDirectional", this.omniDirectional);
        this.duality.writeToNBT(data);
        return data;
    }

    @Override
    public void load(final CompoundTag data) {
        super.load(data);
        this.omniDirectional = data.getBoolean("omniDirectional");

        this.duality.readFromNBT(data);
    }

    @Override
    protected boolean readFromStream(final FriendlyByteBuf data) throws IOException {
        final boolean c = super.readFromStream(data);
        boolean oldOmniDirectional = this.omniDirectional;
        this.omniDirectional = data.readBoolean();
        return oldOmniDirectional != this.omniDirectional || c;
    }

    @Override
    protected void writeToStream(final FriendlyByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeBoolean(this.omniDirectional);
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
    public EnumSet<Direction> getTargets() {
        if (this.omniDirectional) {
            return EnumSet.allOf(Direction.class);
        }
        return EnumSet.of(this.getForward());
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    /**
     * @return True if this interface is omni-directional.
     */
    public boolean isOmniDirectional() {
        return this.omniDirectional;
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
