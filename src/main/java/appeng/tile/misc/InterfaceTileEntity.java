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

package appeng.tile.misc;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.BlockState;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalBlockPos;
import appeng.api.util.IConfigManager;
import appeng.container.implementations.InterfaceContainer;
import appeng.core.definitions.AEBlocks;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.me.ManagedGridNode;
import appeng.me.helpers.TileEntityNodeListener;
import appeng.tile.grid.AENetworkInvTileEntity;
import appeng.util.Platform;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.InvOperation;

public class InterfaceTileEntity extends AENetworkInvTileEntity
        implements IInventoryDestination, IInterfaceHost, IPriorityHost {

    private static final IGridNodeListener<InterfaceTileEntity> NODE_LISTENER = new TileEntityNodeListener<>() {
        @Override
        public void onGridChanged(InterfaceTileEntity nodeOwner, IGridNode node) {
            nodeOwner.duality.gridChanged();
        }
    };

    private final DualityInterface duality = new DualityInterface(this.getMainNode(), this, getItemFromTile());

    // Indicates that this interface has no specific direction set
    private boolean omniDirectional = true;

    public InterfaceTileEntity(TileEntityType<?> tileEntityTypeIn) {
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
    public void getDrops(final World w, final BlockPos pos, final List<ItemStack> drops) {
        this.duality.addDrops(drops);
    }

    @Override
    public void onReady() {
        this.configureNodeSides();

        super.onReady();
        this.duality.initialize();
    }

    @Override
    public CompoundNBT write(final CompoundNBT data) {
        super.write(data);
        data.putBoolean("omniDirectional", this.omniDirectional);
        this.duality.writeToNBT(data);
        return data;
    }

    @Override
    public void read(BlockState blockState, final CompoundNBT data) {
        super.read(blockState, data);
        this.omniDirectional = data.getBoolean("omniDirectional");

        this.duality.readFromNBT(data);
    }

    @Override
    protected boolean readFromStream(final PacketBuffer data) throws IOException {
        final boolean c = super.readFromStream(data);
        boolean oldOmniDirectional = this.omniDirectional;
        this.omniDirectional = data.readBoolean();
        return oldOmniDirectional != this.omniDirectional || c;
    }

    @Override
    protected void writeToStream(final PacketBuffer data) throws IOException {
        super.writeToStream(data);
        data.writeBoolean(this.omniDirectional);
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
    public boolean canInsert(final ItemStack stack) {
        return this.duality.canInsert(stack);
    }

    @Override
    public IItemHandler getInventoryByName(final String name) {
        return this.duality.getInventoryByName(name);
    }

    @Override
    public IItemHandler getInternalInventory() {
        return this.duality.getInternalInventory();
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removed, final ItemStack added) {
        this.duality.onChangeInventory(inv, slot, mc, removed, added);
    }

    @Override
    public DualityInterface getInterfaceDuality() {
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
    public TileEntity getTileEntity() {
        return this;
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.duality.getConfigManager();
    }

    @Override
    public boolean pushPattern(final ICraftingPatternDetails patternDetails, final CraftingInventory table) {
        return this.duality.pushPattern(patternDetails, table);
    }

    @Override
    public boolean isBusy() {
        return this.duality.isBusy();
    }

    @Override
    public void provideCrafting(final ICraftingProviderHelper craftingTracker) {
        this.duality.provideCrafting(craftingTracker);
    }

    @Override
    public int getInstalledUpgrades(final Upgrades u) {
        return this.duality.getInstalledUpgrades(u);
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.duality.getRequestedJobs();
    }

    @Override
    public IAEItemStack injectCraftedItems(final ICraftingLink link, final IAEItemStack items, final Actionable mode) {
        return this.duality.injectCraftedItems(link, items, mode);
    }

    @Override
    public void jobStateChange(final ICraftingLink link) {
        this.duality.jobStateChange(link);
    }

    @Override
    public int getPriority() {
        return this.duality.getPriority();
    }

    @Override
    public void setPriority(final int newValue) {
        this.duality.setPriority(newValue);
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
        return AEBlocks.INTERFACE.stack();
    }

    @Override
    public ContainerType<?> getContainerType() {
        return InterfaceContainer.TYPE;
    }
}
