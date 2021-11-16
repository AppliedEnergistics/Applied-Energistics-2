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

package appeng.helpers;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.features.IWirelessTerminalHandler;
import appeng.api.features.Locatables;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.AEKey;
import appeng.api.util.DimensionalBlockPos;
import appeng.api.util.IConfigManager;
import appeng.blockentity.networking.WirelessBlockEntity;
import appeng.menu.interfaces.IInventorySlotAware;

public class WirelessTerminalGuiObject implements IPortableCell, IActionHost, IInventorySlotAware {

    private final ItemStack effectiveItem;
    private final IWirelessTerminalHandler wth;
    private final Player myPlayer;
    private IGrid targetGrid;
    private IStorageService sg;
    private IWirelessAccessPoint myWap;
    private double sqRange = Double.MAX_VALUE;
    private double myRange = Double.MAX_VALUE;
    private final int inventorySlot;

    public WirelessTerminalGuiObject(final IWirelessTerminalHandler wh, final ItemStack is, final Player ep,
            int inventorySlot) {
        this.effectiveItem = is;
        this.myPlayer = ep;
        this.wth = wh;
        this.inventorySlot = inventorySlot;

        var gridKey = wh.getGridKey(is);
        if (gridKey.isEmpty()) {
            return;
        }

        var actionHost = Locatables.securityStations().get(ep.level, gridKey.getAsLong());
        if (actionHost != null) {
            final IGridNode n = actionHost.getActionableNode();
            if (n != null) {
                this.targetGrid = n.getGrid();
                if (this.targetGrid != null) {
                    this.sg = this.targetGrid.getStorageService();
                }
            }
        }
    }

    public double getRange() {
        return this.myRange;
    }

    @Override
    public <T extends AEKey> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        return this.sg.getInventory(channel);
    }

    @Override
    public double extractAEPower(final double amt, final Actionable mode, final PowerMultiplier usePowerMultiplier) {
        if (this.wth != null && this.effectiveItem != null) {
            if (mode == Actionable.SIMULATE) {
                return this.wth.hasPower(this.myPlayer, amt, this.effectiveItem) ? amt : 0;
            }
            return this.wth.usePower(this.myPlayer, amt, this.effectiveItem) ? amt : 0;
        }
        return 0.0;
    }

    @Override
    public ItemStack getItemStack() {
        return this.effectiveItem;
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.wth.getConfigManager(this.effectiveItem);
    }

    @Override
    public IGridNode getActionableNode() {
        this.rangeCheck();
        if (this.myWap != null) {
            return this.myWap.getActionableNode();
        }
        return null;
    }

    public boolean rangeCheck() {
        this.sqRange = this.myRange = Double.MAX_VALUE;

        if (this.targetGrid != null) {
            if (this.myWap != null) {
                if (this.myWap.getGrid() == this.targetGrid && this.testWap(this.myWap)) {
                    return true;
                }
                return false;
            }

            this.myWap = null;

            for (var wap : this.targetGrid.getMachines(WirelessBlockEntity.class)) {
                if (this.testWap(wap)) {
                    this.myWap = wap;
                }
            }

            return this.myWap != null;
        }
        return false;
    }

    protected boolean testWap(final IWirelessAccessPoint wap) {
        double rangeLimit = wap.getRange();
        rangeLimit *= rangeLimit;

        final DimensionalBlockPos dc = wap.getLocation();

        if (dc.getLevel() == this.myPlayer.level) {
            var offX = dc.getPos().getX() - this.myPlayer.getX();
            var offY = dc.getPos().getY() - this.myPlayer.getY();
            var offZ = dc.getPos().getZ() - this.myPlayer.getZ();

            final double r = offX * offX + offY * offY + offZ * offZ;
            if (r < rangeLimit && this.sqRange > r && wap.isActive()) {
                this.sqRange = r;
                this.myRange = Math.sqrt(r);
                return true;
            }
        }
        return false;
    }

    @Override
    public int getInventorySlot() {
        return this.inventorySlot;
    }

}
