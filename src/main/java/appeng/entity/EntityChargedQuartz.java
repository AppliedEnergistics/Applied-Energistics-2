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


import appeng.api.AEApi;
import appeng.api.definitions.IMaterials;
import appeng.client.EffectType;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.helpers.Reflected;
import appeng.util.Platform;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;


public final class EntityChargedQuartz extends AEBaseEntityItem {

    private int delay = 0;
    private int transformTime = 0;

    @Reflected
    public EntityChargedQuartz(final World w) {
        super(w);
    }

    public EntityChargedQuartz(final World w, final double x, final double y, final double z, final ItemStack is) {
        super(w, x, y, z, is);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.isDead || !AEConfig.instance().isFeatureEnabled(AEFeature.IN_WORLD_FLUIX)) {
            return;
        }

        if (Platform.isClient() && this.delay > 30 && AEConfig.instance().isEnableEffects()) {
            AppEng.proxy.spawnEffect(EffectType.Lightning, this.world, this.posX, this.posY, this.posZ, null);
            this.delay = 0;
        }

        this.delay++;

        final int j = MathHelper.floor(this.posX);
        final int i = MathHelper.floor((this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D);
        final int k = MathHelper.floor(this.posZ);

        IBlockState state = this.world.getBlockState(new BlockPos(j, i, k));
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
        final ItemStack item = this.getItem();
        final IMaterials materials = AEApi.instance().definitions().materials();

        if (materials.certusQuartzCrystalCharged().isSameAs(item)) {
            final AxisAlignedBB region = new AxisAlignedBB(this.posX - 1, this.posY - 1, this.posZ - 1, this.posX + 1, this.posY + 1, this.posZ + 1);
            final List<Entity> l = this.getCheckedEntitiesWithinAABBExcludingEntity(region);

            EntityItem redstone = null;
            EntityItem netherQuartz = null;

            for (final Entity e : l) {
                if (e instanceof EntityItem && !e.isDead) {
                    final ItemStack other = ((EntityItem) e).getItem();
                    if (!other.isEmpty()) {
                        if (ItemStack.areItemsEqual(other, new ItemStack(Items.REDSTONE))) {
                            redstone = (EntityItem) e;
                        }

                        if (ItemStack.areItemsEqual(other, new ItemStack(Items.QUARTZ))) {
                            netherQuartz = (EntityItem) e;
                        }
                    }
                }
            }

            if (redstone != null && netherQuartz != null) {
                this.getItem().grow(-1);
                redstone.getItem().grow(-1);
                netherQuartz.getItem().grow(-1);

                if (this.getItem().getCount() <= 0) {
                    this.setDead();
                }

                if (redstone.getItem().getCount() <= 0) {
                    redstone.setDead();
                }

                if (netherQuartz.getItem().getCount() <= 0) {
                    netherQuartz.setDead();
                }

                materials.fluixCrystal().maybeStack(2).ifPresent(is ->
                {
                    final EntityItem entity = new EntityItem(this.world, this.posX, this.posY, this.posZ, is);

                    this.world.spawnEntity(entity);
                });

                return true;
            }
        }

        return false;
    }
}
