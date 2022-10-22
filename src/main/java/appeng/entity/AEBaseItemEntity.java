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

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import appeng.core.sync.packets.ICustomEntity;
import appeng.core.sync.packets.SpawnEntityPacket;

public abstract class AEBaseItemEntity extends ItemEntity implements ICustomEntity {

    protected AEBaseItemEntity(EntityType<? extends AEBaseItemEntity> entityType, Level level) {
        super(entityType, level);
    }

    protected AEBaseItemEntity(EntityType<? extends AEBaseItemEntity> entityType, Level level, double x,
            double y, double z, ItemStack stack) {
        this(entityType, level);
        this.setPos(x, y, z);
        this.setYRot(this.random.nextFloat() * 360.0F);
        this.setDeltaMovement(this.random.nextDouble() * 0.2D - 0.1D, 0.2D, this.random.nextDouble() * 0.2D - 0.1D);
        this.setItem(stack);
    }

    protected List<Entity> getCheckedEntitiesWithinAABB(AABB region) {
        return this.level.getEntities(null, region);
    }

    protected List<Entity> getCheckedEntitiesWithinAABBExcludingEntity(AABB region) {
        return this.level.getEntities(this, region);
    }

    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return SpawnEntityPacket.create(this);
    }

}
