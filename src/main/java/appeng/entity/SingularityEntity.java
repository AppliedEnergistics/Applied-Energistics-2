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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import appeng.api.definitions.IMaterials;
import appeng.api.features.AEFeature;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.util.Platform;

public final class SingularityEntity extends AEBaseItemEntity {

    private static final ResourceLocation TAG_ENDER_PEARL = new ResourceLocation("forge:ender_pearls");

    public static EntityType<SingularityEntity> TYPE;

    private static int randTickSeed = 0;

    public SingularityEntity(EntityType<? extends SingularityEntity> entityType, final World w) {
        super(entityType, w);
    }

    public SingularityEntity(final World w, final double x, final double y, final double z, final ItemStack is) {
        super(TYPE, w, x, y, z, is);
    }

    @Override
    public boolean damage(final DamageSource src, final float dmg) {
        if (src.isExplosive()) {
            this.doExplosion();
            return false;
        }

        return super.damage(src, dmg);
    }

    private void doExplosion() {
        if (Platform.isClient()) {
            return;
        }

        if (!AEConfig.instance().isFeatureEnabled(AEFeature.IN_WORLD_SINGULARITY)) {
            return;
        }

        final ItemStack item = this.getStack();

        final IMaterials materials = Api.instance().definitions().materials();

        if (materials.singularity().isSameAs(item)) {
            final Box region = new Box(this.getX() - 4, this.getY() - 4, this.getZ() - 4,
                    this.getX() + 4, this.getY() + 4, this.getZ() + 4);
            final List<Entity> l = this.getCheckedEntitiesWithinAABBExcludingEntity(region);

            for (final Entity e : l) {
                if (e instanceof ItemEntity) {
                    final ItemStack other = ((ItemEntity) e).getStack();
                    if (!other.isEmpty()) {
                        boolean matches = false;

                        if (materials.enderDust().isSameAs(other)) {
                            matches = true;
                        }

                        // check... other name.
                        if (!matches) {
                            if (other.getItem().getTags().contains(TAG_ENDER_PEARL)) {
                                matches = true;
                            }
                        }

                        if (matches) {
                            while (item.getCount() > 0 && other.getCount() > 0) {
                                other.increment(-1);
                                ;
                                if (other.getCount() == 0) {
                                    e.remove();
                                }

                                materials.qESingularity().maybeStack(2).ifPresent(singularityStack -> {
                                    final CompoundTag cmp = singularityStack.getOrCreateTag();
                                    cmp.putLong("freq", (new Date()).getTime() * 100 + (randTickSeed) % 100);
                                    randTickSeed++;
                                    item.increment(-1);

                                    final SingularityEntity entity = new SingularityEntity(this.world, this.getX(),
                                            this.getY(), this.getZ(), singularityStack);
                                    this.world.spawnEntity(entity);
                                });
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
