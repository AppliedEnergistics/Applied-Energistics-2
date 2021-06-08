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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;

import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.container.AEBaseContainer;
import appeng.container.SlotSemantic;
import appeng.container.slot.AppEngSlot;
import appeng.tile.storage.SkyChestTileEntity;

/**
 * @see appeng.client.gui.implementations.SkyChestScreen
 */
public class SkyChestContainer extends AEBaseContainer {

    public static final ContainerType<SkyChestContainer> TYPE = ContainerTypeBuilder
            .create(SkyChestContainer::new, SkyChestTileEntity.class)
            .build("skychest");

    private final SkyChestTileEntity chest;

    public SkyChestContainer(int id, final PlayerInventory ip, final SkyChestTileEntity chest) {
        super(TYPE, id, ip, chest);
        this.chest = chest;

        FixedItemInv inv = this.chest.getInternalInventory();
        for (int i = 0; i < inv.getSlotCount(); i++) {
            this.addSlot(new AppEngSlot(inv, i), SlotSemantic.STORAGE);
        }

        this.chest.openInventory(ip.player);

        this.createPlayerInventorySlots(ip);
    }

    @Override
    public void onContainerClosed(final PlayerEntity par1PlayerEntity) {
        super.onContainerClosed(par1PlayerEntity);
        this.chest.closeInventory(par1PlayerEntity);
    }
}
