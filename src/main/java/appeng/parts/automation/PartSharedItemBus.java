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

package appeng.parts.automation;


import appeng.api.config.RedstoneMode;
import appeng.api.config.Upgrades;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.me.GridAccessException;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.InventoryAdaptor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;


public abstract class PartSharedItemBus extends PartUpgradeable implements IGridTickable {

    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, 9);
    private boolean lastRedstone = false;

    public PartSharedItemBus(final ItemStack is) {
        super(is);
    }

    @Override
    public void upgradesChanged() {
        this.updateState();
    }

    @Override
    public void readFromNBT(final net.minecraft.nbt.NBTTagCompound extra) {
        super.readFromNBT(extra);
        this.getConfig().readFromNBT(extra, "config");
    }

    @Override
    public void writeToNBT(final net.minecraft.nbt.NBTTagCompound extra) {
        super.writeToNBT(extra);
        this.getConfig().writeToNBT(extra, "config");
    }

    @Override
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("config")) {
            return this.getConfig();
        }

        return super.getInventoryByName(name);
    }

    @Override
    public void onNeighborChanged(IBlockAccess w, BlockPos pos, BlockPos neighbor) {
        this.updateState();
        if (this.lastRedstone != this.getHost().hasRedstone(this.getSide())) {
            this.lastRedstone = !this.lastRedstone;
            if (this.lastRedstone && this.getRSMode() == RedstoneMode.SIGNAL_PULSE) {
                this.doBusWork();
            }
        }
    }

    protected InventoryAdaptor getHandler() {
        final TileEntity self = this.getHost().getTile();
        final TileEntity target = this.getTileEntity(self, self.getPos().offset(this.getSide().getFacing()));

        return InventoryAdaptor.getAdaptor(target, this.getSide().getFacing().getOpposite());
    }

    private TileEntity getTileEntity(final TileEntity self, final BlockPos pos) {
        final World w = self.getWorld();

        if (w.getChunkProvider().getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4) != null) {
            return w.getTileEntity(pos);
        }

        return null;
    }

    protected int availableSlots() {
        return Math.min(1 + this.getInstalledUpgrades(Upgrades.CAPACITY) * 4, this.getConfig().getSlots());
    }

    protected int calculateItemsToSend() {
        switch (this.getInstalledUpgrades(Upgrades.SPEED)) {
            default:
            case 0:
                return 1;
            case 1:
                return 8;
            case 2:
                return 32;
            case 3:
                return 64;
            case 4:
                return 96;
        }
    }

    /**
     * Checks if the bus can actually do something.
     * <p>
     * Currently this tests if the chunk for the target is actually loaded.
     *
     * @return true, if the the bus should do its work.
     */
    protected boolean canDoBusWork() {
        final TileEntity self = this.getHost().getTile();
        final BlockPos selfPos = self.getPos().offset(this.getSide().getFacing());
        final int xCoordinate = selfPos.getX();
        final int zCoordinate = selfPos.getZ();
        final World world = self.getWorld();

        return world != null && world.getChunkProvider().getLoadedChunk(xCoordinate >> 4, zCoordinate >> 4) != null;
    }

    private void updateState() {
        try {
            if (!this.isSleeping()) {
                this.getProxy().getTick().wakeDevice(this.getProxy().getNode());
            } else {
                this.getProxy().getTick().sleepDevice(this.getProxy().getNode());
            }
        } catch (final GridAccessException e) {
            // :P
        }
    }

    protected abstract TickRateModulation doBusWork();

    AppEngInternalAEInventory getConfig() {
        return this.config;
    }
}
