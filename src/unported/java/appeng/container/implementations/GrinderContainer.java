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
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.network.PacketByteBuf;
import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.slot.InaccessibleSlot;
import appeng.container.slot.OutputSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.tile.grindstone.GrinderBlockEntity;

public class GrinderContainer extends AEBaseContainer {

    public static ScreenHandlerType<GrinderContainer> TYPE;

    private static final ContainerHelper<GrinderContainer, GrinderBlockEntity> helper = new ContainerHelper<>(
            GrinderContainer::new, GrinderBlockEntity.class);

    public static GrinderContainer fromNetwork(int windowId, PlayerInventory inv, PacketByteBuf buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    public GrinderContainer(int id, final PlayerInventory ip, final GrinderBlockEntity grinder) {
        super(TYPE, id, ip, grinder, null);

        FixedItemInv inv = grinder.getInternalInventory();

        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ORE, inv, 0, 12, 17,
                this.getPlayerInventory()));
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ORE, inv, 1, 12 + 18, 17,
                this.getPlayerInventory()));
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ORE, inv, 2, 12 + 36, 17,
                this.getPlayerInventory()));

        this.addSlot(new InaccessibleSlot(inv, 6, 80, 40));

        this.addSlot(new OutputSlot(inv, 3, 112, 63, 2 * 16 + 15));
        this.addSlot(new OutputSlot(inv, 4, 112 + 18, 63, 2 * 16 + 15));
        this.addSlot(new OutputSlot(inv, 5, 112 + 36, 63, 2 * 16 + 15));

        this.bindPlayerInventory(ip, 0, 176 - /* height of player inventory */82);
    }

}
