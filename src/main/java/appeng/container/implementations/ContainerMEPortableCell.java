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
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.helpers.WirelessTerminalGuiObject;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;


public class ContainerMEPortableCell extends ContainerMEMonitorable {

    private double powerMultiplier = 0.5;

    protected final IPortableCell civ;
    private int ticks = 0;
    private final int slot;

    public ContainerMEPortableCell(final InventoryPlayer ip, final IPortableCell monitorable) {
        this(ip, monitorable, null, true);
    }

    public ContainerMEPortableCell(final InventoryPlayer ip, final IPortableCell monitorable, IGuiItemObject iGuiItemObject) {
        this(ip, monitorable, iGuiItemObject, true);
    }

    public ContainerMEPortableCell(InventoryPlayer ip, IPortableCell monitorable, IGuiItemObject iGuiItemObject, boolean bindInventory) {
        super(ip, monitorable, iGuiItemObject, bindInventory);
        if (monitorable instanceof IInventorySlotAware) {
            final int slotIndex = ((IInventorySlotAware) monitorable).getInventorySlot();
            this.lockPlayerInventorySlot(slotIndex);
            this.slot = slotIndex;
        } else {
            this.slot = -1;
            this.lockPlayerInventorySlot(ip.currentItem);
        }
        this.civ = monitorable;
        if (bindInventory) {
            this.bindPlayerInventory(ip, 0, 0);
        }
    }

    @Override
    public void detectAndSendChanges() {
        final ItemStack currentItem = this.slot < 0 ? this.getPlayerInv().getCurrentItem() : this.getPlayerInv().getStackInSlot(this.slot);

        if (this.civ == null || currentItem.isEmpty()) {
            this.setValidContainer(false);
        } else if (this.civ != null && !this.civ.getItemStack().isEmpty() && currentItem != this.civ.getItemStack()) {
            if (ItemStack.areItemsEqual(this.civ.getItemStack(), currentItem)) {
                this.getPlayerInv().setInventorySlotContents(this.getPlayerInv().currentItem, this.civ.getItemStack());
            } else {
                this.setValidContainer(false);
            }
        }

        // drain 1 ae t
        this.ticks++;
        if (this.ticks > 10) {
            this.civ.extractAEPower(this.getPowerMultiplier() * this.ticks, Actionable.MODULATE, PowerMultiplier.CONFIG);
            this.ticks = 0;
        }
        super.detectAndSendChanges();
    }

    private double getPowerMultiplier() {
        return this.powerMultiplier;
    }

    void setPowerMultiplier(final double powerMultiplier) {
        this.powerMultiplier = powerMultiplier;
    }
}
