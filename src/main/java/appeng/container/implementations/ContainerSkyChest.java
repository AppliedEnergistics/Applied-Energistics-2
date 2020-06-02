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


import appeng.core.AppEng;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;

import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotNormal;
import appeng.tile.storage.TileSkyChest;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkHooks;


public class ContainerSkyChest extends AEBaseContainer
{
	public static ContainerType<ContainerSkyChest> TYPE;

	private final TileSkyChest chest;

	public ContainerSkyChest(int id, final PlayerInventory ip, final TileSkyChest chest )
	{
		super( TYPE, id, ip, chest, null );
		this.chest = chest;

		for( int y = 0; y < 4; y++ )
		{
			for( int x = 0; x < 9; x++ )
			{
				this.addSlot( new SlotNormal( this.chest.getInternalInventory(), y * 9 + x, 8 + 18 * x, 24 + 18 * y ) );
			}
		}

		this.chest.openInventory( ip.player );

		this.bindPlayerInventory( ip, 0, 195 - /* height of player inventory */82 );
	}

	public static ContainerSkyChest fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
		BlockPos pos = buf.readBlockPos();
		TileEntity te = inv.player.world.getTileEntity(pos);
		if (te instanceof TileSkyChest) {
			return new ContainerSkyChest(windowId, inv, (TileSkyChest) te);
		}
		return null;
	}

	public static void open(ServerPlayerEntity player, TileSkyChest tile, ITextComponent title) {
		BlockPos pos = tile.getPos();
		INamedContainerProvider container = new SimpleNamedContainerProvider(
				(wnd, p, pl) -> new ContainerSkyChest(wnd, p, tile), title
		);
		NetworkHooks.openGui(player, container, pos);
	}

	@Override
	public void onContainerClosed( final PlayerEntity par1PlayerEntity )
	{
		super.onContainerClosed( par1PlayerEntity );
		this.chest.closeInventory( par1PlayerEntity );
	}
}
