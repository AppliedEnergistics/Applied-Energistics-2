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

package appeng.tile.crafting;


import java.io.IOException;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AEColor;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.util.item.AEItemStack;


public class TileCraftingMonitorTile extends TileCraftingTile implements IColorableTile
{

	@SideOnly( Side.CLIENT )
	public Integer dspList;

	@SideOnly( Side.CLIENT )
	public boolean updateList;

	IAEItemStack dspPlay;
	AEColor paintedColor = AEColor.Transparent;

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean readFromStream_TileCraftingMonitorTile( ByteBuf data ) throws IOException
	{
		AEColor oldPaintedColor = this.paintedColor;
		this.paintedColor = AEColor.values()[data.readByte()];

		boolean hasItem = data.readBoolean();

		if( hasItem )
		{
			this.dspPlay = AEItemStack.loadItemStackFromPacket( data );
		}
		else
		{
			this.dspPlay = null;
		}

		this.updateList = true;
		return oldPaintedColor != this.paintedColor; // tesr!
	}

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToStream_TileCraftingMonitorTile( ByteBuf data ) throws IOException
	{
		data.writeByte( this.paintedColor.ordinal() );

		if( this.dspPlay == null )
		{
			data.writeBoolean( false );
		}
		else
		{
			data.writeBoolean( true );
			this.dspPlay.writeToPacket( data );
		}
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileCraftingMonitorTile( NBTTagCompound data )
	{
		if( data.hasKey( "paintedColor" ) )
		{
			this.paintedColor = AEColor.values()[data.getByte( "paintedColor" )];
		}
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileCraftingMonitorTile( NBTTagCompound data )
	{
		data.setByte( "paintedColor", (byte) this.paintedColor.ordinal() );
	}

	@Override
	public boolean isAccelerator()
	{
		return false;
	}

	@Override
	public boolean isStatus()
	{
		return true;
	}

	public void setJob( IAEStack is )
	{
		if( ( is == null ) != ( this.dspPlay == null ) )
		{
			this.dspPlay = is == null ? null : is.copy();
			this.markForUpdate();
		}
		else if( is != null && this.dspPlay != null )
		{
			if( is.getStackSize() != this.dspPlay.getStackSize() )
			{
				this.dspPlay = is.copy();
				this.markForUpdate();
			}
		}
	}

	public IAEItemStack getJobProgress()
	{
		return this.dspPlay;// AEItemStack.create( new ItemStack( Items.diamond, 64 ) );
	}

	@Override
	public boolean requiresTESR()
	{
		return this.dspPlay != null;
	}

	@Override
	public AEColor getColor()
	{
		return this.paintedColor;
	}

	@Override
	public boolean recolourBlock( ForgeDirection side, AEColor newPaintedColor, EntityPlayer who )
	{
		if( this.paintedColor == newPaintedColor )
		{
			return false;
		}

		this.paintedColor = newPaintedColor;
		this.markDirty();
		this.markForUpdate();
		return true;
	}
}
