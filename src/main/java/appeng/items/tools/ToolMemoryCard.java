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


import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.items.AEBaseItem;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.EnumSet;
import java.util.List;


public class ToolMemoryCard extends AEBaseItem implements IMemoryCard
{
	public ToolMemoryCard()
	{
		this.setFeature( EnumSet.of( AEFeature.Core ) );
		this.setMaxStackSize( 1 );
	}

	@Override
	public void addCheckedInformation( final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo )
	{
		lines.add( this.getLocalizedName( this.getSettingsName( stack ) + ".name", this.getSettingsName( stack ) ) );

		final NBTTagCompound data = this.getData( stack );
		if( data.hasKey( "tooltip" ) )
		{
			lines.add( StatCollector.translateToLocal( this.getLocalizedName( data.getString( "tooltip" ) + ".name", data.getString( "tooltip" ) ) ) );
		}
	}

	/**
	 * Find the localized string...
	 *
	 * @param name possible names for the localized string
	 * @return localized name
	 */
	private String getLocalizedName( final String... name )
	{
		for( final String n : name )
		{
			final String l = StatCollector.translateToLocal( n );
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
	public boolean onItemUse( final ItemStack is, final EntityPlayer player, final World w, final int x, final int y, final int z, final int side, final float hx, final float hy, final float hz )
	{
		if( player.isSneaking() && !w.isRemote )
		{
			if( ForgeEventFactory.onItemUseStart( player, is, 1 ) <= 0 )
				return false;
			final IMemoryCard mem = (IMemoryCard) is.getItem();
			mem.notifyUser( player, MemoryCardMessages.SETTINGS_CLEARED );
			is.setTagCompound( null );
			return true;
		}
		else
		{
			return super.onItemUse( is, player, w, x, y, z, side, hx, hy, hz );
		}
	}

	@Override
	public boolean doesSneakBypassUse( final World world, final int x, final int y, final int z, final EntityPlayer player )
	{
		return true;
	}
}
