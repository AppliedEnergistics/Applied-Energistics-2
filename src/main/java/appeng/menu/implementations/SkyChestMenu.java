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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import appeng.blockentity.storage.SkyChestBlockEntity;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import appeng.menu.slot.AppEngSlot;

/**
 * @see appeng.client.gui.implementations.SkyChestScreen
 */
public class SkyChestMenu extends AEBaseMenu {

    public static final MenuType<SkyChestMenu> TYPE = MenuTypeBuilder
            .create(SkyChestMenu::new, SkyChestBlockEntity.class)
            .build("skychest");

    private final SkyChestBlockEntity chest;

    public SkyChestMenu(int id, final Inventory ip, final SkyChestBlockEntity chest) {
        super(TYPE, id, ip, chest);

        this.chest = chest;
        chest.startOpen(ip.player);

        var inv = chest.getInternalInventory();
        for (int i = 0; i < inv.size(); i++) {
            this.addSlot(new AppEngSlot(inv, i), SlotSemantic.STORAGE);
        }

        this.createPlayerInventorySlots(ip);
    }

    public void removed(Player player) {
        super.removed(player);
        this.chest.stopOpen(player);
    }

    public SkyChestBlockEntity getChest() {
        return chest;
    }
}
