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
import appeng.container.ContainerLocator;
import appeng.container.SlotSemantic;
import appeng.container.slot.AppEngSlot;
import appeng.tile.storage.SkyChestTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;

/**
 * @see appeng.client.gui.implementations.SkyChestScreen
 */
public class SkyChestContainer extends AEBaseContainer {

    public static ContainerType<SkyChestContainer> TYPE;

    private static final ContainerHelper<SkyChestContainer, SkyChestTileEntity> helper = new ContainerHelper<>(
            SkyChestContainer::new, SkyChestTileEntity.class);

    private final SkyChestTileEntity chest;

    public SkyChestContainer(int id, final PlayerInventory ip, final SkyChestTileEntity chest) {
        super(TYPE, id, ip, chest, null);
        this.chest = chest;

        IItemHandler inv = this.chest.getInternalInventory();
        for (int i = 0; i < inv.getSlots(); i++) {
            this.addSlot(new AppEngSlot(inv, i), SlotSemantic.STORAGE);
        }

        this.chest.openInventory(ip.player);

        this.createPlayerInventorySlots(ip);
    }

    public static SkyChestContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
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
