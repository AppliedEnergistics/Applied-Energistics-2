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

import java.util.Date;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.core.AEConfig;
import appeng.core.definitions.AEEntities;
import appeng.core.definitions.AEItems;
import net.minecraft.world.phys.AABB;

public final class SingularityEntity extends AEBaseItemEntity {

    private static final ResourceLocation TAG_ENDER_PEARL = new net.minecraft.resources.ResourceLocation("forge:ender_pearls");

    private static int randTickSeed = 0;

    public SingularityEntity(net.minecraft.world.entity.EntityType<? extends SingularityEntity> entityType, final Level w) {
        super(entityType, w);
    }

    public SingularityEntity(final Level w, final double x, final double y, final double z, final net.minecraft.world.item.ItemStack is) {
        super(AEEntities.SINGULARITY, w, x, y, z, is);
    }

    @Override
    public boolean hurt(final DamageSource src, final float dmg) {
        if (src.isExplosion()) {
            this.doExplosion();
            return false;
        }

        return super.hurt(src, dmg);
    }

    private void doExplosion() {
        if (level.isClientSide()) {
            return;
        }

        if (!AEConfig.instance().isInWorldSingularityEnabled()) {
            return;
        }

        final net.minecraft.world.item.ItemStack item = this.getItem();

        if (AEItems.SINGULARITY.isSameAs(item)) {
            final AABB region = new AABB(this.getX() - 4, this.getY() - 4, this.getZ() - 4,
                    this.getX() + 4, this.getY() + 4, this.getZ() + 4);
            final List<net.minecraft.world.entity.Entity> l = this.getCheckedEntitiesWithinAABBExcludingEntity(region);

            for (final Entity e : l) {
                if (e instanceof net.minecraft.world.entity.item.ItemEntity) {
                    final ItemStack other = ((ItemEntity) e).getItem();
                    if (!other.isEmpty()) {
                        boolean matches = false;

                        if (AEItems.ENDER_DUST.isSameAs(other)) {
                            matches = true;
                        }

                        // check... other name.
                        if (!matches && other.getItem().getTags().contains(TAG_ENDER_PEARL)) {
                            matches = true;
                        }

                        if (matches) {
                            while (item.getCount() > 0 && other.getCount() > 0) {
                                other.grow(-1);

                                if (other.getCount() == 0) {
                                    e.remove();
                                }

                                net.minecraft.world.item.ItemStack singularityStack = AEItems.QUANTUM_ENTANGLED_SINGULARITY.stack(2);
                                final CompoundTag cmp = singularityStack.getOrCreateTag();
                                cmp.putLong("freq", new Date().getTime() * 100 + randTickSeed % 100);
                                randTickSeed++;
                                item.grow(-1);

                                final SingularityEntity entity = new SingularityEntity(this.level, this.getX(),
                                        this.getY(), this.getZ(), singularityStack);
                                this.level.addFreshEntity(entity);
                            }

                            if (item.getCount() <= 0) {
                                this.remove();
                            }
                        }
                    }
                }
            }
        }
    }
}
