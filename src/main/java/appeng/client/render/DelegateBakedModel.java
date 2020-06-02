/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.client.render;


import com.mojang.blaze3d.matrix.MatrixStack;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;


public abstract class DelegateBakedModel implements IBakedModel
{
	private IBakedModel baseModel;

	protected DelegateBakedModel( IBakedModel base )
	{
		this.baseModel = base;
	}

	@Override
	public IBakedModel handlePerspective( ItemCameraTransforms.TransformType cameraTransformType, MatrixStack mat )
	{
		baseModel.handlePerspective( cameraTransformType, mat );
		return this;
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return this.baseModel.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return this.baseModel.getItemCameraTransforms();
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return this.baseModel.isAmbientOcclusion();
	}

	public IBakedModel getBaseModel()
	{
		return this.baseModel;
	}
}
