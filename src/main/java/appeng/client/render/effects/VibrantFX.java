/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.client.render.effects;


import net.minecraft.client.particle.EntityFX;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


@SideOnly( Side.CLIENT )
public final class VibrantFX extends EntityFX
{

	public VibrantFX( World par1World, double x, double y, double z, double par8, double par10, double par12 )
	{
		super( par1World, x, y, z, par8, par10, par12 );
		float f = this.rand.nextFloat() * 0.1F + 0.8F;
		this.particleRed = f * 0.7f;
		this.particleGreen = f * 0.89f;
		this.particleBlue = f * 0.9f;
		this.setParticleTextureIndex( 0 );
		this.setSize( 0.04F, 0.04F );
		this.particleScale *= this.rand.nextFloat() * 0.6F + 1.9F;
		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.particleMaxAge = (int) ( 20.0D / ( Math.random() * 0.8D + 0.1D ) );
		this.noClip = true;
	}

	@Override
	public final float getBrightness( float par1 )
	{
		return 1.0f;
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public final void onUpdate()
	{
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		// this.moveEntity(this.motionX, this.motionY, this.motionZ);
		this.particleScale *= 0.95;

		if( this.particleMaxAge <= 0 || this.particleScale < 0.1 )
		{
			this.setDead();
		}
		this.particleMaxAge--;
	}
}
