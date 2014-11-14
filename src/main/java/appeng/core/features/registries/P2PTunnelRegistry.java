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

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import appeng.api.definitions.Parts;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.api.util.AEColor;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;

public class P2PTunnelRegistry implements IP2PTunnelRegistry
{

	final HashMap<ItemStack, TunnelType> Tunnels = new HashMap<ItemStack, TunnelType>();

	public ItemStack getModItem(String modID, String Name, int meta)
	{
		ItemStack myItemStack = GameRegistry.findItemStack( modID, Name, 1 );

		if ( myItemStack == null )
			return null;

		myItemStack.setItemDamage( meta );
		return myItemStack;
	}

	public void configure()
	{
		/**
		 * light!
		 */
		addNewAttunement( new ItemStack( Blocks.torch ), TunnelType.LIGHT );
		addNewAttunement( new ItemStack( Blocks.glowstone ), TunnelType.LIGHT );

		/**
		 * attune based on most redstone base items.
		 */
		addNewAttunement( new ItemStack( Items.redstone ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Items.repeater ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Blocks.redstone_lamp ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Blocks.unpowered_comparator ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Blocks.powered_comparator ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Blocks.powered_repeater ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Blocks.unpowered_repeater ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Blocks.daylight_detector ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Blocks.redstone_wire ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Blocks.redstone_block ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Blocks.lever ), TunnelType.REDSTONE );
		addNewAttunement( getModItem( "EnderIO", "itemRedstoneConduit", OreDictionary.WILDCARD_VALUE ), TunnelType.REDSTONE );

		/**
		 * attune based on lots of random item related stuff
		 */
		appeng.api.definitions.Blocks AEBlocks = AEApi.instance().blocks();
		Parts Parts = AEApi.instance().parts();

		addNewAttunement( AEBlocks.blockInterface.stack( 1 ), TunnelType.ITEM );
		addNewAttunement( Parts.partInterface.stack( 1 ), TunnelType.ITEM );
		addNewAttunement( Parts.partStorageBus.stack( 1 ), TunnelType.ITEM );
		addNewAttunement( Parts.partImportBus.stack( 1 ), TunnelType.ITEM );
		addNewAttunement( Parts.partExportBus.stack( 1 ), TunnelType.ITEM );
		addNewAttunement( new ItemStack( Blocks.hopper ), TunnelType.ITEM );
		addNewAttunement( new ItemStack( Blocks.chest ), TunnelType.ITEM );
		addNewAttunement( new ItemStack( Blocks.trapped_chest ), TunnelType.ITEM );
		addNewAttunement( getModItem( "ExtraUtilities", "extractor_base", 0 ), TunnelType.ITEM );
		addNewAttunement( getModItem( "Mekanism", "PartTransmitter", 9 ), TunnelType.ITEM );
		addNewAttunement( getModItem( "EnderIO", "itemItemConduit", OreDictionary.WILDCARD_VALUE ), TunnelType.ITEM );

		/**
		 * attune based on lots of random item related stuff
		 */
		addNewAttunement( new ItemStack( Items.bucket ), TunnelType.FLUID );
		addNewAttunement( new ItemStack( Items.lava_bucket ), TunnelType.FLUID );
		addNewAttunement( new ItemStack( Items.milk_bucket ), TunnelType.FLUID );
		addNewAttunement( new ItemStack( Items.water_bucket ), TunnelType.FLUID );
		addNewAttunement( getModItem( "Mekanism", "MachineBlock2", 11 ), TunnelType.FLUID );
		addNewAttunement( getModItem( "Mekanism", "PartTransmitter", 4 ), TunnelType.FLUID );
		addNewAttunement( getModItem( "ExtraUtilities", "extractor_base", 6 ), TunnelType.FLUID );
		addNewAttunement( getModItem( "ExtraUtilities", "drum", OreDictionary.WILDCARD_VALUE ), TunnelType.FLUID );
		addNewAttunement( getModItem( "EnderIO", "itemLiquidConduit", OreDictionary.WILDCARD_VALUE ), TunnelType.FLUID );

		for (AEColor c : AEColor.values())
		{
			addNewAttunement( Parts.partCableGlass.stack( c, 1 ), TunnelType.ME );
			addNewAttunement( Parts.partCableCovered.stack( c, 1 ), TunnelType.ME );
			addNewAttunement( Parts.partCableSmart.stack( c, 1 ), TunnelType.ME );
			addNewAttunement( Parts.partCableDense.stack( c, 1 ), TunnelType.ME );
		}
	}

	@Override
	public void addNewAttunement(ItemStack trigger, TunnelType type)
	{
		if ( type == null || trigger == null )
			return;

		Tunnels.put( trigger, type );
	}

	@Override
	public TunnelType getTunnelTypeByItem(ItemStack trigger)
	{
		if ( trigger != null )
		{
			if ( FluidContainerRegistry.isContainer( trigger ) )
				return TunnelType.FLUID;

			for (ItemStack is : Tunnels.keySet())
			{
				if ( is.getItem() == trigger.getItem() && is.getItemDamage() == OreDictionary.WILDCARD_VALUE )
					return Tunnels.get( is );

				if ( Platform.isSameItem( is, trigger ) )
					return Tunnels.get( is );
			}
		}

		return null;
	}

}
