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

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public abstract class AEBaseItemEntity extends ItemEntity {

    protected AEBaseItemEntity(EntityType<? extends AEBaseItemEntity> entityType, final World world) {
        super(entityType, world);
    }

    protected AEBaseItemEntity(EntityType<? extends AEBaseItemEntity> entityType, final World world, final double x,
            final double y, final double z, final ItemStack stack) {
        this(entityType, world);
        this.updatePosition(x, y, z);
        this.yaw = this.random.nextFloat() * 360.0F;
        this.setVelocity(this.random.nextDouble() * 0.2D - 0.1D, 0.2D, this.random.nextDouble() * 0.2D - 0.1D);
        this.setStack(stack);
        // FIXME FABRIC Needs replacement hook this.lifespan =
        // stack.getEntityLifespan(world);
    }

    protected List<Entity> getCheckedEntitiesWithinAABBExcludingEntity(final Box region) {
        return this.world.getEntities(this, region);
    }

}
