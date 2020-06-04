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

package appeng.client.render.model;


import java.util.List;
import java.util.Random;

import appeng.client.render.cablebus.QuadRotator;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import appeng.block.AEBaseTileBlock;
import appeng.client.render.FacingToRotation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class AutoRotatingBakedModel implements IBakedModel
{

	private final IBakedModel parent;
	private final LoadingCache<AutoRotatingCacheKey, List<BakedQuad>> quadCache;

	public AutoRotatingBakedModel(IBakedModel parent )
	{
		this.parent = parent;
		// 6 (DUNSWE) * 6 (DUNSWE) * 7 (DUNSWE + null) = 252
		this.quadCache = CacheBuilder.newBuilder().maximumSize( 252 ).build( new CacheLoader<AutoRotatingCacheKey, List<BakedQuad>>()
		{
			@Override
			public List<BakedQuad> load( AutoRotatingCacheKey key ) throws Exception
			{
				return AutoRotatingBakedModel.this.getRotatedModel( key.getBlockState(), key.getSide(), key.getForward(), key.getUp() );
			}
		} );
	}

	private List<BakedQuad> getRotatedModel( BlockState state, Direction side, Direction forward, Direction up )
	{
		FacingToRotation f2r = FacingToRotation.get( forward, up );
		List<BakedQuad> original = AutoRotatingBakedModel.this.parent.getQuads( state, f2r.resultingRotate( side ), new Random(0), EmptyModelData.INSTANCE );
		return new QuadRotator().rotateQuads(original, forward, up);
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return this.parent.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return this.parent.isGui3d();
	}

	@Override
	public boolean func_230044_c_() {
		return parent.func_230044_c_();
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return this.parent.isBuiltInRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return this.parent.getParticleTexture();
	}

	@Override
	@Deprecated
	public ItemCameraTransforms getItemCameraTransforms() {
		return parent.getItemCameraTransforms();
	}

	@Override
	public ItemOverrideList getOverrides() {
		return parent.getOverrides();
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
		return getQuads(state, side, rand, EmptyModelData.INSTANCE);
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {

		Direction forward = extraData.getData(AEBaseTileBlock.FORWARD);
		Direction up = extraData.getData(AEBaseTileBlock.UP);

		if( forward == null || up == null )
		{
			return this.parent.getQuads( state, side, rand );
		}

		// The model has other properties than just forward/up, so it would cause our cache to inadvertendly also cache
		// these
		// additional states, possibly leading to huge issues if the other extended state properties do not implement
		// equals/hashCode correctly
		// FIXME: IModelData does not expose a way for us to check if it only has the two properties and no other
		return this.getRotatedModel( state, side, forward, up );

	}

}
