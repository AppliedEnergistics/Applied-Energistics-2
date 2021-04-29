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

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;

import appeng.container.AEBaseContainer;
import appeng.container.SlotSemantic;
import appeng.container.slot.RestrictedInputSlot;
import appeng.tile.storage.DriveTileEntity;

/**
 * @see appeng.client.gui.implementations.DriveScreen
 */
public class DriveContainer extends AEBaseContainer {

    public static final ContainerType<DriveContainer> TYPE = ContainerTypeBuilder
            .create(DriveContainer::new, DriveTileEntity.class)
            .build("drive");

    public DriveContainer(int id, final PlayerInventory ip, final DriveTileEntity drive) {
        super(TYPE, id, ip, drive);

        for (int i = 0; i < 10; i++) {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.STORAGE_CELLS,
                    drive.getInternalInventory(), i), SlotSemantic.STORAGE_CELL);
        }

        this.createPlayerInventorySlots(ip);
    }

}
