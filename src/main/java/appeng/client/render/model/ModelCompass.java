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

	private final ModelRenderer Ring1;
	private final ModelRenderer Ring2;
	private final ModelRenderer Ring3;
	private final ModelRenderer Ring4;
	private final ModelRenderer Middle;
	private final ModelRenderer Base;

	private final ModelRenderer Pointer;

	public ModelCompass()
	{
		this.textureWidth = 16;
		this.textureHeight = 8;

		this.Ring1 = new ModelRenderer( this, 0, 0 );
		this.Ring1.addBox( 0F, 0F, 0F, 4, 1, 1 );
		this.Ring1.setRotationPoint( -2F, 22F, 2F );
		this.Ring1.setTextureSize( 16, 8 );
		this.Ring1.mirror = true;
		this.setRotation( this.Ring1, 0F, 0F, 0F );

		this.Ring2 = new ModelRenderer( this, 0, 0 );
		this.Ring2.addBox( 0F, 0F, 0F, 1, 1, 4 );
		this.Ring2.setRotationPoint( -3F, 22F, -2F );
		this.Ring2.setTextureSize( 16, 8 );
		this.Ring2.mirror = true;
		this.setRotation( this.Ring2, 0F, 0F, 0F );

		this.Ring3 = new ModelRenderer( this, 0, 0 );
		this.Ring3.addBox( 0F, 0F, 0F, 4, 1, 1 );
		this.Ring3.setRotationPoint( -2F, 22F, -3F );
		this.Ring3.setTextureSize( 16, 8 );
		this.Ring3.mirror = true;
		this.setRotation( this.Ring3, 0F, 0F, 0F );

		this.Ring4 = new ModelRenderer( this, 0, 0 );
		this.Ring4.addBox( 0F, 0F, 0F, 1, 1, 4 );
		this.Ring4.setRotationPoint( 2F, 22F, -2F );
		this.Ring4.setTextureSize( 16, 8 );
		this.Ring4.mirror = true;
		this.setRotation( this.Ring4, 0F, 0F, 0F );

		this.Middle = new ModelRenderer( this, 0, 0 );
		this.Middle.addBox( 0F, 0F, 0F, 1, 1, 1 );
		this.Middle.setRotationPoint( -0.5333334F, 22F, -0.5333334F );
		this.Middle.setTextureSize( 16, 8 );
		this.Middle.mirror = true;
		this.setRotation( this.Middle, 0F, 0F, 0F );

		this.Pointer = new ModelRenderer( this, 0, 0 );
		this.Pointer.setTextureOffset( 0, 5 );
		this.Pointer.addBox( -0.5F, 0F, 0F, 1, 1, 2 );
		this.Pointer.setRotationPoint( 0.5F, 22.5F, 0.5F );
		this.Pointer.setTextureSize( 16, 8 );
		this.Pointer.mirror = true;
		this.Pointer.offsetZ = -0.034f;
		this.Pointer.offsetX = -0.034f;
		this.setRotation( this.Pointer, 0F, 0F, 0F );

		this.Base = new ModelRenderer( this, 0, 0 );
		this.Base.addBox( 0F, 0F, 0F, 4, 1, 4 );
		this.Base.setRotationPoint( -2F, 23F, -2F );
		this.Base.setTextureSize( 16, 8 );
		this.Base.mirror = true;
		this.setRotation( this.Base, 0F, 0F, 0F );
	}

	private void setRotation( final ModelRenderer model, final float x, final float y, final float z )
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void renderAll( final float rad )
	{
		this.setRotation( this.Pointer, 0F, 0F, 0F );

		this.Pointer.rotateAngleY = rad;

		this.Base.render( 0.0625F );
		this.Middle.render( 0.0625F );

		this.Pointer.render( 0.0625F );

		this.Ring1.render( 0.0625F );
		this.Ring2.render( 0.0625F );
		this.Ring3.render( 0.0625F );
		this.Ring4.render( 0.0625F );
	}
}
