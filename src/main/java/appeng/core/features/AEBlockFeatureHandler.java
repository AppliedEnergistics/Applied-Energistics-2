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

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import appeng.api.definitions.IBlockDefinition;
import appeng.block.AEBaseBlock;
import appeng.block.IHasSpecialItemModel;
import appeng.client.render.model.AEIgnoringStateMapper;
import appeng.core.AppEng;
import appeng.core.CreativeTab;


public final class AEBlockFeatureHandler implements IFeatureHandler
{
	private final Block featured;
	private final FeatureNameExtractor extractor;
	private final boolean enabled;
	private final BlockDefinition definition;

	private ResourceLocation registryName;

	public AEBlockFeatureHandler( final EnumSet<AEFeature> features, final Block featured, final Optional<String> subName )
	{
		final ActivityState state = new FeaturedActiveChecker( features ).getActivityState();

		this.featured = featured;
		this.extractor = new FeatureNameExtractor( featured.getClass(), subName );
		this.enabled = state == ActivityState.Enabled;
		// TODO use real identifier
		this.definition = new BlockDefinition( featured.getClass().getSimpleName(), featured, state );
	}

	@Override
	public boolean isFeatureAvailable()
	{
		return this.enabled;
	}

	@Override
	public IBlockDefinition getDefinition()
	{
		return this.definition;
	}

	@Override
	public void register( final Side side )
	{
		if( this.enabled )
		{
			String name = this.extractor.get();
			if( Item.REGISTRY.containsKey( new ResourceLocation( AppEng.MOD_ID, name ) ) )
			{
				name += "_block";
			}
			this.featured.setCreativeTab( CreativeTab.instance );
			this.featured.setUnlocalizedName( "appliedenergistics2." + name );

			registryName = new ResourceLocation( AppEng.MOD_ID, name );
			GameRegistry.register( this.featured.setRegistryName( registryName ) );

			// register the block/item conversion...
			if( this.definition.maybeItem().isPresent() )
			{
				GameRegistry.register( this.definition.maybeItem().get().setRegistryName( registryName ) );

				if( side == Side.CLIENT )
				{
					ModelBakery.registerItemVariants( this.definition.maybeItem().get(), registryName );
				}
			}
		}
	}

	@Override
	public void registerModel()
	{
		ItemModelMesher itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		Item item = this.definition.maybeItem().get();

		// Retrieve a custom item mesh definition, if the block defines one
		ItemMeshDefinition itemMeshDefinition = null;
		if( featured instanceof AEBaseBlock )
		{
			itemMeshDefinition = ( (AEBaseBlock) featured ).getItemMeshDefinition();
		}

		if ( itemMeshDefinition != null )
		{
			// This block has a custom item mesh definition, so register it instead of the resource location
			itemModelMesher.register( item, itemMeshDefinition );
		}
		else if( !featured.getBlockState().getProperties().isEmpty() || featured instanceof IHasSpecialItemModel )
		{
			itemModelMesher.register( item, 0, new ModelResourceLocation( registryName, "inventory" ) );
		}
		else
		{
			itemModelMesher.register( item, 0, new ModelResourceLocation( registryName, "normal" ) );
		}
	}

	@Override
	public void registerStateMapper()
	{
		AEIgnoringStateMapper mapper = new AEIgnoringStateMapper( registryName );
		ModelLoader.setCustomStateMapper( this.featured, mapper );
		( (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager() ).registerReloadListener( mapper );
	}

	@Override
	public void registerCustomModelOverride( IRegistry<ModelResourceLocation, IBakedModel> modelRegistry )
	{

	}
}
