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

package appeng.core.features;


import java.util.EnumSet;

import net.minecraft.item.Item;

import cpw.mods.fml.common.registry.GameRegistry;

import com.google.common.base.Optional;

import appeng.api.util.AEItemDefinition;
import appeng.core.CreativeTab;
import appeng.core.CreativeTabFacade;
import appeng.items.parts.ItemFacade;


public class ItemFeatureHandler implements IFeatureHandler
{
	private final EnumSet<AEFeature> features;
	private final Item item;
	private final FeatureNameExtractor extractor;
	private final boolean enabled;
	private final ItemDefinition definition;

	public ItemFeatureHandler( EnumSet<AEFeature> features, Item item, IAEFeature featured, Optional<String> subName )
	{
		this.features = features;
		this.item = item;
		this.extractor = new FeatureNameExtractor( featured.getClass(), subName );
		this.enabled = new FeaturedActiveChecker( features ).get();
		this.definition = new ItemDefinition( item, this.enabled );
	}

	@Override
	public boolean isFeatureAvailable()
	{
		return this.enabled;
	}

	@Override
	public EnumSet<AEFeature> getFeatures()
	{
		return this.features;
	}

	@Override
	public AEItemDefinition getDefinition()
	{
		return this.definition;
	}

	@Override
	public void register()
	{
		String name = this.extractor.get();
		this.item.setTextureName( "appliedenergistics2:" + name );
		this.item.setUnlocalizedName( /* "item." */"appliedenergistics2." + name );

		if( this.item instanceof ItemFacade )
		{
			this.item.setCreativeTab( CreativeTabFacade.instance );
		}
		else
		{
			this.item.setCreativeTab( CreativeTab.instance );
		}

		if( name.equals( "ItemMaterial" ) )
		{
			name = "ItemMultiMaterial";
		}
		else if( name.equals( "ItemPart" ) )
		{
			name = "ItemMultiPart";
		}

		GameRegistry.registerItem( this.item, "item." + name );
	}
}
