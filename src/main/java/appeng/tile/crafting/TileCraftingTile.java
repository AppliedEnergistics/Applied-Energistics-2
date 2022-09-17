/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.tile.crafting;


import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridHost;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEPartLocation;
import appeng.api.util.WorldCoord;
import appeng.block.crafting.BlockCraftingUnit;
import appeng.block.crafting.BlockCraftingUnit.CraftingUnitType;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.implementations.CraftingCPUCalculator;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.AENetworkProxyMultiblock;
import appeng.tile.grid.AENetworkTile;
import appeng.util.Platform;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.util.*;


public class TileCraftingTile extends AENetworkTile implements IAEMultiBlock, IPowerChannelState {

    private final CraftingCPUCalculator calc = new CraftingCPUCalculator(this);
    private NBTTagCompound previousState = null;
    private boolean isCoreBlock = false;
    private CraftingCPUCluster cluster;

    public TileCraftingTile() {
        this.getProxy().setFlags(GridFlags.MULTIBLOCK, GridFlags.REQUIRE_CHANNEL);
        this.getProxy().setValidSides(EnumSet.noneOf(EnumFacing.class));
    }

    @Override
    protected AENetworkProxy createProxy() {
        return new AENetworkProxyMultiblock(this, "proxy", this.getItemFromTile(this), true);
    }

    @Override
    protected ItemStack getItemFromTile(final Object obj) {
        Optional<ItemStack> is = Optional.empty();

        if (((TileCraftingTile) obj).isAccelerator()) {
            is = AEApi.instance().definitions().blocks().craftingAccelerator().maybeStack(1);
        } else {
            is = AEApi.instance().definitions().blocks().craftingUnit().maybeStack(1);
        }

        return is.orElseGet(() -> super.getItemFromTile(obj));
    }

    @Override
    public boolean canBeRotated() {
        return true;// return BlockCraftingUnit.checkType( world.getBlockMetadata( xCoord, yCoord, zCoord ),
        // BlockCraftingUnit.BASE_MONITOR );
    }

    @Override
    public void setName(final String name) {
        super.setName(name);
        if (this.cluster != null) {
            this.cluster.updateName();
        }
    }

    public boolean isAccelerator() {
        if (this.world == null) {
            return false;
        }

        final BlockCraftingUnit unit = (BlockCraftingUnit) this.world.getBlockState(this.pos).getBlock();
        return unit.type == CraftingUnitType.ACCELERATOR;
    }

    @Override
    public void onReady() {
        super.onReady();
        this.getProxy().setVisualRepresentation(this.getItemFromTile(this));
        this.updateMultiBlock();
    }

    public void updateMultiBlock() {
        this.calc.calculateMultiblock(this.world, this.getLocation());
    }

    public void updateStatus(final CraftingCPUCluster c) {
        if (this.cluster != null && this.cluster != c) {
            this.cluster.breakCluster();
        }

        this.cluster = c;
        this.updateMeta(true);
    }

    public void updateMeta(final boolean updateFormed) {
        if (this.world == null || this.notLoaded() || this.isInvalid()) {
            return;
        }

        final boolean formed = this.isFormed();
        boolean power = false;

        if (this.getProxy().isReady()) {
            power = this.getProxy().isActive();
        }

        final IBlockState current = this.world.getBlockState(this.pos);

        // The tile might try to update while being destroyed
        if (current.getBlock() instanceof BlockCraftingUnit) {
            final IBlockState newState = current.withProperty(BlockCraftingUnit.POWERED, power).withProperty(BlockCraftingUnit.FORMED, formed);

            if (current != newState) {
                // Not using flag 2 here (only send to clients, prevent block update) will cause infinite loops
                // In case there is an inconsistency in the crafting clusters.
                this.world.setBlockState(this.pos, newState, 2);
            }
        }

        if (updateFormed) {
            if (formed) {
                this.getProxy().setValidSides(EnumSet.allOf(EnumFacing.class));
            } else {
                this.getProxy().setValidSides(EnumSet.noneOf(EnumFacing.class));
            }
        }
    }

    public boolean isFormed() {
        if (Platform.isClient()) {
            return this.world.getBlockState(this.pos).getValue(BlockCraftingUnit.FORMED);
        }
        return this.cluster != null;
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("core", this.isCoreBlock());
        if (this.isCoreBlock() && this.cluster != null) {
            this.cluster.writeToNBT(data);
        }
        return data;
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.setCoreBlock(data.getBoolean("core"));
        if (this.isCoreBlock()) {
            if (this.cluster != null) {
                this.cluster.readFromNBT(data);
            } else {
                this.setPreviousState(data.copy());
            }
        }
    }

    @Override
    public void disconnect(final boolean update) {
        if (this.cluster != null) {
            this.cluster.destroy();
            if (update) {
                this.updateMeta(true);
            }
        }
    }

    @Override
    public IAECluster getCluster() {
        return this.cluster;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @MENetworkEventSubscribe
    public void onPowerStateChange(final MENetworkChannelsChanged ev) {
        this.updateMeta(false);
    }

    @MENetworkEventSubscribe
    public void onPowerStateChange(final MENetworkPowerStatusChange ev) {
        this.updateMeta(false);
    }

    public boolean isStatus() {
        return false;
    }

    public boolean isStorage() {
        return false;
    }

    public int getStorageBytes() {
        return 0;
    }

    public void breakCluster() {
        if (this.cluster != null) {
            this.cluster.cancel();
            final IMEInventory<IAEItemStack> inv = this.cluster.getInventory();

            final LinkedList<WorldCoord> places = new LinkedList<>();

            final Iterator<IGridHost> i = this.cluster.getTiles();
            while (i.hasNext()) {
                final IGridHost h = i.next();
                if (h == this) {
                    places.add(new WorldCoord(this));
                } else {
                    final TileEntity te = (TileEntity) h;

                    for (final AEPartLocation d : AEPartLocation.SIDE_LOCATIONS) {
                        final WorldCoord wc = new WorldCoord(te);
                        wc.add(d, 1);
                        if (this.world.isAirBlock(wc.getPos())) {
                            places.add(wc);
                        }
                    }
                }
            }

            Collections.shuffle(places);

            if (places.isEmpty()) {
                throw new IllegalStateException(this.cluster + " does not contain any kind of blocks, which were destroyed.");
            }

            for (IAEItemStack ais : inv.getAvailableItems(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList())) {
                ais = ais.copy();
                ais.setStackSize(ais.getDefinition().getMaxStackSize());
                while (true) {
                    final IAEItemStack g = inv.extractItems(ais.copy(), Actionable.MODULATE, this.cluster.getActionSource());
                    if (g == null) {
                        break;
                    }

                    final WorldCoord wc = places.poll();
                    places.add(wc);

                    Platform.spawnDrops(this.world, wc.getPos(), Collections.singletonList(g.createItemStack()));
                }
            }

            this.cluster.destroy();
        }
    }

    @Override
    public boolean isPowered() {
        if (Platform.isClient()) {
            return this.world.getBlockState(this.pos).getValue(BlockCraftingUnit.POWERED);
        }
        return this.getProxy().isActive();
    }

    @Override
    public boolean isActive() {
        if (Platform.isServer()) {
            return this.getProxy().isActive();
        }
        return this.isPowered() && this.isFormed();
    }

    public boolean isCoreBlock() {
        return this.isCoreBlock;
    }

    public void setCoreBlock(final boolean isCoreBlock) {
        this.isCoreBlock = isCoreBlock;
    }

    public NBTTagCompound getPreviousState() {
        return this.previousState;
    }

    public void setPreviousState(final NBTTagCompound previousState) {
        this.previousState = previousState;
    }
}
