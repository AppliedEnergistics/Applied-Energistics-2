/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

import javax.annotation.Nullable;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IParts;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.api.util.AEColor;
import appeng.util.Platform;


public final class P2PTunnelRegistry implements IP2PTunnelRegistry
{
	private static final int INITIAL_CAPACITY = 40;

	private final Map<ItemStack, TunnelType> tunnels = new HashMap<ItemStack, TunnelType>( INITIAL_CAPACITY );

	public void configure()
	{
		/**
		 * light!
		 */
		this.addNewAttunement( new ItemStack( Blocks.TORCH ), TunnelType.LIGHT );
		this.addNewAttunement( new ItemStack( Blocks.GLOWSTONE ), TunnelType.LIGHT );

		/**
		 * attune based on most redstone base items.
		 */
		this.addNewAttunement( new ItemStack( Items.REDSTONE ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Items.REPEATER ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Blocks.REDSTONE_LAMP ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Blocks.UNPOWERED_COMPARATOR ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Blocks.POWERED_COMPARATOR ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Blocks.POWERED_REPEATER ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Blocks.UNPOWERED_REPEATER ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Blocks.DAYLIGHT_DETECTOR ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Blocks.REDSTONE_WIRE ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Blocks.REDSTONE_BLOCK ), TunnelType.REDSTONE );
		this.addNewAttunement( new ItemStack( Blocks.LEVER ), TunnelType.REDSTONE );
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

		this.addNewAttunement( new ItemStack( Blocks.HOPPER ), TunnelType.ITEM );
		this.addNewAttunement( new ItemStack( Blocks.CHEST ), TunnelType.ITEM );
		this.addNewAttunement( new ItemStack( Blocks.TRAPPED_CHEST ), TunnelType.ITEM );
		this.addNewAttunement( this.getModItem( "ExtraUtilities", "extractor_base", 0 ), TunnelType.ITEM );
		this.addNewAttunement( this.getModItem( "Mekanism", "PartTransmitter", 9 ), TunnelType.ITEM );
		this.addNewAttunement( this.getModItem( "EnderIO", "itemItemConduit", OreDictionary.WILDCARD_VALUE ), TunnelType.ITEM );
		this.addNewAttunement( this.getModItem( "ThermalDynamics", "ThermalDynamics_32", 0 ), TunnelType.ITEM );

		/**
		 * attune based on lots of random item related stuff
		 */
		this.addNewAttunement( new ItemStack( Items.BUCKET ), TunnelType.FLUID );
		this.addNewAttunement( new ItemStack( Items.LAVA_BUCKET ), TunnelType.FLUID );
		this.addNewAttunement( new ItemStack( Items.MILK_BUCKET ), TunnelType.FLUID );
		this.addNewAttunement( new ItemStack( Items.WATER_BUCKET ), TunnelType.FLUID );
		this.addNewAttunement( this.getModItem( "Mekanism", "MachineBlock2", 11 ), TunnelType.FLUID );
		this.addNewAttunement( this.getModItem( "Mekanism", "PartTransmitter", 4 ), TunnelType.FLUID );
		this.addNewAttunement( this.getModItem( "ExtraUtilities", "extractor_base", 6 ), TunnelType.FLUID );
		this.addNewAttunement( this.getModItem( "ExtraUtilities", "drum", OreDictionary.WILDCARD_VALUE ), TunnelType.FLUID );
		this.addNewAttunement( this.getModItem( "EnderIO", "itemLiquidConduit", OreDictionary.WILDCARD_VALUE ), TunnelType.FLUID );
		this.addNewAttunement( this.getModItem( "ThermalDynamics", "ThermalDynamics_16", 0 ), TunnelType.FLUID );

		for( final AEColor c : AEColor.values() )
		{
			this.addNewAttunement( parts.cableGlass().stack( c, 1 ), TunnelType.ME );
			this.addNewAttunement( parts.cableCovered().stack( c, 1 ), TunnelType.ME );
			this.addNewAttunement( parts.cableSmart().stack( c, 1 ), TunnelType.ME );
			this.addNewAttunement( parts.cableDense().stack( c, 1 ), TunnelType.ME );
		}
	}

	@Override
	public void addNewAttunement( @Nullable final ItemStack trigger, @Nullable final TunnelType type )
	{
		if( type == null || trigger == null )
		{
			return;
		}

		this.tunnels.put( trigger, type );
	}

	@Nullable
	@Override
	public TunnelType getTunnelTypeByItem( final ItemStack trigger )
	{
		if( trigger != null )
		{
			if( FluidContainerRegistry.isContainer( trigger ) )
			{
				return TunnelType.FLUID;
			}

			for( final ItemStack is : this.tunnels.keySet() )
			{
				if( is.getItem() == trigger.getItem() && is.getItemDamage() == OreDictionary.WILDCARD_VALUE )
				{
					return this.tunnels.get( is );
				}

				if( Platform.isSameItem( is, trigger ) )
				{
					return this.tunnels.get( is );
				}
			}
		}

		return null;
	}

	@Nullable
	private ItemStack getModItem( final String modID, final String name, final int meta )
	{
		final Item item = GameRegistry.findItem( modID, name );

		if( item == null )
		{
			return null;
		}

		final ItemStack myItemStack = new ItemStack( item, 1, meta );
		return myItemStack;
	}

	private void addNewAttunement( final IItemDefinition definition, final TunnelType type )
	{
		definition.maybeStack( 1 ).ifPresent( definitionStack -> addNewAttunement( definitionStack, type ) );
	}
}
