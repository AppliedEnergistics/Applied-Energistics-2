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

package appeng.block.storage;


import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.storage.ICellHandler;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderMEChest;
import appeng.core.features.AEFeature;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.GuiBridge;
import appeng.tile.storage.TileChest;
import appeng.util.Platform;


public class BlockChest extends AEBaseBlock
{

	public BlockChest()
	{
		super( Material.iron );
		this.setTileEntity( TileChest.class );
		this.setFeature( EnumSet.of( AEFeature.StorageCells, AEFeature.MEChest ) );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderMEChest.class;
	}

	@Override
	public boolean onActivated( World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ )
	{
		TileChest tg = this.getTileEntity( w, x, y, z );
		if( tg != null && !p.isSneaking() )
		{
			if( Platform.isClient() )
			{
				return true;
			}

			if( side != tg.getUp().ordinal() )
			{
				Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_CHEST );
			}
			else
			{
				ItemStack cell = tg.getStackInSlot( 1 );
				if( cell != null )
				{
					ICellHandler ch = AEApi.instance().registries().cell().getHandler( cell );

					tg.openGui( p, ch, cell, side );
				}
				else
				{
					p.addChatMessage( PlayerMessages.ChestCannotReadStorageCell.get() );
				}
			}

			return true;
		}

		return false;
	}
}
