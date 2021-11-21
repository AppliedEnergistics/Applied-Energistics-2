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

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.implementations.menuobjects.IPortableCell;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.fluids.FluidTerminalMenu;

/**
 * @see appeng.client.gui.me.fluids.FluidTerminalScreen
 */
public class PortableFluidCellMenu extends FluidTerminalMenu {

    public static final MenuType<PortableFluidCellMenu> TYPE = MenuTypeBuilder
            .create(PortableFluidCellMenu::new, IPortableCell.class)
            .build("portable_fluid_cell");

    protected PortableFluidCellMenu(MenuType<? extends PortableFluidCellMenu> type, int id,
            Inventory ip, IPortableCell monitorable) {
        super(type, id, ip, monitorable, false);
        this.createPlayerInventorySlots(ip);
    }
}
