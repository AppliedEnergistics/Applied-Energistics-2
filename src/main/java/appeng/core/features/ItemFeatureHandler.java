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

import com.google.common.base.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import appeng.api.definitions.IItemDefinition;
import appeng.core.AppEng;
import appeng.core.CreativeTab;
import appeng.core.CreativeTabFacade;
import appeng.items.AEBaseItem;
import appeng.items.parts.ItemFacade;


public final class ItemFeatureHandler implements IFeatureHandler
{
	private final Item item;
	private final FeatureNameExtractor extractor;
	private final boolean enabled;
	private final ItemDefinition definition;

	private ResourceLocation registryName;

	public ItemFeatureHandler( final EnumSet<AEFeature> features, final Item item, final IAEFeature featured, final Optional<String> subName )
	{
		final ActivityState state = new FeaturedActiveChecker( features ).getActivityState();

		this.item = item;
		this.extractor = new FeatureNameExtractor( featured.getClass(), subName );
		this.enabled = state == ActivityState.Enabled;
		this.definition = new ItemDefinition( item.getClass().getSimpleName(), item, state );
	}

	@Override
	public boolean isFeatureAvailable()
	{
		return this.enabled;
	}

	@Override
	public IItemDefinition getDefinition()
	{
		return this.definition;
	}

	@Override
	public void register( final Side side )
	{
		if( this.enabled )
		{
			String name = this.extractor.get();
			final String itemPhysicalName = name;

			// this.item.setTextureName( "appliedenergistics2:" + name );
			this.item.setUnlocalizedName( "appliedenergistics2." + name );

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

			registryName = new ResourceLocation( AppEng.MOD_ID, name );
			GameRegistry.register( this.item.setRegistryName( registryName ) );

			if( side == Side.CLIENT )
			{
				if( item instanceof AEBaseItem )
				{
					AEBaseItem baseItem = (AEBaseItem) item;

					// Handle registration of item variants
					ResourceLocation[] variants = baseItem.getItemVariants().toArray( new ResourceLocation[0] );
					ModelBakery.registerItemVariants( item, variants );
				}
				else
				{
					ModelBakery.registerItemVariants( item, registryName );
				}
			}
		}
	}

	@Override
	public void registerModel()
	{
		ItemMeshDefinition meshDefinition = null;

		// Register a custom item model handler if the item wants one
		if( item instanceof AEBaseItem )
		{
			AEBaseItem baseItem = (AEBaseItem) item;
			meshDefinition = baseItem.getItemMeshDefinition();
		}

		if( meshDefinition != null )
		{
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register( item, meshDefinition );
		}
		else
		{
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register( item, 0, new ModelResourceLocation( registryName, "inventory" ) );
		}
	}

	@Override
	public void registerStateMapper()
	{

	}

	@Override
	public void registerCustomModelOverride( IRegistry<ModelResourceLocation, IBakedModel> modelRegistry )
	{

	}
}
