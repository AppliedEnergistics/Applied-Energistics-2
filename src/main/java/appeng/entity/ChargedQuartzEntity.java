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
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

import appeng.client.EffectType;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.definitions.AEEntities;
import appeng.core.definitions.AEItems;

public final class ChargedQuartzEntity extends AEBaseItemEntity {

    private static final Random RANDOM = new Random();

    private int delay = 0;
    private int transformTime = 0;

    public ChargedQuartzEntity(EntityType<? extends ChargedQuartzEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ChargedQuartzEntity(final Level level, final double x, final double y, final double z, final ItemStack is) {
        super(AEEntities.CHARGED_QUARTZ, level, x, y, z, is);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isRemoved() || !AEConfig.instance().isInWorldFluixEnabled()) {
            return;
        }

        if (level.isClientSide() && this.delay > 30 && AEConfig.instance().isEnableEffects()) {
            AppEng.instance().spawnEffect(EffectType.Lightning, this.level, this.getX(), this.getY(),
                    this.getZ(),
                    null);
            this.delay = 0;
        }

        this.delay++;

        final int j = Mth.floor(this.getX());
        final int i = Mth.floor((this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D);
        final int k = Mth.floor(this.getZ());

        BlockState state = this.level.getBlockState(new BlockPos(j, i, k));
        final Material mat = state.getMaterial();

        if (!level.isClientSide() && mat.isLiquid()) {
            this.transformTime++;
            if (this.transformTime > 60 && !this.transform()) {
                this.transformTime = 0;
            }
        } else {
            this.transformTime = 0;
        }
    }

    private boolean transform() {
        final ItemStack item = this.getItem();

        if (AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.isSameAs(item)) {
            final AABB region = new AABB(this.getX() - 1, this.getY() - 1, this.getZ() - 1,
                    this.getX() + 1, this.getY() + 1, this.getZ() + 1);
            final List<Entity> l = this.getCheckedEntitiesWithinAABBExcludingEntity(region);

            ItemEntity redstone = null;
            ItemEntity netherQuartz = null;

            for (final Entity e : l) {
                if (e instanceof ItemEntity && !e.isRemoved()) {
                    final ItemStack other = ((ItemEntity) e).getItem();
                    if (!other.isEmpty()) {
                        if (ItemStack.isSame(other, new ItemStack(Items.REDSTONE))) {
                            redstone = (ItemEntity) e;
                        }

                        if (ItemStack.isSame(other, new ItemStack(Items.QUARTZ))) {
                            netherQuartz = (ItemEntity) e;
                        }
                    }
                }
            }

            if (redstone != null && netherQuartz != null) {
                this.getItem().grow(-1);
                redstone.getItem().grow(-1);
                netherQuartz.getItem().grow(-1);

                if (this.getItem().getCount() <= 0) {
                    this.discard();
                }

                if (redstone.getItem().getCount() <= 0) {
                    redstone.discard();
                }

                if (netherQuartz.getItem().getCount() <= 0) {
                    netherQuartz.discard();
                }

                final double x = Math.floor(this.getX()) + .25d + RANDOM.nextDouble() * .5;
                final double y = Math.floor(this.getY()) + .25d + RANDOM.nextDouble() * .5;
                final double z = Math.floor(this.getZ()) + .25d + RANDOM.nextDouble() * .5;
                final double xSpeed = RANDOM.nextDouble() * .25 - 0.125;
                final double ySpeed = RANDOM.nextDouble() * .25 - 0.125;
                final double zSpeed = RANDOM.nextDouble() * .25 - 0.125;

                final ItemEntity entity = new ItemEntity(this.level, x, y, z, AEItems.FLUIX_DUST.stack(2));
                entity.setDeltaMovement(xSpeed, ySpeed, zSpeed);
                this.level.addFreshEntity(entity);

                return true;
            }
        }

        return false;
    }
}
