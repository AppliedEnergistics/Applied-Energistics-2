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

import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.EffectType;
import appeng.core.CommonHelper;
import appeng.entity.EntityFloatingItem;

public class AssemblerFX extends EntityFX
{

	final IAEItemStack item;
	final EntityFloatingItem fi;
	float time = 0;
	final float speed;

	public AssemblerFX(World w, double x, double y, double z, double r, double g, double b, float speed, IAEItemStack is) {
		super( w, x, y, z, r, g, b );
		motionX = 0;
		motionY = 0;
		motionZ = 0;
		item = is;
		this.speed = speed;
		fi = new EntityFloatingItem( this, w, x, y, z, is.getItemStack() );
		w.spawnEntityInWorld( fi );
		particleMaxAge = (int) Math.ceil( Math.max( 1, 100.0f / speed ) ) + 2;
	}

	@Override
	public int getBrightnessForRender(float par1)
	{
		int j1 = 13;
		return j1 << 20 | j1 << 4;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if ( isDead )
			fi.setDead();
		else
		{
			float lifeSpan = (float) this.particleAge / (float) this.particleMaxAge;
			fi.setProgress( lifeSpan );
		}
	}

	@Override
	public void renderParticle(Tessellator tess, float l, float rX, float rY, float rZ, float rYZ, float rXY)
	{
		time += l;
		if ( time > 4.0 )
		{
			time -= 4.0;
			// if ( CommonHelper.proxy.shouldAddParticles( r ) )
			for (int x = 0; x < (int) Math.ceil( speed / 5 ); x++)
				CommonHelper.proxy.spawnEffect( EffectType.Crafting, worldObj, posX, posY, posZ, null );
		}
	}

}
