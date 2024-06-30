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

package appeng.menu.implementations;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.blockentity.storage.MEChestBlockEntity;
import appeng.client.gui.implementations.MEChestScreen;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.RestrictedInputSlot;

/**
 * @see MEChestScreen
 */
public class MEChestMenu extends AEBaseMenu {

    public static final MenuType<MEChestMenu> TYPE = MenuTypeBuilder
            .create(MEChestMenu::new, MEChestBlockEntity.class)
            .build("me_chest");

    public MEChestMenu(int id, Inventory ip, MEChestBlockEntity chest) {
        super(TYPE, id, ip, chest);

        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.STORAGE_CELLS,
                chest.getInternalInventory(), 1), SlotSemantics.STORAGE_CELL);

        this.createPlayerInventorySlots(ip);
    }

}
