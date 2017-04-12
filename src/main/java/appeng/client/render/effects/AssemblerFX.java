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

package appeng.client.render.effects;


import appeng.api.storage.data.IAEItemStack;
import appeng.client.EffectType;
import appeng.core.CommonHelper;
import appeng.entity.EntityFloatingItem;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;


public class AssemblerFX extends EntityFX
{

	private final EntityFloatingItem fi;
	private final float speed;
	private float time = 0;

	public AssemblerFX( final World w, final double x, final double y, final double z, final double r, final double g, final double b, final float speed, final IAEItemStack is )
	{
		super( w, x, y, z, r, g, b );
		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;
		this.speed = speed;
		this.fi = new EntityFloatingItem( this, w, x, y, z, is.getItemStack() );
		w.spawnEntityInWorld( this.fi );
		this.particleMaxAge = (int) Math.ceil( Math.max( 1, 100.0f / speed ) ) + 2;
		this.noClip = true;
	}

	@Override
	public int getBrightnessForRender( final float par1 )
	{
		final int j1 = 13;
		return j1 << 20 | j1 << 4;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if( this.isDead )
		{
			this.fi.setDead();
		}
		else
		{
			final float lifeSpan = (float) this.particleAge / (float) this.particleMaxAge;
			this.fi.setProgress( lifeSpan );
		}
	}

	@Override
	public void renderParticle( final Tessellator tess, final float l, final float rX, final float rY, final float rZ, final float rYZ, final float rXY )
	{
		this.time += l;

		if( this.time > 4.0 )
		{
			this.time -= 4.0;
			// if ( CommonHelper.proxy.shouldAddParticles( r ) )
			for( int x = 0; x < (int) Math.ceil( this.speed / 5 ); x++ )
			{
				CommonHelper.proxy.spawnEffect( EffectType.Crafting, this.worldObj, this.posX, this.posY, this.posZ, null );
			}
		}
	}
}
