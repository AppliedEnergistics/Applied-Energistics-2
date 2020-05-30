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


import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.EffectType;
import appeng.core.AppEng;
import appeng.entity.EntityFloatingItem;
import appeng.entity.ICanDie;


public class AssemblerFX extends Particle implements ICanDie
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
		final ItemStack displayItem = is.asItemStackRepresentation();
		this.fi = new EntityFloatingItem( this, w, x, y, z, displayItem );
		w.addEntity( this.fi );
		this.maxAge = (int) Math.ceil( Math.max( 1, 100.0f / speed ) ) + 2;
	}

	@Override
	public boolean isDead()
	{
		return this.isExpired;
	}

	@Override
	public int getBrightnessForRender( final float par1 )
	{
		final int j1 = 13;
		return j1 << 20 | j1 << 4;
	}

	@Override
	public void tick()
	{
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if( this.age++ >= this.maxAge )
		{
			this.setExpired();
		}

		this.motionY -= 0.04D * this.particleGravity;
		this.move( this.motionX, this.motionY, this.motionZ );
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		if( this.isExpired )
		{
			this.fi.remove();
		}
		else
		{
			final float lifeSpan = (float) this.age / (float) this.maxAge;
			this.fi.setProgress( lifeSpan );
		}
	}

	@Override
	public IParticleRenderType getRenderType() {
		// TODO: FIXME
		return IParticleRenderType.NO_RENDER;
	}

	@Override
	public void renderParticle( IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks )
	{
		this.time += partialTicks;
		if( this.time > 4.0 )
		{
			this.time -= 4.0;
			// if ( AppEng.proxy.shouldAddParticles( r ) )
			for( int x = 0; x < (int) Math.ceil( this.speed / 5 ); x++ )
			{
				AppEng.proxy.spawnEffect( EffectType.Crafting, this.world, this.posX, this.posY, this.posZ, null );
			}
		}
	}
}
