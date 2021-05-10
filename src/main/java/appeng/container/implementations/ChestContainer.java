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

import appeng.api.config.SecurityPermissions;
import appeng.container.AEBaseContainer;
import appeng.container.SlotSemantic;
import appeng.container.slot.RestrictedInputSlot;
import appeng.tile.storage.ChestTileEntity;

/**
 * @see appeng.client.gui.implementations.ChestScreen
 */
public class ChestContainer extends AEBaseContainer {

    public static final ContainerType<ChestContainer> TYPE = ContainerTypeBuilder
            .create(ChestContainer::new, ChestTileEntity.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("chest");

    public ChestContainer(int id, final PlayerInventory ip, final ChestTileEntity chest) {
        super(TYPE, id, ip, chest);

        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.STORAGE_CELLS,
                chest.getInternalInventory(), 1), SlotSemantic.STORAGE_CELL);

        this.createPlayerInventorySlots(ip);
    }

}
