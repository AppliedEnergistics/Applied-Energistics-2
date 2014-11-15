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

import appeng.container.slot.SlotInaccessible;
import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.grindstone.TileGrinder;

public class ContainerGrinder extends AEBaseContainer
{

	final TileGrinder grinder;

	public ContainerGrinder(InventoryPlayer ip, TileGrinder grinder) {
		super( ip, grinder, null );
		this.grinder = grinder;

		addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.ORE, grinder, 0, 12, 17, invPlayer ) );
		addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.ORE, grinder, 1, 12 + 18, 17, invPlayer ) );
		addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.ORE, grinder, 2, 12 + 36, 17, invPlayer ) );

		addSlotToContainer( new SlotInaccessible( grinder, 6, 80, 40 ) );

		addSlotToContainer( new SlotOutput( grinder, 3, 112, 63, 2 * 16 + 15 ) );
		addSlotToContainer( new SlotOutput( grinder, 4, 112 + 18, 63, 2 * 16 + 15 ) );
		addSlotToContainer( new SlotOutput( grinder, 5, 112 + 36, 63, 2 * 16 + 15 ) );

		bindPlayerInventory( ip, 0, 176 - /* height of player inventory */82 );
	}

}
