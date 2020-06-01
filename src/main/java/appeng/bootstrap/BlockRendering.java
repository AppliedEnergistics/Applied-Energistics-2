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


import appeng.block.AEBaseTileBlock;
import appeng.bootstrap.components.BlockColorComponent;
import appeng.bootstrap.components.RenderTypeComponent;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;


class BlockRendering implements IBlockRendering
{

	@OnlyIn( Dist.CLIENT )
	private BiFunction<ResourceLocation, IBakedModel, IBakedModel> modelCustomizer;

	@OnlyIn( Dist.CLIENT )
	private IBlockColor blockColor;

	@OnlyIn( Dist.CLIENT )
	private TileEntityRenderer<?> tesr;

//	FIXME @OnlyIn( Dist.CLIENT )
//	FIXME private IStateMapper stateMapper;

	@OnlyIn( Dist.CLIENT )
	private Map<String, IUnbakedModel> builtInModels = new HashMap<>();

	@OnlyIn( Dist.CLIENT )
	private RenderType renderType;

	@Override
	@OnlyIn( Dist.CLIENT )
	public IBlockRendering modelCustomizer( BiFunction<ResourceLocation, IBakedModel, IBakedModel> customizer )
	{
		this.modelCustomizer = customizer;
		return this;
	}

	@OnlyIn( Dist.CLIENT )
	@Override
	public IBlockRendering blockColor( IBlockColor blockColor )
	{
		this.blockColor = blockColor;
		return this;
	}

	@OnlyIn( Dist.CLIENT )
	@Override
	public IBlockRendering tesr( TileEntityRenderer<?> tesr )
	{
		this.tesr = tesr;
		return this;
	}

	@Override
	public IBlockRendering builtInModel( String name, IUnbakedModel model )
	{
		this.builtInModels.put( name, model );
		return this;
	}

	@Override
	public IBlockRendering renderType(RenderType type) {
		this.renderType = type;
		return this;
	}

	// FIXME	@OnlyIn( Dist.CLIENT )
// FIXME	@Override
// FIXME	public IBlockRendering stateMapper( IStateMapper mapper )
// FIXME	{
// FIXME		this.stateMapper = mapper;
// FIXME		return this;
// FIXME	}

	void apply( FeatureFactory factory, Block block, Class<?> tileEntityClass )
	{
// FIXME		if( this.tesr != null )
// FIXME		{
// FIXME			if( tileEntityClass == null )
// FIXME			{
// FIXME				throw new IllegalStateException( "Tried to register a TESR for " + block + " even though no tile entity has been specified." );
// FIXME			}
// FIXME			factory.addBootstrapComponent( new TesrComponent( tileEntityClass, this.tesr ) );
// FIXME		}

		if( this.modelCustomizer != null )
		{
			factory.addModelOverride( block.getRegistryName().getPath(), this.modelCustomizer );
		}
		else if( block instanceof AEBaseTileBlock )
		{
			// This is a default rotating model if the base-block uses an AE tile entity which exposes UP/FRONT as
			// extended props
			// FIXME factory.addModelOverride( block.getRegistryName().getPath(), ( l, m ) -> new AutoRotatingModel( m ) );
		}

		// TODO : 1.12
		this.builtInModels.forEach( factory::addBuiltInModel );

		if( this.blockColor != null )
		{
			factory.addBootstrapComponent( new BlockColorComponent( block, this.blockColor ) );
		}

		if (this.renderType != null) {
			factory.addBootstrapComponent( new RenderTypeComponent( block, this.renderType));
		}

	// FIXME	if( this.stateMapper != null )
	// FIXME	{
	// FIXME		factory.addBootstrapComponent( new StateMapperComponent( block, this.stateMapper ) );
	// FIXME	}
	}
}
