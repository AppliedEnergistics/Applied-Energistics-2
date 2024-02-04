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

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.features.HotkeyAction;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.implementations.menuobjects.IPortableTerminal;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.ILinkStatus;
import appeng.api.storage.MEStorage;
import appeng.api.util.IConfigManager;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.items.contents.StackDependentSupplier;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.me.storage.NullInventory;
import appeng.me.storage.SupplierStorage;
import appeng.menu.ISubMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class WirelessTerminalMenuHost<T extends WirelessTerminalItem> extends ItemMenuHost<T> implements IPortableTerminal, IActionHost {

    private final BiConsumer<Player, ISubMenu> returnToMainMenu;
    @Nullable
    private IWirelessAccessPoint currentAccessPoint;
    /**
     * The distance to the currently connected access point in blocks.
     */
    protected double currentDistanceFromGrid = Double.MAX_VALUE;
    private final MEStorage storage;
    @Nullable
    private Component lastLinkError;

    public WirelessTerminalMenuHost(T item, Player player, ItemMenuHostLocator locator,
                                    BiConsumer<Player, ISubMenu> returnToMainMenu) {
        super(item, player, locator);
        this.returnToMainMenu = returnToMainMenu;

        this.storage = new SupplierStorage(new StackDependentSupplier<>(
                this::getItemStack, this::getStorageFromStack
        ));
    }

    @Override
    public ILinkStatus getLinkStatus() {
        if (currentAccessPoint != null) {
            return ILinkStatus.ofConnected();
        }
        if (lastLinkError != null) {
            return ILinkStatus.ofDisconnected(lastLinkError);
        }

        return ILinkStatus.ofDisconnected(PlayerMessages.OutOfRange.text());
    }

    @Nullable
    private MEStorage getStorageFromStack(ItemStack stack) {
        var targetGrid = getLinkedGrid(stack);
        if (targetGrid != null) {
            return targetGrid.getStorageService().getInventory();
        }
        return NullInventory.of();
    }

    @Nullable
    private IGrid getLinkedGrid(ItemStack stack) {
        this.lastLinkError = null;
        return getItem().getLinkedGrid(stack, getPlayer().level(), err -> lastLinkError = err);
    }

    @Override
    public MEStorage getInventory() {
        return this.storage;
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        final double extracted = Math.min(amt, getItem().getAECurrentPower(getItemStack()));

        if (mode == Actionable.SIMULATE) {
            return extracted;
        }

        return getItem().usePower(getPlayer(), extracted, getItemStack()) ? extracted : 0;
    }

    @Override
    public IConfigManager getConfigManager() {
        return getItem().getConfigManager(getItemStack());
    }

    @Override
    public IGridNode getActionableNode() {
        this.rangeCheck();
        if (this.currentAccessPoint != null) {
            return this.currentAccessPoint.getActionableNode();
        }
        return null;
    }

    public boolean rangeCheck() {
        this.currentAccessPoint = null;
        this.currentDistanceFromGrid = Double.MAX_VALUE;

        var targetGrid = getLinkedGrid(getItemStack());
        if (targetGrid != null) {
            @Nullable
            IWirelessAccessPoint bestWap = null;
            double bestSqDistance = Double.MAX_VALUE;

            // Find closest WAP
            for (var wap : targetGrid.getMachines(WirelessAccessPointBlockEntity.class)) {
                double sqDistance = getWapSqDistance(wap);

                // If the WAP is not suitable then MAX_VALUE will be returned and the check will fail
                if (sqDistance < bestSqDistance) {
                    bestSqDistance = sqDistance;
                    bestWap = wap;
                }
            }

            // If no WAP is found this will work too
            this.currentAccessPoint = bestWap;
            this.currentDistanceFromGrid = Math.sqrt(bestSqDistance);
        }

        return this.currentAccessPoint != null;
    }

    /**
     * @return square distance to WAP if the WAP can be used, or {@link Double#MAX_VALUE} if it cannot be used.
     */
    protected double getWapSqDistance(IWirelessAccessPoint wap) {
        double rangeLimit = wap.getRange();
        rangeLimit *= rangeLimit;

        var dc = wap.getLocation();

        if (dc.getLevel() == this.getPlayer().level()) {
            var offX = dc.getPos().getX() - this.getPlayer().getX();
            var offY = dc.getPos().getY() - this.getPlayer().getY();
            var offZ = dc.getPos().getZ() - this.getPlayer().getZ();

            double r = offX * offX + offY * offY + offZ * offZ;
            if (r < rangeLimit && wap.isActive()) {
                return r;
            }
        }

        return Double.MAX_VALUE;
    }

    @Override
    public boolean onBroadcastChanges(AbstractContainerMenu menu) {
        checkWirelessRange();
        return super.onBroadcastChanges(menu) && drainPower();
    }

    /**
     * Can only be used with a host that extends {@link WirelessTerminalMenuHost}
     */
    private void checkWirelessRange() {
        if (rangeCheck()) {
            setPowerDrainPerTick(getPowerDrainRate(currentDistanceFromGrid));
        } else {
            // No drain when we are connected
            setPowerDrainPerTick(0);
        }
    }

    private double getPowerDrainRate(double distance) {
        return AEConfig.instance().wireless_getDrainRate(distance);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        returnToMainMenu.accept(player, subMenu);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return getItemStack();
    }

    public String getCloseHotkey() {
        return HotkeyAction.WIRELESS_TERMINAL;
    }
}
