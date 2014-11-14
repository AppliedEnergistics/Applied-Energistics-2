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

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelCompass extends ModelBase
{

	final ModelRenderer Ring1;
	final ModelRenderer Ring2;
	final ModelRenderer Ring3;
	final ModelRenderer Ring4;
	final ModelRenderer Middle;
	final ModelRenderer Base;

	final ModelRenderer Pointer;

	public ModelCompass() {
		textureWidth = 16;
		textureHeight = 8;

		Ring1 = new ModelRenderer( this, 0, 0 );
		Ring1.addBox( 0F, 0F, 0F, 4, 1, 1 );
		Ring1.setRotationPoint( -2F, 22F, 2F );
		Ring1.setTextureSize( 16, 8 );
		Ring1.mirror = true;
		setRotation( Ring1, 0F, 0F, 0F );

		Ring2 = new ModelRenderer( this, 0, 0 );
		Ring2.addBox( 0F, 0F, 0F, 1, 1, 4 );
		Ring2.setRotationPoint( -3F, 22F, -2F );
		Ring2.setTextureSize( 16, 8 );
		Ring2.mirror = true;
		setRotation( Ring2, 0F, 0F, 0F );

		Ring3 = new ModelRenderer( this, 0, 0 );
		Ring3.addBox( 0F, 0F, 0F, 4, 1, 1 );
		Ring3.setRotationPoint( -2F, 22F, -3F );
		Ring3.setTextureSize( 16, 8 );
		Ring3.mirror = true;
		setRotation( Ring3, 0F, 0F, 0F );

		Ring4 = new ModelRenderer( this, 0, 0 );
		Ring4.addBox( 0F, 0F, 0F, 1, 1, 4 );
		Ring4.setRotationPoint( 2F, 22F, -2F );
		Ring4.setTextureSize( 16, 8 );
		Ring4.mirror = true;
		setRotation( Ring4, 0F, 0F, 0F );

		Middle = new ModelRenderer( this, 0, 0 );
		Middle.addBox( 0F, 0F, 0F, 1, 1, 1 );
		Middle.setRotationPoint( -0.5333334F, 22F, -0.5333334F );
		Middle.setTextureSize( 16, 8 );
		Middle.mirror = true;
		setRotation( Middle, 0F, 0F, 0F );

		Pointer = new ModelRenderer( this, 0, 0 );
		Pointer.setTextureOffset( 0, 5 );
		Pointer.addBox( -0.5F, 0F, 0F, 1, 1, 2 );
		Pointer.setRotationPoint( 0.5F, 22.5F, 0.5F );
		Pointer.setTextureSize( 16, 8 );
		Pointer.mirror = true;
		Pointer.offsetZ = -0.034f;
		Pointer.offsetX = -0.034f;
		setRotation( Pointer, 0F, 0F, 0F );

		Base = new ModelRenderer( this, 0, 0 );
		Base.addBox( 0F, 0F, 0F, 4, 1, 4 );
		Base.setRotationPoint( -2F, 23F, -2F );
		Base.setTextureSize( 16, 8 );
		Base.mirror = true;
		setRotation( Base, 0F, 0F, 0F );
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void renderAll(float rad)
	{
		setRotation( Pointer, 0F, 0F, 0F );

		Pointer.rotateAngleY = rad;

		Base.render( 0.0625F );
		Middle.render( 0.0625F );

		Pointer.render( 0.0625F );

		Ring1.render( 0.0625F );
		Ring2.render( 0.0625F );
		Ring3.render( 0.0625F );
		Ring4.render( 0.0625F );
	}
}
