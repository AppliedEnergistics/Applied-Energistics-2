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

package appeng.container.implementations;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.items.IItemHandler;

import appeng.container.AEBaseContainer;
import appeng.container.SlotSemantic;
import appeng.container.slot.AppEngSlot;
import appeng.tile.storage.SkyChestBlockEntity;

/**
 * @see appeng.client.gui.implementations.SkyChestScreen
 */
public class SkyChestContainer extends AEBaseContainer {

    public static final MenuType<SkyChestContainer> TYPE = ContainerTypeBuilder
            .create(SkyChestContainer::new, SkyChestBlockEntity.class)
            .build("skychest");

    private final SkyChestBlockEntity chest;

    public SkyChestContainer(int id, final Inventory ip, final SkyChestBlockEntity chest) {
        super(TYPE, id, ip, chest);

        this.chest = chest;
        chest.startOpen(ip.player);

        IItemHandler inv = chest.getInternalInventory();
        for (int i = 0; i < inv.getSlots(); i++) {
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
