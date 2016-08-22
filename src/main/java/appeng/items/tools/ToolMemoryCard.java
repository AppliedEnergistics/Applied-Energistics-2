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

package appeng.items.tools;


import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.items.AEBaseItem;
import appeng.util.Platform;


public class ToolMemoryCard extends AEBaseItem implements IMemoryCard
{
	public ToolMemoryCard()
	{
		this.setMaxStackSize( 1 );
	}

	@Override
	public void addCheckedInformation( final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo )
	{
		lines.add( this.getLocalizedName( this.getSettingsName( stack ) + ".name", this.getSettingsName( stack ) ) );

		final NBTTagCompound data = this.getData( stack );
		if( data.hasKey( "tooltip" ) )
		{
			lines.add( I18n.translateToLocal( this.getLocalizedName( data.getString( "tooltip" ) + ".name", data.getString( "tooltip" ) ) ) );
		}
	}

	/**
	 * Find the localized string...
	 *
	 * @param name possible names for the localized string
	 *
	 * @return localized name
	 */
	private String getLocalizedName( final String... name )
	{
		for( final String n : name )
		{
			final String l = I18n.translateToLocal( n );
			if( !l.equals( n ) )
			{
				return l;
			}
		}

		for( final String n : name )
		{
			return n;
		}

		return "";
	}

	@Override
	public void setMemoryCardContents( final ItemStack is, final String settingsName, final NBTTagCompound data )
	{
		final NBTTagCompound c = Platform.openNbtData( is );
		c.setString( "Config", settingsName );
		c.setTag( "Data", data );
	}

	@Override
	public String getSettingsName( final ItemStack is )
	{
		final NBTTagCompound c = Platform.openNbtData( is );
		final String name = c.getString( "Config" );
		return name == null || name.isEmpty() ? GuiText.Blank.getUnlocalized() : name;
	}

	@Override
	public NBTTagCompound getData( final ItemStack is )
	{
		final NBTTagCompound c = Platform.openNbtData( is );
		NBTTagCompound o = c.getCompoundTag( "Data" );
		if( o == null )
		{
			o = new NBTTagCompound();
		}
		return (NBTTagCompound) o.copy();
	}

	@Override
	public void notifyUser( final EntityPlayer player, final MemoryCardMessages msg )
	{
		if( Platform.isClient() )
		{
			return;
		}

		switch( msg )
		{
			case SETTINGS_CLEARED:
				player.addChatMessage( PlayerMessages.SettingCleared.get() );
				break;
			case INVALID_MACHINE:
				player.addChatMessage( PlayerMessages.InvalidMachine.get() );
				break;
			case SETTINGS_LOADED:
				player.addChatMessage( PlayerMessages.LoadedSettings.get() );
				break;
			case SETTINGS_SAVED:
				player.addChatMessage( PlayerMessages.SavedSettings.get() );
				break;
			default:
		}
	}

	@Override
	public EnumActionResult onItemUse( final ItemStack is, final EntityPlayer player, final World w, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float hx, final float hy, final float hz )
	{
		if( player.isSneaking() && !w.isRemote )
		{
			final IMemoryCard mem = (IMemoryCard) is.getItem();
			mem.notifyUser( player, MemoryCardMessages.SETTINGS_CLEARED );
			is.setTagCompound( null );
			return EnumActionResult.SUCCESS;
		}
		else
		{
			return super.onItemUse( is, player, w, pos, hand, side, hx, hy, hz );
		}
	}
	
	@Override
	public boolean doesSneakBypassUse( final ItemStack itemstack, final IBlockAccess world, final BlockPos pos, final EntityPlayer player )
	{
		return true;
	}
}
