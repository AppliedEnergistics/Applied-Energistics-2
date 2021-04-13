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
import appeng.container.slot.InaccessibleSlot;
import appeng.container.slot.OutputSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.tile.grindstone.GrinderTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;

/**
 * @see appeng.client.gui.implementations.GrinderScreen
 */
public class GrinderContainer extends AEBaseContainer {

    public static ContainerType<GrinderContainer> TYPE;

    private static final ContainerHelper<GrinderContainer, GrinderTileEntity> helper = new ContainerHelper<>(
            GrinderContainer::new, GrinderTileEntity.class);

    public static GrinderContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    public GrinderContainer(int id, final PlayerInventory ip, final GrinderTileEntity grinder) {
        super(TYPE, id, ip, grinder, null);

        IItemHandler inv = grinder.getInternalInventory();

        for (int i = 0; i < 3; i++) {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ORE, inv, i), SlotSemantic.MACHINE_INPUT);
        }

        this.addSlot(new InaccessibleSlot(inv, 6), SlotSemantic.MACHINE_PROCESSING);

        for (int i = 0; i < 3; i++) {
            this.addSlot(new OutputSlot(inv, 3 + i, 2 * 16 + 15), SlotSemantic.MACHINE_OUTPUT);
        }

        this.createPlayerInventorySlots(ip);
    }

}
