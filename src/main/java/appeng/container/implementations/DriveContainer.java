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
import net.minecraft.network.PacketBuffer;

import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.slot.RestrictedInputSlot;
import appeng.tile.storage.DriveTileEntity;

public class DriveContainer extends AEBaseContainer {

    public static ContainerType<DriveContainer> TYPE;

    private static final ContainerHelper<DriveContainer, DriveTileEntity> helper = new ContainerHelper<>(
            DriveContainer::new, DriveTileEntity.class);

    public static DriveContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    public DriveContainer(int id, final PlayerInventory ip, final DriveTileEntity drive) {
        super(TYPE, id, ip, drive, null);

        for (int y = 0; y < 5; y++) {
            int slotY = 14 + y * 18;
            for (int x = 0; x < 2; x++) {
                int invSlot = x + y * 2;
                int slotX = 71 + x * 18;
                this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.STORAGE_CELLS,
                        drive.getInternalInventory(), invSlot, slotX, slotY, this.getPlayerInventory()));
            }
        }

        this.createPlayerInventorySlots(ip);
    }

}
