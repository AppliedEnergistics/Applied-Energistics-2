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

package appeng.items.tools.powered;


import java.util.EnumSet;
import java.util.List;

import com.google.common.base.Optional;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.client.render.items.ToolWirelessTerminalRender;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.tile.networking.TileWireless;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;


public class ToolWirelessTerminal extends AEBasePoweredItem implements IWirelessTermHandler
{

	public ToolWirelessTerminal()
	{
		super( AEConfig.instance.wirelessTerminalBattery, Optional.<String>absent() );
		this.setFeature( EnumSet.of( AEFeature.WirelessAccessTerminal, AEFeature.PoweredTools ) );

		if( Platform.isClient() )
		{
			MinecraftForgeClient.registerItemRenderer( this, new ToolWirelessTerminalRender() );
		}
	}

	public static AEColor getColor( ItemStack item )
	{
		final NBTTagCompound c = item.getTagCompound();
		if( c != null && c.hasKey( "color" ) )
		{
			final String color = c.getString( "color" );
			if( color != null )
			{
				try
				{
					return AEColor.valueOf( color );
				}
				catch( final Exception e )
				{
					//Ignore invalid colors
				}
			}
		}

		return null;
	}

	@Override
	public String getItemStackDisplayName( final ItemStack par1ItemStack )
	{

		final AEColor color = ToolWirelessTerminal.getColor( par1ItemStack );

		if( color != null && Platform.isClient() )
		{
			final String extra = Platform.gui_localize( color.unlocalizedName );
			return super.getItemStackDisplayName( par1ItemStack ) + " - " + extra;
		}
		else
		{
			return super.getItemStackDisplayName( par1ItemStack );
		}
	}

	@Override
	public ItemStack onItemRightClick( final ItemStack item, final World w, final EntityPlayer player )
	{
		AEApi.instance().registries().wireless().openWirelessTerminalGui( item, w, player );
		return item;
	}

	@SideOnly( Side.CLIENT )
	@Override
	public boolean isFull3D()
	{
		return false;
	}

	@Override
	public void addCheckedInformation( final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo )
	{
		super.addCheckedInformation( stack, player, lines, displayMoreInfo );

		if( isLinked( stack ) )
		{
			lines.add( GuiText.Linked.getLocal() );
		}
		else
		{
			lines.add( GuiText.Unlinked.getLocal() );
		}
	}

	public static boolean isLinked( final ItemStack stack )
	{
		if( stack.hasTagCompound() )
		{
			final NBTTagCompound tag = Platform.openNbtData( stack );
			if( tag != null )
			{
				final String encKey = tag.getString( "encryptionKey" );

				return !( encKey == null || encKey.isEmpty() );
			}
		}
		return false;
	}

	public boolean getIsUsable( final ItemStack stack, Entity holder )
	{
		if( stack.hasTagCompound() )
		{
			final NBTTagCompound tag = Platform.openNbtData( stack );
			if( tag != null )
			{
				boolean usable = this.hasPower( null, 0.5, stack ) && ToolWirelessTerminal.isLinked( stack );
				if( usable )
				{
					final long parsedKey = Long.parseLong( this.getEncryptionKey( stack ) );
					final ILocatable securityStation = AEApi.instance().registries().locatable().getLocatableBy( parsedKey );
					if( securityStation == null )
					{
						usable = false;
					}
				}
				if( usable )
				{
					usable = rangeCheck( stack, holder );
				}
				return usable;
			}
		}

		return false;
	}

	/**
	 * Checks if the user can connect to the ME system associated with the Wireless Terminal
	 *
	 * @param itm The wireless terminal
	 * @param player the player trying to connect
	 *
	 * @return
	 */
	public boolean rangeCheck( ItemStack itm, final Entity player )
	{
		//Get the encryption key
		ILocatable obj = null;
		try
		{
			final long encKey = Long.parseLong( this.getEncryptionKey( itm ) );
			obj = AEApi.instance().registries().locatable().getLocatableBy( encKey );
		}
		catch( final NumberFormatException err )
		{
			// :P
		}

		IGrid grid = null;
		IMEMonitor<IAEItemStack> inv = null;
		if( obj instanceof IGridHost )
		{
			final IGridNode n = ( (IGridHost) obj ).getGridNode( ForgeDirection.UNKNOWN );
			if( n != null )
			{
				grid = n.getGrid();
				if( grid != null )
				{
					final IStorageGrid sg = grid.getCache( IStorageGrid.class );
					if( sg != null )
					{
						inv = sg.getItemInventory();
					}
				}
			}
		}

		if( grid != null && inv != null )
		{

			final IMachineSet tw = grid.getMachines( TileWireless.class );

			for( final IGridNode n : tw )
			{
				final IWirelessAccessPoint wap = (IWirelessAccessPoint) n.getMachine();
				if( this.testWap( wap, player ) )
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns if you can connect to a WAP
	 *
	 * @param wap The WAP to test if you can connect to
	 * @param player The player entity
	 *
	 * @return True if the WAP is within range and active
	 */
	private boolean testWap( final IWirelessAccessPoint wap, final Entity player )
	{
		double rangeLimit = wap.getRange();
		rangeLimit *= rangeLimit;

		final DimensionalCoord dc = wap.getLocation();

		if( dc.getWorld().provider.dimensionId == player.worldObj.provider.dimensionId )
		{
			final double offX = dc.x - player.posX;
			final double offY = dc.y - player.posY;
			final double offZ = dc.z - player.posZ;

			final double r = offX * offX + offY * offY + offZ * offZ;
			if( r < rangeLimit && wap.isActive() )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canHandle( final ItemStack is )
	{
		return AEApi.instance().definitions().items().wirelessTerminal().item( AEColor.Black ).equals( is.getItem() );
	}

	@Override
	public boolean usePower( final EntityPlayer player, final double amount, final ItemStack is )
	{
		return this.extractAEPower( is, amount ) >= amount - 0.5;
	}

	@Override
	public boolean hasPower( final EntityPlayer player, final double amt, final ItemStack is )
	{
		return this.getAECurrentPower( is ) >= amt;
	}

	@Override
	public IConfigManager getConfigManager( final ItemStack target )
	{
		final ConfigManager out = new ConfigManager( new IConfigManagerHost()
		{

			@Override
			public void updateSetting( final IConfigManager manager, final Enum settingName, final Enum newValue )
			{
				final NBTTagCompound data = Platform.openNbtData( target );
				manager.writeToNBT( data );
			}
		} );

		out.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		out.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
		out.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );

		out.readFromNBT( (NBTTagCompound) Platform.openNbtData( target ).copy() );
		return out;
	}

	@Override
	public String getEncryptionKey( final ItemStack item )
	{
		final NBTTagCompound tag = Platform.openNbtData( item );
		return tag.getString( "encryptionKey" );
	}

	@Override
	public void setEncryptionKey( final ItemStack item, final String encKey, final String name )
	{
		final NBTTagCompound tag = Platform.openNbtData( item );
		tag.setString( "encryptionKey", encKey );
		tag.setString( "name", name );
	}
}
