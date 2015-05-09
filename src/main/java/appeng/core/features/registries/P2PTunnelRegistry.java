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

package appeng.core.features.registries;


import java.util.HashMap;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.oredict.OreDictionary;

import cpw.mods.fml.common.registry.GameRegistry;

import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IParts;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.api.util.AEColor;
import appeng.util.Platform;


public class P2PTunnelRegistry implements IP2PTunnelRegistry
{

	final Map<ItemStack, TunnelType> Tunnels = new HashMap<ItemStack, TunnelType>();

	public void configure()
	{
		/**
		 * light!
		 */
		this.addNewAttunement( new ItemStack( Blocks.torch ), TunnelType.LIGHT );
		this.addNewAttunement( new ItemStack( Blocks.glowstone ), TunnelType.LIGHT );

		/**
		 * attune based on most redstone base items.
		 */
		this.addNewAttunement( new ItemStack( Items.redstone ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Items.repeater ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Blocks.redstone_lamp ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Blocks.unpowered_comparator ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Blocks.powered_comparator ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Blocks.powered_repeater ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Blocks.unpowered_repeater ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Blocks.daylight_detector ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Blocks.redstone_wire ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Blocks.redstone_block ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Blocks.lever ), TunnelType.REDSTONE );
		this.addNewAttunement( this.getModItem( "EnderIO", "itemRedstoneConduit", OreDictionary.WILDCARD_VALUE ), TunnelType.REDSTONE );

		/**
		 * attune based on lots of random item related stuff
		 */
		final IDefinitions definitions = AEApi.instance().definitions();
		final IBlocks blocks = definitions.blocks();
		final IParts parts = definitions.parts();

		this.addNewAttunement( blocks.iface(), TunnelType.ITEM );
		this.addNewAttunement( parts.iface(), TunnelType.ITEM );
		this.addNewAttunement( parts.storageBus(), TunnelType.ITEM );
		this.addNewAttunement( parts.importBus(), TunnelType.ITEM );
		this.addNewAttunement( parts.exportBus(), TunnelType.ITEM );

		this.addNewAttunement( new ItemStack( Blocks.hopper ), TunnelType.ITEM );
		this.addNewAttunement( new ItemStack( Blocks.chest ), TunnelType.ITEM );
		this.addNewAttunement( new ItemStack( Blocks.trapped_chest ), TunnelType.ITEM );
		this.addNewAttunement( this.getModItem( "ExtraUtilities", "extractor_base", 0 ), TunnelType.ITEM );
		this.addNewAttunement( this.getModItem( "Mekanism", "PartTransmitter", 9 ), TunnelType.ITEM );
		this.addNewAttunement( this.getModItem( "EnderIO", "itemItemConduit", OreDictionary.WILDCARD_VALUE ), TunnelType.ITEM );

		/**
		 * attune based on lots of random item related stuff
		 */
		this.addNewAttunement( new ItemStack( Items.bucket ), TunnelType.FLUID );
		this.addNewAttunement( new ItemStack( Items.lava_bucket ), TunnelType.FLUID );
		this.addNewAttunement( new ItemStack( Items.milk_bucket ), TunnelType.FLUID );
		this.addNewAttunement( new ItemStack( Items.water_bucket ), TunnelType.FLUID );
		this.addNewAttunement( this.getModItem( "Mekanism", "MachineBlock2", 11 ), TunnelType.FLUID );
		this.addNewAttunement( this.getModItem( "Mekanism", "PartTransmitter", 4 ), TunnelType.FLUID );
		this.addNewAttunement( this.getModItem( "ExtraUtilities", "extractor_base", 6 ), TunnelType.FLUID );
		this.addNewAttunement( this.getModItem( "ExtraUtilities", "drum", OreDictionary.WILDCARD_VALUE ), TunnelType.FLUID );
		this.addNewAttunement( this.getModItem( "EnderIO", "itemLiquidConduit", OreDictionary.WILDCARD_VALUE ), TunnelType.FLUID );

		for( AEColor c : AEColor.values() )
		{
			this.addNewAttunement( parts.cableGlass().stack( c, 1 ), TunnelType.ME );
			this.addNewAttunement( parts.cableCovered().stack( c, 1 ), TunnelType.ME );
			this.addNewAttunement( parts.cableSmart().stack( c, 1 ), TunnelType.ME );
			this.addNewAttunement( parts.cableDense().stack( c, 1 ), TunnelType.ME );
		}
	}

	@Override
	public void addNewAttunement( ItemStack trigger, TunnelType type )
	{
		if( type == null || trigger == null )
		{
			return;
		}

		this.Tunnels.put( trigger, type );
	}

	public ItemStack getModItem( String modID, String name, int meta )
	{
		ItemStack myItemStack = GameRegistry.findItemStack( modID, name, 1 );

		if( myItemStack == null )
		{
			return null;
		}

		myItemStack.setItemDamage( meta );
		return myItemStack;
	}

	private void addNewAttunement( IItemDefinition definition, TunnelType type )
	{
		for( ItemStack definitionStack : definition.maybeStack( 1 ).asSet() )
		{
			this.addNewAttunement( definitionStack, type );
		}
	}

	@Override
	public TunnelType getTunnelTypeByItem( ItemStack trigger )
	{
		if( trigger != null )
		{
			if( FluidContainerRegistry.isContainer( trigger ) )
			{
				return TunnelType.FLUID;
			}

			for( ItemStack is : this.Tunnels.keySet() )
			{
				if( is.getItem() == trigger.getItem() && is.getItemDamage() == OreDictionary.WILDCARD_VALUE )
				{
					return this.Tunnels.get( is );
				}

				if( Platform.isSameItem( is, trigger ) )
				{
					return this.Tunnels.get( is );
				}
			}
		}

		return null;
	}
}
