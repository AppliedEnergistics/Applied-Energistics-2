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


import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.storage.TileDrive;
import net.minecraft.entity.player.InventoryPlayer;


public class ContainerDrive extends AEBaseContainer {

    public ContainerDrive(final InventoryPlayer ip, final TileDrive drive) {
        super(ip, drive, null);

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 2; x++) {
                this.addSlotToContainer(new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.STORAGE_CELLS, drive
                        .getInternalInventory(), x + y * 2, 71 + x * 18, 14 + y * 18, this.getInventoryPlayer()));
            }
        }

        this.bindPlayerInventory(ip, 0, 199 - /* height of player inventory */82);
    }
}
