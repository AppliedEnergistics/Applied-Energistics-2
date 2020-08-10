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
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import appeng.api.definitions.IMaterials;
import appeng.api.features.AEFeature;
import appeng.client.render.effects.ParticleTypes;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.util.Platform;

public final class ChargedQuartzEntity extends AEBaseItemEntity {

    private static final Random RANDOM = new Random();

    public static EntityType<ChargedQuartzEntity> TYPE;

    private int delay = 0;
    private int transformTime = 0;

    public ChargedQuartzEntity(EntityType<? extends ChargedQuartzEntity> entityType, World world) {
        super(entityType, world);
    }

    public ChargedQuartzEntity(final World w, final double x, final double y, final double z, final ItemStack is) {
        super(TYPE, w, x, y, z, is);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.removed || !AEConfig.instance().isFeatureEnabled(AEFeature.IN_WORLD_FLUIX)) {
            return;
        }

        if (world.isClient && this.delay > 30 && AEConfig.instance().isEnableEffects()) {
            MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.LIGHTNING, this.getX(),
                    this.getY() + 0.3f, this.getZ(), 0.0f, 0.0f, 0.0f);
            this.delay = 0;
        }

        this.delay++;

        final int j = MathHelper.floor(this.getX());
        final int i = MathHelper.floor((this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D);
        final int k = MathHelper.floor(this.getZ());

        BlockState state = this.world.getBlockState(new BlockPos(j, i, k));
        final Material mat = state.getMaterial();

        if (Platform.isServer() && mat.isLiquid()) {
            this.transformTime++;
            if (this.transformTime > 60) {
                if (!this.transform()) {
                    this.transformTime = 0;
                }
            }
        } else {
            this.transformTime = 0;
        }
    }

    private boolean transform() {
        final ItemStack item = this.getStack();
        final IMaterials materials = Api.instance().definitions().materials();

        if (materials.certusQuartzCrystalCharged().isSameAs(item)) {
            final Box region = new Box(this.getX() - 1, this.getY() - 1, this.getZ() - 1, this.getX() + 1,
                    this.getY() + 1, this.getZ() + 1);
            final List<Entity> l = this.getCheckedEntitiesWithinAABBExcludingEntity(region);

            ItemEntity redstone = null;
            ItemEntity netherQuartz = null;

            for (final Entity e : l) {
                if (e instanceof ItemEntity && !e.removed) {
                    final ItemStack other = ((ItemEntity) e).getStack();
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
                this.getStack().increment(-1);
                redstone.getStack().increment(-1);
                netherQuartz.getStack().increment(-1);

                if (this.getStack().getCount() <= 0) {
                    this.remove();
                }

                if (redstone.getStack().getCount() <= 0) {
                    redstone.remove();
                }

                if (netherQuartz.getStack().getCount() <= 0) {
                    netherQuartz.remove();
                }

                materials.fluixCrystal().maybeStack(2).ifPresent(is -> {
                    final double x = Math.floor(this.getX()) + .25d + RANDOM.nextDouble() * .5;
                    final double y = Math.floor(this.getY()) + .25d + RANDOM.nextDouble() * .5;
                    final double z = Math.floor(this.getZ()) + .25d + RANDOM.nextDouble() * .5;
                    final double xSpeed = RANDOM.nextDouble() * .25 - 0.125;
                    final double ySpeed = RANDOM.nextDouble() * .25 - 0.125;
                    final double zSpeed = RANDOM.nextDouble() * .25 - 0.125;

                    final ItemEntity entity = new ItemEntity(this.world, x, y, z, is);
                    entity.setVelocity(xSpeed, ySpeed, zSpeed);
                    this.world.spawnEntity(entity);
                });

                return true;
            }
        }

        return false;
    }
}
