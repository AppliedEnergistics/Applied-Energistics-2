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


import appeng.api.config.SecurityPermissions;
import appeng.container.ContainerLocator;
import appeng.container.helper.TileContainerHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;

import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.qnb.TileQuantumBridge;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;


public class ContainerQNB extends AEBaseContainer
{

public static ContainerType<ContainerQNB> TYPE;

	private static final TileContainerHelper<ContainerQNB, TileQuantumBridge> helper
			= new TileContainerHelper<>(ContainerQNB::new, TileQuantumBridge.class, SecurityPermissions.BUILD);

	public static ContainerQNB fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
		return helper.fromNetwork(windowId, inv, buf);
	}

	public static boolean open(PlayerEntity player, ContainerLocator locator) {
		return helper.open(player, locator);
	}

	public ContainerQNB(int id, final PlayerInventory ip, final TileQuantumBridge quantumBridge )
	{
		super( TYPE, id,  ip, quantumBridge, null );

		this.addSlot( ( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.QE_SINGULARITY, quantumBridge
				.getInternalInventory(), 0, 80, 37, this.getPlayerInventory() ) ).setStackLimit( 1 ) );

		this.bindPlayerInventory( ip, 0, 166 - /* height of player inventory */82 );
	}

}
