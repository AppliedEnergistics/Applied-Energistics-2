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

package appeng.debug;


import appeng.api.util.WorldCoord;
import appeng.client.texture.MissingIcon;
import appeng.core.AELog;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;
import appeng.util.Platform;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;


public class ToolEraser extends AEBaseItem
{

	private static final int BLOCK_ERASE_LIMIT = 90000;

	public ToolEraser()
	{
		this.setFeature( EnumSet.of( AEFeature.UnsupportedDeveloperTools, AEFeature.Creative ) );
	}

	@Override
	public void registerIcons( final IIconRegister par1IconRegister )
	{
		this.itemIcon = new MissingIcon( this );
	}

	@Override
	public boolean onItemUseFirst( final ItemStack stack, final EntityPlayer player, final World world, final int x, final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ )
	{
		if( ForgeEventFactory.onItemUseStart( player, stack, 1 ) <= 0 )
			return true;

		if( Platform.isClient() )
		{
			return false;
		}

		final Block blk = world.getBlock( x, y, z );
		final int meta = world.getBlockMetadata( x, y, z );

		if( blk != null && ForgeEventFactory.onPlayerInteract( player, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, x, y, z, side, world ).isCanceled() )
			return true;

		List<WorldCoord> next = new LinkedList<WorldCoord>();
		next.add( new WorldCoord( x, y, z ) );

		int blocks = 0;
		while( blocks < BLOCK_ERASE_LIMIT && !next.isEmpty() )
		{
			final List<WorldCoord> c = next;
			next = new LinkedList<WorldCoord>();

			for( final WorldCoord wc : c )
			{
				final Block c_blk = world.getBlock( wc.x, wc.y, wc.z );
				final int c_meta = world.getBlockMetadata( wc.x, wc.y, wc.z );

				if( c_blk == blk && c_meta == meta )
				{
					blocks++;
					world.setBlock( wc.x, wc.y, wc.z, Platform.AIR_BLOCK );

					this.wrappedAdd( world, wc.x + 1, wc.y, wc.z, next );
					this.wrappedAdd( world, wc.x - 1, wc.y, wc.z, next );
					this.wrappedAdd( world, wc.x, wc.y + 1, wc.z, next );
					this.wrappedAdd( world, wc.x, wc.y - 1, wc.z, next );
					this.wrappedAdd( world, wc.x, wc.y, wc.z + 1, next );
					this.wrappedAdd( world, wc.x, wc.y, wc.z - 1, next );
				}
			}
		}

		AELog.info( "Delete " + blocks + " blocks" );

		return true;
	}

	private void wrappedAdd( final World world, final int i, final int y, final int z, final Collection<WorldCoord> next )
	{
		next.add( new WorldCoord( i, y, z ) );
	}
}
