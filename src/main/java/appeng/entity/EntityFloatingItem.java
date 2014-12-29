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

package appeng.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

final public class EntityFloatingItem extends EntityItem
{

	public static int ageStatic = 0;

	int superDeath = 0;
	private final Entity parent;
	float progress = 0;

	public EntityFloatingItem(Entity parent, World p_i1710_1_, double p_i1710_2_, double p_i1710_4_, double p_i1710_6_, ItemStack p_i1710_8_) {
		super( p_i1710_1_, p_i1710_2_, p_i1710_4_, p_i1710_6_, p_i1710_8_ );
		this.motionX = this.motionY = this.motionZ = 0.0d;
		this.hoverStart = 0.5f;
		this.rotationYaw = 0;
		this.parent = parent;
	}

	// public boolean isEntityAlive()

	@Override
	public void onUpdate()
	{
		if ( !this.isDead && this.parent.isDead )
			this.setDead();

		if ( this.superDeath++ > 100 )
			this.setDead();

		this.age = ageStatic;
	}

	public void setProgress(float progress)
	{
		this.progress = progress;
		if ( this.progress > 0.99 )
			this.setDead();
	}

}
