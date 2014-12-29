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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.util.IConfigManager;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;


public class ToolWirelessTerminal extends AEBasePoweredItem implements IWirelessTermHandler
{

	public ToolWirelessTerminal()
	{
		super( ToolWirelessTerminal.class, Optional.<String> absent() );
		this.setFeature( EnumSet.of( AEFeature.WirelessAccessTerminal, AEFeature.PoweredTools ) );
		this.maxStoredPower = AEConfig.instance.wirelessTerminalBattery;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean isFull3D()
	{
		return false;
	}
	
	@Override
	public ItemStack onItemRightClick( ItemStack item, World w, EntityPlayer player )
	{
		AEApi.instance().registries().wireless().openWirelessTerminalGui( item, w, player );
		return item;
	}

	@Override
	public void addCheckedInformation( ItemStack stack, EntityPlayer player, List<String> lines, boolean displayAdditionalInformation )
	{
		super.addCheckedInformation( stack, player, lines, displayAdditionalInformation );

		if ( stack.hasTagCompound() )
		{
			NBTTagCompound tag = Platform.openNbtData( stack );
			if ( tag != null )
			{
				String encKey = tag.getString( "encryptionKey" );

				if ( encKey == null || encKey.isEmpty() )
					lines.add( GuiText.Unlinked.getLocal() );
				else
					lines.add( GuiText.Linked.getLocal() );
			}
		}
		else
			lines.add( StatCollector.translateToLocal( "AppEng.GuiITooltip.Unlinked" ) );
	}

	@Override
	public boolean canHandle( ItemStack is )
	{
		return AEApi.instance().items().itemWirelessTerminal.sameAsStack( is );
	}

	@Override
	public boolean usePower( EntityPlayer player, double amount, ItemStack is )
	{
		return this.extractAEPower( is, amount ) >= amount - 0.5;
	}

	@Override
	public boolean hasPower( EntityPlayer player, double amt, ItemStack is )
	{
		return this.getAECurrentPower( is ) >= amt;
	}

	@Override
	public IConfigManager getConfigManager( final ItemStack target )
	{
		final ConfigManager out = new ConfigManager( new IConfigManagerHost(){

			@Override
			public void updateSetting( IConfigManager manager, Enum settingName, Enum newValue )
			{
				NBTTagCompound data = Platform.openNbtData( target );
				manager.writeToNBT( data );
			}

		} );

		out.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		out.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
		out.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );

		out.readFromNBT( ( NBTTagCompound ) Platform.openNbtData( target ).copy() );
		return out;
	}

	@Override
	public String getEncryptionKey( ItemStack item )
	{
		NBTTagCompound tag = Platform.openNbtData( item );
		return tag.getString( "encryptionKey" );
	}

	@Override
	public void setEncryptionKey( ItemStack item, String encKey, String name )
	{
		NBTTagCompound tag = Platform.openNbtData( item );
		tag.setString( "encryptionKey", encKey );
		tag.setString( "name", name );
	}

}
