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

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.items.IItemHandler;

import appeng.client.gui.Icon;
import appeng.container.AEBaseContainer;
import appeng.container.SlotSemantic;
import appeng.container.slot.InaccessibleSlot;
import appeng.container.slot.OutputSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.tile.grindstone.GrinderTileEntity;

/**
 * @see appeng.client.gui.implementations.GrinderScreen
 */
public class GrinderContainer extends AEBaseContainer {

    public static final MenuType<GrinderContainer> TYPE = ContainerTypeBuilder
            .create(GrinderContainer::new, GrinderTileEntity.class)
            .build("grinder");

    public GrinderContainer(int id, final Inventory ip, final GrinderTileEntity grinder) {
        super(TYPE, id, ip, grinder);

        IItemHandler inv = grinder.getInternalInventory();

        for (int i = 0; i < 3; i++) {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ORE, inv, i),
                    SlotSemantic.MACHINE_INPUT);
        }

        this.addSlot(new InaccessibleSlot(inv, 6), SlotSemantic.MACHINE_PROCESSING);

        for (int i = 0; i < 3; i++) {
            this.addSlot(new OutputSlot(inv, 3 + i, Icon.BACKGROUND_DUST), SlotSemantic.MACHINE_OUTPUT);
        }

        this.createPlayerInventorySlots(ip);
    }

}
