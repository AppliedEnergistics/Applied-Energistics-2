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

package appeng.bootstrap;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.bootstrap.components.ItemColorComponent;
import appeng.bootstrap.components.ItemVariantsComponent;


class ItemRendering implements IItemRendering
{

	@OnlyIn( Dist.CLIENT )
	private IItemColor itemColor;

	@OnlyIn( Dist.CLIENT )
	private Set<ResourceLocation> variants = new HashSet<>();

	@Override
	public IItemRendering variants( Collection<ResourceLocation> resources )
	{
		this.variants.addAll( resources );
		return this;
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public IItemRendering color( IItemColor itemColor )
	{
		this.itemColor = itemColor;
		return this;
	}

	void apply( FeatureFactory factory, Item item )
	{
		Set<ResourceLocation> resources = new HashSet<>( this.variants );

		if( !resources.isEmpty() )
		{
			factory.addBootstrapComponent( new ItemVariantsComponent( item, resources ) );
		}

		if( this.itemColor != null )
		{
			factory.addBootstrapComponent( new ItemColorComponent( item, this.itemColor ) );
		}
	}

}
