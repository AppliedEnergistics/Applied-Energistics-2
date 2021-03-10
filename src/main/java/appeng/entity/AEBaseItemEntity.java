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
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public abstract class AEBaseItemEntity extends ItemEntity {

    protected AEBaseItemEntity(EntityType<? extends AEBaseItemEntity> entityType, final World world) {
        super(entityType, world);
    }

    protected AEBaseItemEntity(EntityType<? extends AEBaseItemEntity> entityType, final World world, final double x,
            final double y, final double z, final ItemStack stack) {
        this(entityType, world);
        this.setPos(x, y, z);
        this.yRot = this.random.nextFloat() * 360.0F;
        this.setDeltaMovement(this.random.nextDouble() * 0.2D - 0.1D, 0.2D, this.random.nextDouble() * 0.2D - 0.1D);
        this.setItem(stack);
        this.lifespan = stack.getEntityLifespan(world);
    }

    protected List<Entity> getCheckedEntitiesWithinAABBExcludingEntity(final AxisAlignedBB region) {
        return this.level.getEntities(this, region);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

}
