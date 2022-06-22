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
import java.util.Set;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import appeng.client.EffectType;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.definitions.AEEntities;
import appeng.core.definitions.AEItems;
import appeng.recipes.handlers.TransformRecipe;

public final class ChargedQuartzEntity extends AEBaseItemEntity {

    private static final RandomSource RANDOM = RandomSource.create();

    private int delay = 0;
    private int transformTime = 0;

    public ChargedQuartzEntity(EntityType<? extends ChargedQuartzEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ChargedQuartzEntity(Level level, double x, double y, double z, ItemStack is) {
        super(AEEntities.CHARGED_QUARTZ, level, x, y, z, is);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isRemoved() || !AEConfig.instance().isInWorldChargedQuartzTransformEnabled()) {
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

        if (!AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.isSameAs(item)) {
            return false;
        }

        final AABB region = new AABB(this.getX() - 1, this.getY() - 1, this.getZ() - 1,
                this.getX() + 1, this.getY() + 1, this.getZ() + 1);
        final List<Entity> l = this.getCheckedEntitiesWithinAABB(region);

        for (var recipe : level.getRecipeManager().byType(TransformRecipe.TYPE).values()) {
            List<Ingredient> missingIngredients = Lists.newArrayList(recipe.ingredients);
            Set<ItemEntity> selectedEntities = new ReferenceOpenHashSet<>();

            entityLoop: for (Entity e : l) {
                if (e instanceof ItemEntity itemEntity && !e.isRemoved()) {
                    final ItemStack other = itemEntity.getItem();
                    if (!other.isEmpty()) {
                        if (missingIngredients.stream().noneMatch(ingredient -> ingredient.test(other))) {
                            continue; // Skip items that are not required (anymore)
                        }

                        for (var selectedEntity : selectedEntities) {
                            if (selectedEntity.getItem().getItem() == other.getItem()) {
                                continue entityLoop; // We already have this item
                            }
                        }

                        for (var it = missingIngredients.iterator(); it.hasNext();) {
                            Ingredient ing = it.next();
                            if (ing.test(other)) {
                                selectedEntities.add(itemEntity);
                                it.remove();
                                break;
                            }
                        }
                    }
                }
            }

            if (selectedEntities.size() == recipe.ingredients.size()) {
                this.getItem().grow(-1);

                if (this.getItem().getCount() <= 0) {
                    this.discard();
                }

                for (var e : selectedEntities) {
                    e.getItem().grow(-1);

                    if (e.getItem().getCount() <= 0) {
                        e.discard();
                    }
                }

                final double x = Math.floor(this.getX()) + .25d + RANDOM.nextDouble() * .5;
                final double y = Math.floor(this.getY()) + .25d + RANDOM.nextDouble() * .5;
                final double z = Math.floor(this.getZ()) + .25d + RANDOM.nextDouble() * .5;
                final double xSpeed = RANDOM.nextDouble() * .25 - 0.125;
                final double ySpeed = RANDOM.nextDouble() * .25 - 0.125;
                final double zSpeed = RANDOM.nextDouble() * .25 - 0.125;

                final ItemEntity entity = new ItemEntity(this.level, x, y, z,
                        new ItemStack(recipe.output, recipe.count));
                entity.setDeltaMovement(xSpeed, ySpeed, zSpeed);
                this.level.addFreshEntity(entity);

                return true;
            }
        }

        return false;
    }
}
