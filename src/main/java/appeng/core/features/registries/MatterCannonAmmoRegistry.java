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

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import appeng.api.features.IMatterCannonAmmoRegistry;


public class MatterCannonAmmoRegistry implements /* FIXME IOreListener, */ IMatterCannonAmmoRegistry
{

	private final HashMap<ItemStack, Double> DamageModifiers = new HashMap<>();

	public MatterCannonAmmoRegistry()
	{
		// FIXME OreDictionaryHandler.INSTANCE.observe( this );
		this.registerAmmo( new ItemStack( Items.GOLD_NUGGET ), 196.96655 );
	}

	@Override
	public void registerAmmo( final ItemStack ammo, final double weight )
	{
		this.DamageModifiers.put( ammo, weight );
	}

	@Override
	public float getPenetration( final ItemStack is )
	{
		for( final ItemStack o : this.DamageModifiers.keySet() )
		{
			if( ItemStack.areItemsEqual( o, is ) )
			{
				return this.DamageModifiers.get( o ).floatValue();
			}
		}
		return 0;
	}

// FIXME	@Override
// FIXME	public void oreRegistered( final String name, final ItemStack item )
// FIXME	{
// FIXME		if( !( name.startsWith( "berry" ) || name.startsWith( "nugget" ) ) )
// FIXME		{
// FIXME			return;
// FIXME		}
// FIXME
// FIXME		// addNugget( "Cobble", 18 ); // ?
// FIXME		this.considerItem( name, item, "MeatRaw", 32 );
// FIXME		this.considerItem( name, item, "MeatCooked", 32 );
// FIXME		this.considerItem( name, item, "Meat", 32 );
// FIXME		this.considerItem( name, item, "Chicken", 32 );
// FIXME		this.considerItem( name, item, "Beef", 32 );
// FIXME		this.considerItem( name, item, "Sheep", 32 );
// FIXME		this.considerItem( name, item, "Fish", 32 );
// FIXME
// FIXME		// real world...
// FIXME		this.considerItem( name, item, "Lithium", 6.941 );
// FIXME		this.considerItem( name, item, "Beryllium", 9.0122 );
// FIXME		this.considerItem( name, item, "Boron", 10.811 );
// FIXME		this.considerItem( name, item, "Carbon", 12.0107 );
// FIXME		this.considerItem( name, item, "Coal", 12.0107 );
// FIXME		this.considerItem( name, item, "Charcoal", 12.0107 );
// FIXME		this.considerItem( name, item, "Sodium", 22.9897 );
// FIXME		this.considerItem( name, item, "Magnesium", 24.305 );
// FIXME		this.considerItem( name, item, "Aluminum", 26.9815 );
// FIXME		this.considerItem( name, item, "SILICON", 28.0855 );
// FIXME		this.considerItem( name, item, "Phosphorus", 30.9738 );
// FIXME		this.considerItem( name, item, "Sulfur", 32.065 );
// FIXME		this.considerItem( name, item, "Potassium", 39.0983 );
// FIXME		this.considerItem( name, item, "Calcium", 40.078 );
// FIXME		this.considerItem( name, item, "Scandium", 44.9559 );
// FIXME		this.considerItem( name, item, "Titanium", 47.867 );
// FIXME		this.considerItem( name, item, "Vanadium", 50.9415 );
// FIXME		this.considerItem( name, item, "Manganese", 54.938 );
// FIXME		this.considerItem( name, item, "Iron", 55.845 );
// FIXME		this.considerItem( name, item, "Nickel", 58.6934 );
// FIXME		this.considerItem( name, item, "Cobalt", 58.9332 );
// FIXME		this.considerItem( name, item, "Copper", 63.546 );
// FIXME		this.considerItem( name, item, "Zinc", 65.39 );
// FIXME		this.considerItem( name, item, "Gallium", 69.723 );
// FIXME		this.considerItem( name, item, "Germanium", 72.64 );
// FIXME		this.considerItem( name, item, "Bromine", 79.904 );
// FIXME		this.considerItem( name, item, "Krypton", 83.8 );
// FIXME		this.considerItem( name, item, "Rubidium", 85.4678 );
// FIXME		this.considerItem( name, item, "Strontium", 87.62 );
// FIXME		this.considerItem( name, item, "Yttrium", 88.9059 );
// FIXME		this.considerItem( name, item, "Zirconiumm", 91.224 );
// FIXME		this.considerItem( name, item, "Niobiumm", 92.9064 );
// FIXME		this.considerItem( name, item, "Technetium", 98 );
// FIXME		this.considerItem( name, item, "Ruthenium", 101.07 );
// FIXME		this.considerItem( name, item, "Rhodium", 102.9055 );
// FIXME		this.considerItem( name, item, "Palladium", 106.42 );
// FIXME		this.considerItem( name, item, "Silver", 107.8682 );
// FIXME		this.considerItem( name, item, "Cadmium", 112.411 );
// FIXME		this.considerItem( name, item, "Indium", 114.818 );
// FIXME		this.considerItem( name, item, "Tin", 118.71 );
// FIXME		this.considerItem( name, item, "Antimony", 121.76 );
// FIXME		this.considerItem( name, item, "Iodine", 126.9045 );
// FIXME		this.considerItem( name, item, "Tellurium", 127.6 );
// FIXME		this.considerItem( name, item, "Xenon", 131.293 );
// FIXME		this.considerItem( name, item, "Cesium", 132.9055 );
// FIXME		this.considerItem( name, item, "Barium", 137.327 );
// FIXME		this.considerItem( name, item, "Lanthanum", 138.9055 );
// FIXME		this.considerItem( name, item, "Cerium", 140.116 );
// FIXME		this.considerItem( name, item, "Tantalum", 180.9479 );
// FIXME		this.considerItem( name, item, "Tungsten", 183.84 );
// FIXME		this.considerItem( name, item, "Osmium", 190.23 );
// FIXME		this.considerItem( name, item, "Iridium", 192.217 );
// FIXME		this.considerItem( name, item, "Platinum", 195.078 );
// FIXME		this.considerItem( name, item, "Lead", 207.2 );
// FIXME		this.considerItem( name, item, "Bismuth", 208.9804 );
// FIXME		this.considerItem( name, item, "Uranium", 238.0289 );
// FIXME		this.considerItem( name, item, "Plutonium", 244 );
// FIXME
// FIXME		// TE stuff...
// FIXME		this.considerItem( name, item, "Invar", ( 58.6934 + 55.845 + 55.845 ) / 3.0 );
// FIXME		this.considerItem( name, item, "Electrum", ( 107.8682 + 196.96655 ) / 2.0 );
// FIXME	}

	private void considerItem( final String ore, final ItemStack item, final String name, final double weight )
	{
		if( ore.equals( "berry" + name ) || ore.equals( "nugget" + name ) )
		{
			this.registerAmmo( item, weight );
		}
	}
}
