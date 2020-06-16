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
import appeng.container.implementations.ContainerHelper;
import appeng.container.slot.SlotNormal;
import appeng.tile.storage.TileSkyChest;

public class ContainerSkyChest extends AEBaseContainer {

    public static ContainerType<ContainerSkyChest> TYPE;

    private static final ContainerHelper<ContainerSkyChest, TileSkyChest> helper = new ContainerHelper<>(
            ContainerSkyChest::new, TileSkyChest.class);

    private final TileSkyChest chest;

    public ContainerSkyChest(int id, final PlayerInventory ip, final TileSkyChest chest) {
        super(TYPE, id, ip, chest, null);
        this.chest = chest;

        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(new SlotNormal(this.chest.getInternalInventory(), y * 9 + x, 8 + 18 * x, 24 + 18 * y));
            }
        }

        this.chest.openInventory(ip.player);

        this.bindPlayerInventory(ip, 0, 195 - /* height of player inventory */82);
    }

    public static ContainerSkyChest fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    @Override
    public void onContainerClosed(final PlayerEntity par1PlayerEntity) {
        super.onContainerClosed(par1PlayerEntity);
        this.chest.closeInventory(par1PlayerEntity);
    }
}
