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

package appeng.tile.misc;


import appeng.api.util.AEColor;
import appeng.helpers.Splotch;
import appeng.items.misc.ItemPaintBall;
import appeng.tile.AEBaseTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


public class TilePaint extends AEBaseTile
{

	private static final int LIGHT_PER_DOT = 12;

	private int isLit = 0;
	private List<Splotch> dots = null;

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TilePaint( final NBTTagCompound data )
	{
		final ByteBuf myDat = Unpooled.buffer();
		this.writeBuffer( myDat );
		if( myDat.hasArray() )
		{
			data.setByteArray( "dots", myDat.array() );
		}
	}

	private void writeBuffer( final ByteBuf out )
	{
		if( this.dots == null )
		{
			out.writeByte( 0 );
			return;
		}

		out.writeByte( this.dots.size() );

		for( final Splotch s : this.dots )
		{
			s.writeToStream( out );
		}
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TilePaint( final NBTTagCompound data )
	{
		if( data.hasKey( "dots" ) )
		{
			this.readBuffer( Unpooled.copiedBuffer( data.getByteArray( "dots" ) ) );
		}
	}

	private void readBuffer( final ByteBuf in )
	{
		final byte howMany = in.readByte();

		if( howMany == 0 )
		{
			this.isLit = 0;
			this.dots = null;
			return;
		}

		this.dots = new ArrayList( howMany );
		for( int x = 0; x < howMany; x++ )
		{
			this.dots.add( new Splotch( in ) );
		}

		this.isLit = 0;
		for( final Splotch s : this.dots )
		{
			if( s.isLumen() )
			{
				this.isLit += LIGHT_PER_DOT;
			}
		}

		this.maxLit();
	}

	private void maxLit()
	{
		if( this.isLit > 14 )
		{
			this.isLit = 14;
		}

		if( this.worldObj != null )
		{
			this.worldObj.updateLightByType( EnumSkyBlock.Block, this.xCoord, this.yCoord, this.zCoord );
		}
	}

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToStream_TilePaint( final ByteBuf data )
	{
		this.writeBuffer( data );
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean readFromStream_TilePaint( final ByteBuf data )
	{
		this.readBuffer( data );
		return true;
	}

	public void onNeighborBlockChange()
	{
		if( this.dots == null )
		{
			return;
		}

		for( final ForgeDirection side : ForgeDirection.VALID_DIRECTIONS )
		{
			if( !this.isSideValid( side ) )
			{
				this.removeSide( side );
			}
		}

		this.updateData();
	}

	public boolean isSideValid( final ForgeDirection side )
	{
		final Block blk = this.worldObj.getBlock( this.xCoord + side.offsetX, this.yCoord + side.offsetY, this.zCoord + side.offsetZ );
		return blk.isSideSolid( this.worldObj, this.xCoord + side.offsetX, this.yCoord + side.offsetY, this.zCoord + side.offsetZ, side.getOpposite() );
	}

	private void removeSide( final ForgeDirection side )
	{
		final Iterator<Splotch> i = this.dots.iterator();
		while( i.hasNext() )
		{
			final Splotch s = i.next();
			if( s.getSide() == side )
			{
				i.remove();
			}
		}

		this.markForUpdate();
		this.markDirty();
	}

	private void updateData()
	{
		this.isLit = 0;
		for( final Splotch s : this.dots )
		{
			if( s.isLumen() )
			{
				this.isLit += LIGHT_PER_DOT;
			}
		}

		this.maxLit();

		if( this.dots.isEmpty() )
		{
			this.dots = null;
		}

		if( this.dots == null )
		{
			this.worldObj.setBlock( this.xCoord, this.yCoord, this.zCoord, Blocks.air );
		}
	}

	public void cleanSide( final ForgeDirection side )
	{
		if( this.dots == null )
		{
			return;
		}

		this.removeSide( side );

		this.updateData();
	}

	public int getLightLevel()
	{
		return this.isLit;
	}

	public void addBlot( final ItemStack type, final ForgeDirection side, final Vec3 hitVec )
	{
		final Block blk = this.worldObj.getBlock( this.xCoord + side.offsetX, this.yCoord + side.offsetY, this.zCoord + side.offsetZ );
		if( blk.isSideSolid( this.worldObj, this.xCoord + side.offsetX, this.yCoord + side.offsetY, this.zCoord + side.offsetZ, side.getOpposite() ) )
		{
			final ItemPaintBall ipb = (ItemPaintBall) type.getItem();

			final AEColor col = ipb.getColor( type );
			final boolean lit = ipb.isLumen( type );

			if( this.dots == null )
			{
				this.dots = new ArrayList<Splotch>();
			}

			if( this.dots.size() > 20 )
			{
				this.dots.remove( 0 );
			}

			this.dots.add( new Splotch( col, lit, side, hitVec ) );
			if( lit )
			{
				this.isLit += LIGHT_PER_DOT;
			}

			this.maxLit();
			this.markForUpdate();
			this.markDirty();
		}
	}

	public Collection<Splotch> getDots()
	{
		if( this.dots == null )
		{
			return ImmutableList.of();
		}

		return this.dots;
	}
}
