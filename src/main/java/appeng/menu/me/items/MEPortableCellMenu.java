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

package appeng.menu.me.items;

import java.util.Objects;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.interfaces.IInventorySlotAware;

/**
 * @see appeng.client.gui.me.items.ItemTerminalScreen
 */
public class MEPortableCellMenu extends ItemTerminalMenu {

    public static final MenuType<MEPortableCellMenu> TYPE = MenuTypeBuilder
            .create(MEPortableCellMenu::new, IPortableCell.class)
            .build("meportablecell");

    private final IPortableCell cell;
    private final int slot;
    private int ticks = 0;
    private double powerMultiplier = 0.5;

    protected MEPortableCellMenu(MenuType<? extends MEPortableCellMenu> type, int id,
                                 final Inventory ip, final IPortableCell monitorable) {
        super(type, id, ip, monitorable, false);
        // Is the screen being opened a specific slot? If not, it must be for the currently held item
        if (monitorable instanceof IInventorySlotAware) {
            this.slot = ((IInventorySlotAware) monitorable).getInventorySlot();
        } else {
            this.slot = ip.selected;
        }
        this.lockPlayerInventorySlot(this.slot);
        this.cell = Objects.requireNonNull(monitorable);
        this.createPlayerInventorySlots(ip);
    }

    @Override
    public void broadcastChanges() {
        if (!ensureGuiItemIsInSlot(this.cell, this.slot)) {
            this.setValidMenu(false);
            return;
        }

        // drain 1 ae t
        this.ticks++;
        if (this.ticks > 10) {
            this.cell.extractAEPower(this.getPowerMultiplier() * this.ticks, Actionable.MODULATE,
                    PowerMultiplier.CONFIG);
            this.ticks = 0;
        }
        super.broadcastChanges();
    }

    private double getPowerMultiplier() {
        return this.powerMultiplier;
    }

    void setPowerMultiplier(final double powerMultiplier) {
        this.powerMultiplier = powerMultiplier;
    }
}
