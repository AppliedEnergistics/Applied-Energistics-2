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
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import appeng.api.client.BakingPipeline;
import appeng.api.definitions.ITileDefinition;
import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.block.networking.BlockCableBus;
import appeng.client.render.model.pipeline.BakingPipelineBakedModel;
import appeng.client.render.model.pipeline.FacingQuadRotator;
import appeng.client.render.model.pipeline.Merge;
import appeng.client.render.model.pipeline.TintIndexModifier;
import appeng.client.render.model.pipeline.TypeTransformer;
import appeng.client.render.model.pipeline.cable.CableAndConnections;
import appeng.client.render.model.pipeline.cable.Facades;
import appeng.client.render.model.pipeline.cable.Parts;
import appeng.core.AppEng;
import appeng.core.CommonHelper;
import appeng.core.CreativeTab;
import appeng.parts.CableBusContainer;
import appeng.util.Platform;


public final class AECableBusFeatureHandler implements IFeatureHandler
{
	private final AEBaseTileBlock featured;
	private final FeatureNameExtractor extractor;
	private final boolean enabled;
	private final TileDefinition definition;

	private ResourceLocation registryName;

	public AECableBusFeatureHandler( final EnumSet<AEFeature> features, final BlockCableBus featured, final Optional<String> subName )
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

	/**
	 * Registration of the {@link TileEntity} will actually be handled by {@link BlockCableBus#setupTile()}.
	 */
	@Override
	public void register( final Side side )
	{
		if( this.enabled )
		{
			final String name = this.extractor.get();
			this.featured.setCreativeTab( CreativeTab.instance );
			this.featured.setUnlocalizedName( "appliedenergistics2." + name );

			if( Platform.isClient() )
			{
				CommonHelper.proxy.bindTileEntitySpecialRenderer( this.featured.getTileEntityClass(), this.featured );
			}

			registryName = new ResourceLocation( AppEng.MOD_ID, name );

			// Bypass the forge magic with null to register our own itemblock later.
			GameRegistry.register( this.featured.setRegistryName( registryName ) );
			GameRegistry.register( this.definition.maybeItem().get().setRegistryName( registryName ) );

			if( side == Side.CLIENT )
			{
				ModelBakery.registerItemVariants( this.definition.maybeItem().get(), registryName );
			}
		}
	}

	@Override
	public void registerModel()
	{
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler( new CableBusColor(), this.featured );
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register( this.definition.maybeItem().get(), 0, new ModelResourceLocation( registryName, "normal" ) );
	}

	@Override
	public void registerStateMapper()
	{

	}

	@Override
	public void registerCustomModelOverride( IRegistry<ModelResourceLocation, IBakedModel> modelRegistry )
	{
		final BakingPipeline rotatingPipeline = new BakingPipeline( TypeTransformer.quads2vecs, new FacingQuadRotator(), TypeTransformer.vecs2quads );
		final TintIndexModifier tintIndexModifier = new TintIndexModifier( tint -> tint );
		final BakingPipeline tintIndexFixPipeline = new BakingPipeline( TypeTransformer.quads2vecs, tintIndexModifier, TypeTransformer.vecs2quads );
		Set<ModelResourceLocation> keys = Sets.newHashSet( modelRegistry.getKeys() );
		for( ModelResourceLocation model : keys )
		{
			if( model.getResourceDomain().equals( registryName.getResourceDomain() ) && model.getResourcePath().equals( registryName.getResourcePath() ) )
			{
				modelRegistry.putObject( model, new BakingPipelineBakedModel( modelRegistry.getObject( model ), new Merge( new CableAndConnections( rotatingPipeline, tintIndexModifier, tintIndexFixPipeline ), new Facades( rotatingPipeline, tintIndexModifier, tintIndexFixPipeline ), new Parts( rotatingPipeline, tintIndexModifier, tintIndexFixPipeline ) ) ) );
			}
		}
	}

	public static class CableBusColor implements IBlockColor
	{

		@Override
		public int colorMultiplier( IBlockState state, IBlockAccess worldIn, BlockPos pos, int color )
		{
			AEPartLocation side = AEPartLocation.fromOrdinal( ( color >> 2 ) & 7 );
			CableBusContainer bus = ( (IExtendedBlockState) state ).getValue( BlockCableBus.cableBus );
			switch( color & 3 )
			{
				case 0:
					return bus.getGridNode( side ) != null && bus.getGridNode( side ).isActive() ? 0xffffff : 0;
				case 1:
					return bus.getColor().blackVariant;
				case 2:
					return bus.getColor().mediumVariant;
				case 3:
					return bus.getColor().whiteVariant;
				default:
					return color;
			}
		}
	}
}
