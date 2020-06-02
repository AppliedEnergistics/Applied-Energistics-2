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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;

import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotInaccessible;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.grindstone.TileGrinder;

public class ContainerGrinder extends AEBaseContainer
{

	public static ContainerType<ContainerGrinder> TYPE;

	public ContainerGrinder(int id, final PlayerInventory ip, final TileGrinder grinder )
	{
		super( TYPE, id, ip, grinder, null );

		IItemHandler inv = grinder.getInternalInventory();

		this.addSlot( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.ORE, inv, 0, 12, 17, this.getPlayerInventory() ) );
		this.addSlot( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.ORE, inv, 1, 12 + 18, 17, this.getPlayerInventory() ) );
		this.addSlot( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.ORE, inv, 2, 12 + 36, 17, this.getPlayerInventory() ) );

		this.addSlot( new SlotInaccessible( inv, 6, 80, 40 ) );

		this.addSlot( new SlotOutput( inv, 3, 112, 63, 2 * 16 + 15 ) );
		this.addSlot( new SlotOutput( inv, 4, 112 + 18, 63, 2 * 16 + 15 ) );
		this.addSlot( new SlotOutput( inv, 5, 112 + 36, 63, 2 * 16 + 15 ) );

		this.bindPlayerInventory( ip, 0, 176 - /* height of player inventory */82 );
	}

	public static ContainerGrinder fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
		BlockPos pos = buf.readBlockPos();
		TileEntity te = inv.player.world.getTileEntity(pos);
		if (te instanceof TileGrinder) {
			return new ContainerGrinder(windowId, inv, (TileGrinder) te);
		}
		return null;
	}

	public static void open(ServerPlayerEntity player, TileGrinder tile, ITextComponent title) {
		BlockPos pos = tile.getPos();
		INamedContainerProvider container = new SimpleNamedContainerProvider(
				(wnd, p, pl) -> new ContainerGrinder(wnd, p, tile), title
		);
		NetworkHooks.openGui(player, container, pos);
	}

}
