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

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.spatial.ISpatialCache;
import appeng.api.util.DimensionalCoord;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

public class ToolReplicatorCard extends AEBaseItem
{

	public ToolReplicatorCard() {
		super( ToolReplicatorCard.class );
		this.setFeature( EnumSet.of( AEFeature.UnsupportedDeveloperTools, AEFeature.Creative ) );
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if ( Platform.isClient() )
			return false;

		if ( player.isSneaking() )
		{
			if ( world.getTileEntity( x, y, z ) instanceof IGridHost )
			{
				NBTTagCompound tag = new NBTTagCompound();
				tag.setInteger( "x", x );
				tag.setInteger( "y", y );
				tag.setInteger( "z", z );
				tag.setInteger( "side", side );
				tag.setInteger( "dimid", world.provider.dimensionId );
				stack.setTagCompound( tag );
			}
			else
				this.outputMsg( player, "This is not a Grid Tile." );
		}
		else
		{
			NBTTagCompound ish = stack.getTagCompound();
			if ( ish != null )
			{
				int src_x = ish.getInteger( "x" );
				int src_y = ish.getInteger( "y" );
				int src_z = ish.getInteger( "z" );
				int src_side = ish.getInteger( "side" );
				int dimid = ish.getInteger( "dimid" );
				World src_w = DimensionManager.getWorld( dimid );

				TileEntity te = src_w.getTileEntity( src_x, src_y, src_z );
				if ( te instanceof IGridHost )
				{
					IGridHost gh = (IGridHost) te;
					ForgeDirection sideOff = ForgeDirection.getOrientation( src_side );
					ForgeDirection currentSideOff = ForgeDirection.getOrientation( side );
					IGridNode n = gh.getGridNode( sideOff );
					if ( n != null )
					{
						IGrid g = n.getGrid();
						if ( g != null )
						{
							ISpatialCache sc = g.getCache( ISpatialCache.class );
							if ( sc.isValidRegion() )
							{
								DimensionalCoord min = sc.getMin();
								DimensionalCoord max = sc.getMax();

								x += currentSideOff.offsetX;
								y += currentSideOff.offsetY;
								z += currentSideOff.offsetZ;

								int min_x = min.x;
								int min_y = min.y;
								int min_z = min.z;

								int rel_x = min.x - src_x + x;
								int rel_y = min.y - src_y + y;
								int rel_z = min.z - src_z + z;

								int scale_x = max.x - min.x;
								int scale_y = max.y - min.y;
								int scale_z = max.z - min.z;

								for (int i = 1; i < scale_x; i++)
									for (int j = 1; j < scale_y; j++)
										for (int k = 1; k < scale_z; k++)
										{
											Block blk = src_w.getBlock( min_x + i, min_y + j, min_z + k );
											int meta = src_w.getBlockMetadata( min_x + i, min_y + j, min_z + k );
											world.setBlock( i + rel_x, j + rel_y, k + rel_z, blk, meta, 4 );

											if ( blk != null && blk.hasTileEntity( meta ) )
											{
												TileEntity ote = src_w.getTileEntity( min_x + i, min_y + j, min_z + k );
												TileEntity nte = blk.createTileEntity( world, meta );
												NBTTagCompound data = new NBTTagCompound();
												ote.writeToNBT( data );
												nte.readFromNBT( (NBTTagCompound) data.copy() );
												world.setTileEntity( i + rel_x, j + rel_y, k + rel_z, nte );
											}
											world.markBlockForUpdate( i + rel_x, j + rel_y, k + rel_z );
										}

							}
							else
								this.outputMsg( player, "requires valid spatial pylon setup." );
						}
						else
							this.outputMsg( player, "no grid?" );
					}
					else
						this.outputMsg( player, "No grid node?" );
				}
				else
					this.outputMsg( player, "Src is no longer a grid block?" );
			}
			else
				this.outputMsg( player, "No Source Defined" );
		}
		return true;
	}

	private void outputMsg(EntityPlayer player, String string)
	{
		player.addChatMessage( new ChatComponentText( string ) );
	}

}
