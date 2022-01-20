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


import java.util.*;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import appeng.core.AELog;
import appeng.util.item.OreHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IParts;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.api.util.AEColor;
import appeng.capabilities.Capabilities;


public final class P2PTunnelRegistry implements IP2PTunnelRegistry
{
	private static final int INITIAL_CAPACITY = 40;

	private final Map<ItemStack, TunnelType> tunnels = new HashMap<>( INITIAL_CAPACITY );
	private final Map<String, TunnelType> modIdTunnels = new HashMap<>( INITIAL_CAPACITY );
	private final Map<Capability<?>, TunnelType> capTunnels = new HashMap<>( INITIAL_CAPACITY );

	public void configure() {

		final IDefinitions definitions = AEApi.instance().definitions();
		final IBlocks blocks = definitions.blocks();
		final IParts parts = definitions.parts();

		/**
		 * light!
		 */
		this.addNewAttunement(new ItemStack(Blocks.TORCH), TunnelType.LIGHT);
		this.addNewAttunement(new ItemStack(Blocks.GLOWSTONE), TunnelType.LIGHT);

		List<String> gtceOreDict = new ArrayList<>();
		gtceOreDict.add("wireGtHex");
		gtceOreDict.add("wireGtOctal");
		gtceOreDict.add("wireGtQuadruple");
		gtceOreDict.add("wireGtDouble");
		gtceOreDict.add("wireGtSingle");
		gtceOreDict.add("cableGtHex");
		gtceOreDict.add("cableGtOctal");
		gtceOreDict.add("cableGtQuadruple");
		gtceOreDict.add("cableGtDouble");
		gtceOreDict.add("cableGtSingle");

		for(String oreDict : gtceOreDict) {
			Arrays.stream(OreDictionary.getOreNames()).filter(s -> s.startsWith(oreDict)).forEach(s -> {
						OreHelper.INSTANCE.getCachedOres(s).forEach(
								stack -> {
									this.addNewAttunement(stack, TunnelType.GTCEU_POWER);
									AELog.info("Added " + stack + " to P2P Tunnel Registry");
								});
					}
			);
		}

		/**
		 * Forge energy tunnel items
		 */
		
		this.addNewAttunement( blocks.energyCellDense(), TunnelType.FE_POWER );
		this.addNewAttunement( blocks.energyAcceptor(), TunnelType.FE_POWER );
		this.addNewAttunement( blocks.energyCell(), TunnelType.FE_POWER );
		this.addNewAttunement( blocks.energyCellCreative(), TunnelType.FE_POWER );

		this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_0", 0 ), TunnelType.FE_POWER );
		this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_0", 1 ), TunnelType.FE_POWER );
		this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_0", 2 ), TunnelType.FE_POWER );
		this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_0", 3 ), TunnelType.FE_POWER );
		this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_0", 4 ), TunnelType.FE_POWER );
		this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_0", 5 ), TunnelType.FE_POWER );

		/**
		 * EU tunnel items
		 */

		this.addNewAttunement( this.getModItem( "ic2", "cable", 0 ), TunnelType.IC2_POWER ); // Copper cable
		this.addNewAttunement( this.getModItem( "ic2", "cable", 1 ), TunnelType.IC2_POWER ); // Glass fibre cable
		this.addNewAttunement( this.getModItem( "ic2", "cable", 2 ), TunnelType.IC2_POWER ); // Gold cable
		this.addNewAttunement( this.getModItem( "ic2", "cable", 3 ), TunnelType.IC2_POWER ); // HV cable
		this.addNewAttunement( this.getModItem( "ic2", "cable", 4 ), TunnelType.IC2_POWER ); // Tin cable

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
		this.addNewAttunement( this.getModItem( "enderio", "itemredstoneconduit", OreDictionary.WILDCARD_VALUE ), TunnelType.REDSTONE );

		/**
		 * attune based on lots of random item related stuff
		 */

		this.addNewAttunement( blocks.iface(), TunnelType.ITEM );
		this.addNewAttunement( parts.iface(), TunnelType.ITEM );
		this.addNewAttunement( parts.storageBus(), TunnelType.ITEM );
		this.addNewAttunement( parts.importBus(), TunnelType.ITEM );
		this.addNewAttunement( parts.exportBus(), TunnelType.ITEM );

		this.addNewAttunement( new ItemStack( Blocks.HOPPER ), TunnelType.ITEM );
		this.addNewAttunement( new ItemStack( Blocks.CHEST ), TunnelType.ITEM );
		this.addNewAttunement( new ItemStack( Blocks.TRAPPED_CHEST ), TunnelType.ITEM );
		this.addNewAttunement( this.getModItem( "extrautilities", "extractor_base", 0 ), TunnelType.ITEM );
		this.addNewAttunement( this.getModItem( "mekanism", "parttransmitter", 9 ), TunnelType.ITEM );
		this.addNewAttunement( this.getModItem( "enderio", "itemitemconduit", OreDictionary.WILDCARD_VALUE ), TunnelType.ITEM );
		this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_32", 0 ), TunnelType.ITEM ); // itemduct
		this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_32", 1 ), TunnelType.ITEM ); // itemduct
																										// (opaque)
		this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_32", 2 ), TunnelType.ITEM ); // impulse
																										// itemduct
		this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_32", 3 ), TunnelType.ITEM ); // impulse
																										// itemduct
																										// (opaque)

		/**
		 * attune based on lots of random item related stuff
		 */
		this.addNewAttunement( new ItemStack( Items.BUCKET ), TunnelType.FLUID );
		this.addNewAttunement( new ItemStack( Items.LAVA_BUCKET ), TunnelType.FLUID );
		this.addNewAttunement( new ItemStack( Items.MILK_BUCKET ), TunnelType.FLUID );
		this.addNewAttunement( new ItemStack( Items.WATER_BUCKET ), TunnelType.FLUID );
		this.addNewAttunement( this.getModItem( "mekanism", "machineblock2", 11 ), TunnelType.FLUID );
		this.addNewAttunement( this.getModItem( "mekanism", "parttransmitter", 4 ), TunnelType.FLUID );
		this.addNewAttunement( this.getModItem( "extrautilities", "extractor_base", 6 ), TunnelType.FLUID );
		this.addNewAttunement( this.getModItem( "extrautilities", "drum", OreDictionary.WILDCARD_VALUE ), TunnelType.FLUID );
		this.addNewAttunement( this.getModItem( "enderio", "itemliquidconduit", OreDictionary.WILDCARD_VALUE ), TunnelType.FLUID );
		this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_16", 0 ), TunnelType.FLUID ); // fluiduct
		this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_16", 1 ), TunnelType.FLUID ); // fluiduct
																										// (opaque)
		this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_16", 2 ), TunnelType.FLUID ); // fluiduct
																										// hardened
		this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_16", 3 ), TunnelType.FLUID ); // fluiduct
																										// hardened
																										// (opaque)

		for( final AEColor c : AEColor.values() )
		{
			this.addNewAttunement( parts.cableGlass().stack( c, 1 ), TunnelType.ME );
			this.addNewAttunement( parts.cableCovered().stack( c, 1 ), TunnelType.ME );
			this.addNewAttunement( parts.cableSmart().stack( c, 1 ), TunnelType.ME );
			this.addNewAttunement( parts.cableDenseSmart().stack( c, 1 ), TunnelType.ME );
		}

		/**
		 * attune based caps
		 */
		this.addNewAttunement( Capabilities.FORGE_ENERGY, TunnelType.FE_POWER );
		this.addNewAttunement( CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, TunnelType.FLUID );

		/**
		 * attune based on the ItemStack's modId
		 */

		this.addNewAttunement( "thermaldynamics", TunnelType.FE_POWER );
		this.addNewAttunement( "thermalexpansion", TunnelType.FE_POWER );
		this.addNewAttunement( "thermalfoundation", TunnelType.FE_POWER );
		// TODO: Remove when confirmed that the official 1.12 version of EnderIO will support FE.
		this.addNewAttunement( "enderio", TunnelType.FE_POWER );
		// TODO: Remove when confirmed that the official 1.12 version of Mekanism will support FE.
		this.addNewAttunement( "mekanism", TunnelType.FE_POWER );
		// TODO: Remove when support for RFTools' Powercells support is added
		this.addNewAttunement( "rftools", TunnelType.FE_POWER );
		this.addNewAttunement( "ic2", TunnelType.IC2_POWER );

	}

	@Override
	public void addNewAttunement( @Nonnull final String modId, @Nullable final TunnelType type )
	{
		if( type == null || modId == null )
		{
			return;
		}
		this.modIdTunnels.put( modId, type );
	}

	@Override
	public void addNewAttunement( @Nonnull final Capability<?> cap, @Nullable final TunnelType type )
	{
		if( type == null || cap == null )
		{
			return;
		}
		this.capTunnels.put( cap, type );
	}

	@Override
	public void addNewAttunement( @Nonnull final ItemStack trigger, @Nullable final TunnelType type )
	{
		if( type == null || trigger.isEmpty() )
		{
			return;
		}

		this.tunnels.put( trigger, type );
	}

	@Nullable
	@Override
	public TunnelType getTunnelTypeByItem( final ItemStack trigger )
	{
		if( !trigger.isEmpty() )
		{
			// First match exact items
			for( final Entry<ItemStack, TunnelType> entry : this.tunnels.entrySet() )
			{
				final ItemStack is = entry.getKey();

				if( is.getItem() == trigger.getItem() && is.getItemDamage() == OreDictionary.WILDCARD_VALUE )
				{
					return entry.getValue();
				}

				if( ItemStack.areItemsEqual( is, trigger ) )
				{
					return entry.getValue();
				}
			}

			// Next, check if the Item you're holding supports any registered capability
			for( EnumFacing face : EnumFacing.VALUES )
			{
				for( Entry<Capability<?>, TunnelType> entry : this.capTunnels.entrySet() )
				{
					if( trigger.hasCapability( entry.getKey(), face ) )
					{
						return entry.getValue();
					}
				}
			}

			// Use the mod id as last option.
			for( final Entry<String, TunnelType> entry : this.modIdTunnels.entrySet() )
			{
				if( trigger.getItem().getRegistryName() != null && trigger.getItem().getRegistryName().getResourceDomain().equals( entry.getKey() ) )
				{
					return entry.getValue();
				}
			}
		}

		return null;
	}

	@Nonnull
	private ItemStack getModItem( final String modID, final String name, final int meta )
	{

		final Item item = Item.getByNameOrId( modID + ":" + name );

		if( item == null )
		{
			return ItemStack.EMPTY;
		}

		final ItemStack myItemStack = new ItemStack( item, 1, meta );
		return myItemStack;
	}

	private void addNewAttunement( final IItemDefinition definition, final TunnelType type )
	{
		definition.maybeStack( 1 ).ifPresent( definitionStack -> this.addNewAttunement( definitionStack, type ) );
	}
}
