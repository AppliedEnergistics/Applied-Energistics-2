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

import appeng.api.config.SecurityPermissions;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.implementations.ContainerHelper;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.storage.TileChest;

public class ContainerChest extends AEBaseContainer {

    public static ContainerType<ContainerChest> TYPE;

    private static final ContainerHelper<ContainerChest, TileChest> helper = new ContainerHelper<>(ContainerChest::new,
            TileChest.class, SecurityPermissions.BUILD);

    public ContainerChest(int id, final PlayerInventory ip, final TileChest chest) {
        super(TYPE, id, ip, chest, null);

        this.addSlot(new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.STORAGE_CELLS,
                chest.getInternalInventory(), 1, 80, 37, this.getPlayerInventory()));

        this.bindPlayerInventory(ip, 0, 166 - /* height of player inventory */82);
    }

    public static ContainerChest fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

}
