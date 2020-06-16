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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.container.ContainerLocator;
import appeng.container.interfaces.IInventorySlotAware;

public class ContainerMEPortableCell extends ContainerMEMonitorable {

    public static ContainerType<ContainerMEPortableCell> TYPE;

    private static final ContainerHelper<ContainerMEPortableCell, IPortableCell> helper = new ContainerHelper<>(
            ContainerMEPortableCell::new, IPortableCell.class);

    public static ContainerMEPortableCell fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private double powerMultiplier = 0.5;

    private final IPortableCell civ;
    private int ticks = 0;
    private final int slot;

    public ContainerMEPortableCell(int id, final PlayerInventory ip, final IPortableCell monitorable) {
        this(TYPE, id, ip, monitorable);
    }

    protected ContainerMEPortableCell(ContainerType<? extends ContainerMEPortableCell> type, int id,
            final PlayerInventory ip, final IPortableCell monitorable) {
        super(type, id, ip, monitorable, false);
        if (monitorable instanceof IInventorySlotAware) {
            final int slotIndex = ((IInventorySlotAware) monitorable).getInventorySlot();
            this.lockPlayerInventorySlot(slotIndex);
            this.slot = slotIndex;
        } else {
            this.slot = -1;
            this.lockPlayerInventorySlot(ip.currentItem);
        }
        this.civ = monitorable;
        this.bindPlayerInventory(ip, 0, 0);
    }

    @Override
    public void detectAndSendChanges() {
        final ItemStack currentItem = this.slot < 0 ? this.getPlayerInv().getCurrentItem()
                : this.getPlayerInv().getStackInSlot(this.slot);

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
            this.civ.extractAEPower(this.getPowerMultiplier() * this.ticks, Actionable.MODULATE,
                    PowerMultiplier.CONFIG);
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
