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

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import appeng.api.util.WorldCoord;
import appeng.client.texture.MissingIcon;
import appeng.core.AELog;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

public class ToolEraser extends AEBaseItem
{

	public ToolEraser() {
		super( ToolEraser.class );
		this.setFeature( EnumSet.of( AEFeature.UnsupportedDeveloperTools, AEFeature.Creative ) );
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if ( Platform.isClient() )
			return false;

		Block blk = world.getBlock( x, y, z );
		int meta = world.getBlockMetadata( x, y, z );

		int blocks = 0;
		List<WorldCoord> next = new LinkedList<WorldCoord>();
		next.add( new WorldCoord( x, y, z ) );

		while (blocks < 90000 && !next.isEmpty())
		{

			List<WorldCoord> c = next;
			next = new LinkedList<WorldCoord>();

			for (WorldCoord wc : c)
			{
				Block c_blk = world.getBlock( wc.x, wc.y, wc.z );
				int c_meta = world.getBlockMetadata( wc.x, wc.y, wc.z );

				if ( c_blk == blk && c_meta == meta )
				{
					blocks++;
					world.setBlock( wc.x, wc.y, wc.z, Platform.air );

					this.check( world, wc.x + 1, wc.y, wc.z, next );
					this.check( world, wc.x - 1, wc.y, wc.z, next );
					this.check( world, wc.x, wc.y + 1, wc.z, next );
					this.check( world, wc.x, wc.y - 1, wc.z, next );
					this.check( world, wc.x, wc.y, wc.z + 1, next );
					this.check( world, wc.x, wc.y, wc.z - 1, next );
				}
			}

		}

		AELog.info( "Delete " + blocks + " blocks" );

		return true;
	}

	private void check(World world, int i, int y, int z, List<WorldCoord> next)
	{
		next.add( new WorldCoord( i, y, z ) );
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister)
	{
		this.itemIcon = new MissingIcon( this );
	}

}
