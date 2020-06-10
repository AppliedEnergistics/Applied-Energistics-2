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


import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.util.IConfigManager;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.ConfigManager;
import appeng.util.Platform;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class ToolWirelessTerminal extends AEBasePoweredItem implements IWirelessTermHandler
{

	public ToolWirelessTerminal()
	{
		super( AEConfig.instance().getWirelessTerminalBattery() );
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick( final World w, final PlayerEntity player, final Hand hand )
	{
		AEApi.instance().registries().wireless().openWirelessTerminalGui( player.getHeldItem( hand ), w, player );
		return new ActionResult<>( ActionResultType.SUCCESS, player.getHeldItem( hand ) );
	}

	@OnlyIn( Dist.CLIENT )
	@Override
	public boolean isFull3D()
	{
		return false;
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public void addCheckedInformation( final ItemStack stack, final World world, final List<String> lines, final ITooltipFlag advancedTooltips )
	{
		super.addCheckedInformation( stack, world, lines, advancedTooltips );

		if( stack.hasTag() )
		{
			final CompoundNBT tag = Platform.openNbtData( stack );
			if( tag != null )
			{
				final String encKey = tag.getString( "encryptionKey" );

				if( encKey == null || encKey.isEmpty() )
				{
					lines.add( GuiText.Unlinked.getLocal() );
				}
				else
				{
					lines.add( GuiText.Linked.getLocal() );
				}
			}
		}
		else
		{
			lines.add( I18n.format( "AppEng.GuiITooltip.Unlinked" ) );
		}
	}

	@Override
	public boolean canHandle( final ItemStack is )
	{
		return AEApi.instance().definitions().items().wirelessTerminal().isSameAs( is );
	}

	@Override
	public boolean usePower( final PlayerEntity player, final double amount, final ItemStack is )
	{
		return this.extractAEPower( is, amount, Actionable.MODULATE ) >= amount - 0.5;
	}

	@Override
	public boolean hasPower( final PlayerEntity player, final double amt, final ItemStack is )
	{
		return this.getAECurrentPower( is ) >= amt;
	}

	@Override
	public IConfigManager getConfigManager( final ItemStack target )
	{
		final ConfigManager out = new ConfigManager( ( manager, settingName, newValue ) ->
		{
			final CompoundNBT data = Platform.openNbtData( target );
			manager.writeToNBT( data );
		} );

		out.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		out.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
		out.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );

		out.readFromNBT( Platform.openNbtData( target ).copy() );
		return out;
	}

	@Override
	public String getEncryptionKey( final ItemStack item )
	{
		final CompoundNBT tag = Platform.openNbtData( item );
		return tag.getString( "encryptionKey" );
	}

	@Override
	public void setEncryptionKey( final ItemStack item, final String encKey, final String name )
	{
		final CompoundNBT tag = Platform.openNbtData( item );
		tag.putString( "encryptionKey", encKey );
		tag.putString( "name", name );
	}

	@Override
	public boolean shouldCauseReequipAnimation( ItemStack oldStack, ItemStack newStack, boolean slotChanged )
	{
		return slotChanged;
	}
}
