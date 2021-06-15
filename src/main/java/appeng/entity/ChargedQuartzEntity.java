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

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import appeng.api.features.AEFeature;
import appeng.client.EffectType;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.api.definitions.ApiEntities;
import appeng.core.api.definitions.ApiMaterials;

public final class ChargedQuartzEntity extends AEBaseItemEntity {

    private static final Random RANDOM = new Random();

    private int delay = 0;
    private int transformTime = 0;

    public ChargedQuartzEntity(EntityType<? extends ChargedQuartzEntity> entityType, World world) {
        super(entityType, world);
    }

    public ChargedQuartzEntity(final World w, final double x, final double y, final double z, final ItemStack is) {
        super(ApiEntities.CHARGED_QUARTZ, w, x, y, z, is);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.removed || !AEConfig.instance().isFeatureEnabled(AEFeature.IN_WORLD_FLUIX)) {
            return;
        }

        if (world.isRemote() && this.delay > 30 && AEConfig.instance().isEnableEffects()) {
            AppEng.proxy.spawnEffect(EffectType.Lightning, this.world, this.getPosX(), this.getPosY(), this.getPosZ(),
                    null);
            this.delay = 0;
        }

        this.delay++;

        final int j = MathHelper.floor(this.getPosX());
        final int i = MathHelper.floor((this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D);
        final int k = MathHelper.floor(this.getPosZ());

        BlockState state = this.world.getBlockState(new BlockPos(j, i, k));
        final Material mat = state.getMaterial();

        if (!world.isRemote() && mat.isLiquid()) {
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

        if (ApiMaterials.certusQuartzCrystalCharged().isSameAs(item)) {
            final AxisAlignedBB region = new AxisAlignedBB(this.getPosX() - 1, this.getPosY() - 1, this.getPosZ() - 1,
                    this.getPosX() + 1, this.getPosY() + 1, this.getPosZ() + 1);
            final List<Entity> l = this.getCheckedEntitiesWithinAABBExcludingEntity(region);

            ItemEntity redstone = null;
            ItemEntity netherQuartz = null;

            for (final Entity e : l) {
                if (e instanceof ItemEntity && !e.removed) {
                    final ItemStack other = ((ItemEntity) e).getItem();
                    if (!other.isEmpty()) {
                        if (ItemStack.areItemsEqual(other, new ItemStack(Items.REDSTONE))) {
                            redstone = (ItemEntity) e;
                        }

                        if (ItemStack.areItemsEqual(other, new ItemStack(Items.QUARTZ))) {
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
                    this.remove();
                }

                if (redstone.getItem().getCount() <= 0) {
                    redstone.remove();
                }

                if (netherQuartz.getItem().getCount() <= 0) {
                    netherQuartz.remove();
                }

                ApiMaterials.fluixCrystal().maybeStack(2).ifPresent(is -> {
                    final double x = Math.floor(this.getPosX()) + .25d + RANDOM.nextDouble() * .5;
                    final double y = Math.floor(this.getPosY()) + .25d + RANDOM.nextDouble() * .5;
                    final double z = Math.floor(this.getPosZ()) + .25d + RANDOM.nextDouble() * .5;
                    final double xSpeed = RANDOM.nextDouble() * .25 - 0.125;
                    final double ySpeed = RANDOM.nextDouble() * .25 - 0.125;
                    final double zSpeed = RANDOM.nextDouble() * .25 - 0.125;

                    final ItemEntity entity = new ItemEntity(this.world, x, y, z, is);
                    entity.setMotion(xSpeed, ySpeed, zSpeed);
                    this.world.addEntity(entity);
                });

                return true;
            }
        }

        return false;
    }
}
