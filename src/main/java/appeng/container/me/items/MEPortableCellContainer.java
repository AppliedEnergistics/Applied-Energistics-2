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

package appeng.container.me.items;

import java.util.Objects;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.container.ContainerLocator;
import appeng.container.implementations.ContainerHelper;
import appeng.container.interfaces.IInventorySlotAware;

public class MEPortableCellContainer extends MEMonitorableContainer {

    public static ContainerType<MEPortableCellContainer> TYPE;

    private static final ContainerHelper<MEPortableCellContainer, IPortableCell> helper = new ContainerHelper<>(
            MEPortableCellContainer::new, IPortableCell.class);

    public static MEPortableCellContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private final IPortableCell cell;
    private final int slot;
    private int ticks = 0;
    private double powerMultiplier = 0.5;

    public MEPortableCellContainer(int id, final PlayerInventory ip, final IPortableCell monitorable) {
        this(TYPE, id, ip, monitorable);
    }

    protected MEPortableCellContainer(ContainerType<? extends MEPortableCellContainer> type, int id,
            final PlayerInventory ip, final IPortableCell monitorable) {
        super(type, id, ip, monitorable, false);
        // Is the screen being opened a specific slot? If not, it must be for the currently held item
        if (monitorable instanceof IInventorySlotAware) {
            this.slot = ((IInventorySlotAware) monitorable).getInventorySlot();
        } else {
            this.slot = ip.currentItem;
        }
        this.lockPlayerInventorySlot(this.slot);
        this.cell = Objects.requireNonNull(monitorable);
        this.bindPlayerInventory(ip, 0, 0);
    }

    @Override
    public void detectAndSendChanges() {
        if (!ensureGuiItemIsInSlot(this.cell, this.slot)) {
            this.setValidContainer(false);
            return;
        }

        // drain 1 ae t
        this.ticks++;
        if (this.ticks > 10) {
            this.cell.extractAEPower(this.getPowerMultiplier() * this.ticks, Actionable.MODULATE,
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
