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


import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.spatial.ISpatialCache;
import appeng.api.util.DimensionalCoord;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;
import appeng.util.Platform;
import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.EnumSet;


public class ToolReplicatorCard extends AEBaseItem
{
	public ToolReplicatorCard()
	{
		this.setFeature( EnumSet.of( AEFeature.UnsupportedDeveloperTools, AEFeature.Creative ) );
	}

	@Override
	public boolean onItemUseFirst( final ItemStack stack, final EntityPlayer player, final World world, int x, int y, int z, final int side, final float hitX, final float hitY, final float hitZ )
	{
		if( ForgeEventFactory.onItemUseStart( player, stack, 1 ) <= 0 )
			return true;

		if( Platform.isClient() )
		{
			return false;
		}

		if( player.isSneaking() )
		{
			if( world.getTileEntity( x, y, z ) instanceof IGridHost )
			{
				final NBTTagCompound tag = new NBTTagCompound();
				tag.setInteger( "x", x );
				tag.setInteger( "y", y );
				tag.setInteger( "z", z );
				tag.setInteger( "side", side );
				tag.setInteger( "dimid", world.provider.dimensionId );
				stack.setTagCompound( tag );
			}
			else
			{
				this.outputMsg( player, "This is not a Grid Tile." );
			}
		}
		else
		{
			final NBTTagCompound ish = stack.getTagCompound();
			if( ish != null )
			{
				final int src_x = ish.getInteger( "x" );
				final int src_y = ish.getInteger( "y" );
				final int src_z = ish.getInteger( "z" );
				final int src_side = ish.getInteger( "side" );
				final int dimid = ish.getInteger( "dimid" );
				final World src_w = DimensionManager.getWorld( dimid );

				final TileEntity te = src_w.getTileEntity( src_x, src_y, src_z );
				if( te instanceof IGridHost )
				{
					final IGridHost gh = (IGridHost) te;
					final ForgeDirection sideOff = ForgeDirection.getOrientation( src_side );
					final ForgeDirection currentSideOff = ForgeDirection.getOrientation( side );
					final IGridNode n = gh.getGridNode( sideOff );
					if( n != null )
					{
						final IGrid g = n.getGrid();
						if( g != null )
						{
							final ISpatialCache sc = g.getCache( ISpatialCache.class );
							if( sc.isValidRegion() )
							{
								final DimensionalCoord min = sc.getMin();
								final DimensionalCoord max = sc.getMax();

								x += currentSideOff.offsetX;
								y += currentSideOff.offsetY;
								z += currentSideOff.offsetZ;

								final int min_x = min.x;
								final int min_y = min.y;
								final int min_z = min.z;

								final int rel_x = min.x - src_x + x;
								final int rel_y = min.y - src_y + y;
								final int rel_z = min.z - src_z + z;

								final int scale_x = max.x - min.x;
								final int scale_y = max.y - min.y;
								final int scale_z = max.z - min.z;

								for( int i = 1; i < scale_x; i++ )
								{
									for( int j = 1; j < scale_y; j++ )
									{
										for( int k = 1; k < scale_z; k++ )
										{
											final Block blk = src_w.getBlock( min_x + i, min_y + j, min_z + k );
											final int meta = src_w.getBlockMetadata( min_x + i, min_y + j, min_z + k );
											world.setBlock( i + rel_x, j + rel_y, k + rel_z, blk, meta, 4 );

											if( blk != null && blk.hasTileEntity( meta ) )
											{
												final TileEntity ote = src_w.getTileEntity( min_x + i, min_y + j, min_z + k );
												final TileEntity nte = blk.createTileEntity( world, meta );
												final NBTTagCompound data = new NBTTagCompound();
												ote.writeToNBT( data );
												nte.readFromNBT( (NBTTagCompound) data.copy() );
												world.setTileEntity( i + rel_x, j + rel_y, k + rel_z, nte );
											}
											world.markBlockForUpdate( i + rel_x, j + rel_y, k + rel_z );
										}
									}
								}
							}
							else
							{
								this.outputMsg( player, "requires valid spatial pylon setup." );
							}
						}
						else
						{
							this.outputMsg( player, "no grid?" );
						}
					}
					else
					{
						this.outputMsg( player, "No grid node?" );
					}
				}
				else
				{
					this.outputMsg( player, "Src is no longer a grid block?" );
				}
			}
			else
			{
				this.outputMsg( player, "No Source Defined" );
			}
		}
		return true;
	}

	private void outputMsg( final ICommandSender player, final String string )
	{
		player.addChatMessage( new ChatComponentText( string ) );
	}
}
