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

import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.implementations.menuobjects.IPortableTerminal;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.MEStorage;
import appeng.api.util.IConfigManager;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.menu.ISubMenu;

public class WirelessTerminalMenuHost extends ItemMenuHost implements IPortableTerminal, IActionHost {

    private final WirelessTerminalItem terminal;
    private final BiConsumer<Player, ISubMenu> returnToMainMenu;
    private IGrid targetGrid;
    private IStorageService sg;
    private IWirelessAccessPoint myWap;
    private double sqRange = Double.MAX_VALUE;
    /**
     * The distance to the currently connected access point in blocks.
     */
    private double currentDistanceFromGrid = Double.MAX_VALUE;

    public WirelessTerminalMenuHost(Player player, @Nullable Integer slot, ItemStack itemStack,
            BiConsumer<Player, ISubMenu> returnToMainMenu) {
        super(player, slot, itemStack);
        if (!(itemStack.getItem() instanceof WirelessTerminalItem wirelessTerminalItem)) {
            throw new IllegalArgumentException("Can only use this class with subclasses of WirelessTerminalItem");
        }
        this.terminal = wirelessTerminalItem;
        this.returnToMainMenu = returnToMainMenu;

        this.targetGrid = wirelessTerminalItem.getLinkedGrid(itemStack, player.level, player);
        if (this.targetGrid != null) {
            this.sg = this.targetGrid.getStorageService();
        }
    }

    @Override
    public MEStorage getInventory() {
        return this.sg != null ? this.sg.getInventory() : null;
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        if (this.terminal != null) {
            final double extracted = Math.min(amt, this.terminal.getAECurrentPower(getItemStack()));

            if (mode == Actionable.SIMULATE) {
                return extracted;
            }

            return this.terminal.usePower(getPlayer(), extracted, getItemStack()) ? extracted : 0;
        }
        return 0.0;
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.terminal.getConfigManager(getItemStack());
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
        this.sqRange = this.currentDistanceFromGrid = Double.MAX_VALUE;

        if (this.targetGrid != null) {
            if (this.myWap != null) {
                // If the grid changed, give up
                if (this.myWap.getGrid() != this.targetGrid) {
                    return false;
                }

                if (this.testWap(this.myWap)) {
                    // Still in range of current AP
                    return true;
                }
            }

            // else: Did not have an AP yet, or no longer in range of current AP. Try to find one we are in range of.

            for (var wap : this.targetGrid.getMachines(WirelessAccessPointBlockEntity.class)) {
                // `this.myWap` either already returned false for `this.testWap(this.myWap)`, or is null. no need to
                // check it again
                if (wap != this.myWap && this.testWap(wap)) {
                    this.myWap = wap;
                    // break now, if multiple APs are in range we just take the first one we find
                    break;
                }
            }

            return this.myWap != null;
        }
        return false;
    }

    protected boolean testWap(IWirelessAccessPoint wap) {
        double rangeLimit = wap.getRange();
        rangeLimit *= rangeLimit;

        var dc = wap.getLocation();

        if (dc.getLevel() == this.getPlayer().level) {
            var offX = dc.getPos().getX() - this.getPlayer().getX();
            var offY = dc.getPos().getY() - this.getPlayer().getY();
            var offZ = dc.getPos().getZ() - this.getPlayer().getZ();

            final double r = offX * offX + offY * offY + offZ * offZ;
            if (r < rangeLimit && this.sqRange > r && wap.isActive()) {
                this.sqRange = r;
                this.currentDistanceFromGrid = Math.sqrt(r);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onBroadcastChanges(AbstractContainerMenu menu) {
        return super.onBroadcastChanges(menu)
                && checkWirelessRange(menu)
                && drainPower();
    }

    /**
     * Can only be used with a host that extends {@link WirelessTerminalMenuHost}
     */
    private boolean checkWirelessRange(AbstractContainerMenu menu) {
        if (!rangeCheck()) {
            if (!isClientSide()) {
                getPlayer().displayClientMessage(PlayerMessages.OutOfRange.text(), true);
            }
            return false;
        }

        setPowerDrainPerTick(AEConfig.instance().wireless_getDrainRate(currentDistanceFromGrid));
        return true;
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        returnToMainMenu.accept(player, subMenu);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return getItemStack();
    }
}
