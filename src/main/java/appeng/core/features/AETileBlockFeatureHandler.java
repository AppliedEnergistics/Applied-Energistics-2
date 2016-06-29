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
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import appeng.api.definitions.ITileDefinition;
import appeng.block.AEBaseTileBlock;
import appeng.core.AppEng;
import appeng.core.CommonHelper;
import appeng.core.CreativeTab;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;


public final class AETileBlockFeatureHandler implements IFeatureHandler
{
	private final AEBaseTileBlock featured;
	private final FeatureNameExtractor extractor;
	private final boolean enabled;
	private final TileDefinition definition;

	private ResourceLocation registryName;

	public AETileBlockFeatureHandler( final EnumSet<AEFeature> features, final AEBaseTileBlock featured, final Optional<String> subName )
	{
		final ActivityState state = new FeaturedActiveChecker( features ).getActivityState();

		this.featured = featured;
		this.extractor = new FeatureNameExtractor( featured.getClass(), subName );
		this.enabled = state == ActivityState.Enabled;
		this.definition = new TileDefinition( featured.getClass().getSimpleName(), featured, state );
	}

	@Override
	public boolean isFeatureAvailable()
	{
		return this.enabled;
	}

	@Override
	public ITileDefinition getDefinition()
	{
		return this.definition;
	}

	@Override
	public void register( final Side side )
	{
		if( this.enabled )
		{
			final String name = this.extractor.get();
			this.featured.setCreativeTab( CreativeTab.instance );
			this.featured.setUnlocalizedName( /* "tile." */"appliedenergistics2." + name );
			this.featured.setBlockTextureName( name );

			if( Platform.isClient() )
			{
				CommonHelper.proxy.bindTileEntitySpecialRenderer( this.featured.getTileEntityClass(), this.featured );
			}

			registryName = new ResourceLocation( AppEng.MOD_ID, "tile." + name );

			GameRegistry.register( this.featured.setRegistryName( registryName ) );
			GameRegistry.register( this.definition.maybeItem().get().setRegistryName( registryName ) );
			AEBaseTile.registerTileItem( this.featured.getTileEntityClass(), new BlockStackSrc( this.featured, 0, ActivityState.from( this.isFeatureAvailable() ) ) );

			GameRegistry.registerTileEntity( this.featured.getTileEntityClass(), this.featured.toString() );

			if( side == Side.CLIENT )
			{
				ModelBakery.registerItemVariants( this.definition.maybeItem().get(), registryName );
			}
		}
	}

	@Override
	public void registerModel()
	{
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register( this.definition.maybeItem().get(), 0, new ModelResourceLocation( registryName, "normal" ) );
	}
}
