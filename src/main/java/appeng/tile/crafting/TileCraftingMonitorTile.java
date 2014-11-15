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

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEColor;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileCraftingMonitorTile extends TileCraftingTile implements IColorableTile
{

	@SideOnly(Side.CLIENT)
	public Integer dspList;

	@SideOnly(Side.CLIENT)
	public boolean updateList;

	IAEItemStack dspPlay;
	AEColor paintedColor = AEColor.Transparent;

	@TileEvent(TileEventType.NETWORK_READ)
	public boolean readFromStream_TileCraftingMonitorTile(ByteBuf data) throws IOException
	{
		AEColor oldPaintedColor = paintedColor;
		paintedColor = AEColor.values()[data.readByte()];

		boolean hasItem = data.readBoolean();

		if ( hasItem )
			dspPlay = AEItemStack.loadItemStackFromPacket( data );
		else
			dspPlay = null;

		updateList = true;
		return oldPaintedColor != paintedColor; // tesr!
	}

	@TileEvent(TileEventType.NETWORK_WRITE)
	public void writeToStream_TileCraftingMonitorTile(ByteBuf data) throws IOException
	{
		data.writeByte( paintedColor.ordinal() );

		if ( dspPlay == null )
			data.writeBoolean( false );
		else
		{
			data.writeBoolean( true );
			dspPlay.writeToPacket( data );
		}
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void readFromNBT_TileCraftingMonitorTile(NBTTagCompound data)
	{
		if ( data.hasKey( "paintedColor" ) )
			paintedColor = AEColor.values()[data.getByte( "paintedColor" )];
	}

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void writeToNBT_TileCraftingMonitorTile(NBTTagCompound data)
	{
		data.setByte( "paintedColor", (byte) paintedColor.ordinal() );
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

	public void setJob(IAEItemStack is)
	{
		if ( (is == null) != (dspPlay == null) )
		{
			dspPlay = is == null ? null : is.copy();
			markForUpdate();
		}
		else if ( is != null && dspPlay != null )
		{
			if ( is.getStackSize() != dspPlay.getStackSize() )
			{
				dspPlay = is.copy();
				markForUpdate();
			}
		}
	}

	public IAEItemStack getJobProgress()
	{
		return dspPlay;// AEItemStack.create( new ItemStack( Items.diamond, 64 ) );
	}

	@Override
	public boolean requiresTESR()
	{
		return getJobProgress() != null;
	}

	@Override
	public AEColor getColor()
	{
		return paintedColor;
	}

	@Override
	public boolean recolourBlock(ForgeDirection side, AEColor newPaintedColor, EntityPlayer who)
	{
		if ( paintedColor == newPaintedColor )
			return false;

		paintedColor = newPaintedColor;
		markDirty();
		markForUpdate();
		return true;
	}
}
