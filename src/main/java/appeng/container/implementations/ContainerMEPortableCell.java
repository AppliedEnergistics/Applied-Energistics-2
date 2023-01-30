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

package appeng.container.implementations;


import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.IUpgradeableCellContainer;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.container.slot.SlotRestrictedInput;
import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.util.Platform;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;


public class ContainerMEPortableCell extends ContainerMEMonitorable implements IUpgradeableCellContainer {

    protected final WirelessTerminalGuiObject wirelessTerminalGUIObject;
    private final int slot;
    private double powerMultiplier = 0.5;
    private int ticks = 0;


    public ContainerMEPortableCell(InventoryPlayer ip, WirelessTerminalGuiObject monitorable, boolean bindInventory) {
        super(ip, monitorable, bindInventory);
        if (monitorable != null) {
            final int slotIndex = ((IInventorySlotAware) monitorable).getInventorySlot();
            this.lockPlayerInventorySlot(slotIndex);
            this.slot = slotIndex;
        } else {
            this.slot = -1;
            this.lockPlayerInventorySlot(ip.currentItem);
        }
        this.wirelessTerminalGUIObject = monitorable;
        if (bindInventory) {
            this.bindPlayerInventory(ip, 0, 0);
        }
        this.setupUpgrades();
    }

    @Override
    public void detectAndSendChanges() {
        final ItemStack currentItem = this.slot < 0 ? this.getPlayerInv().getCurrentItem() : this.getPlayerInv().getStackInSlot(this.slot);

        if (this.wirelessTerminalGUIObject == null || currentItem.isEmpty()) {
            this.setValidContainer(false);
        } else if (!this.wirelessTerminalGUIObject.getItemStack().isEmpty() && currentItem != this.wirelessTerminalGUIObject.getItemStack()) {
            if (ItemStack.areItemsEqual(this.wirelessTerminalGUIObject.getItemStack(), currentItem)) {
                this.getPlayerInv().setInventorySlotContents(this.getPlayerInv().currentItem, this.wirelessTerminalGUIObject.getItemStack());
            } else {
                this.setValidContainer(false);
            }
        }

        // drain 1 ae t
        this.ticks++;
        if (this.ticks > 10) {
            this.wirelessTerminalGUIObject.extractAEPower(this.getPowerMultiplier() * this.ticks, Actionable.MODULATE, PowerMultiplier.CONFIG);
            this.ticks = 0;
        }

        if (!this.wirelessTerminalGUIObject.rangeCheck()) {
            if (Platform.isServer() && this.isValidContainer()) {
                this.getPlayerInv().player.sendMessage(PlayerMessages.OutOfRange.get());
            }

            this.setValidContainer(false);
        } else {
            this.setPowerMultiplier(AEConfig.instance().wireless_getDrainRate(this.wirelessTerminalGUIObject.getRange()));
        }

        super.detectAndSendChanges();
    }

    private double getPowerMultiplier() {
        return this.powerMultiplier;
    }

    void setPowerMultiplier(final double powerMultiplier) {
        this.powerMultiplier = powerMultiplier;
    }

    public int availableUpgrades() {
        return 2;
    }

    @Override
    public void setupUpgrades() {
        if (wirelessTerminalGUIObject != null) {
            final IItemHandler upgrades = wirelessTerminalGUIObject.getInventoryByName("upgrades");
            for (int a = 0; a < availableUpgrades(); a++) {
                this.addSlotToContainer(
                        (new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, a * 18, this.getInventoryPlayer()))
                                .setNotDraggable());
            }
        }
    }

}
