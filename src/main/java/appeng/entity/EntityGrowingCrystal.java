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


import appeng.api.implementations.items.IGrowableCrystal;
import appeng.api.implementations.tiles.ICrystalGrowthAccelerator;
import appeng.client.EffectType;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.util.Platform;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;


public final class EntityGrowingCrystal extends EntityItem {

    private int progress_1000 = 0;

    public EntityGrowingCrystal(final World w) {
        super(w);
    }

    public EntityGrowingCrystal(final World w, final double x, final double y, final double z, final ItemStack is) {
        super(w, x, y, z, is);
        this.setNoDespawn();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!AEConfig.instance().isFeatureEnabled(AEFeature.IN_WORLD_PURIFICATION)) {
            return;
        }

        final ItemStack is = this.getItem();
        final Item gc = is.getItem();

        if (gc instanceof IGrowableCrystal) // if it changes this just stops being an issue...
        {
            final int j = MathHelper.floor(this.posX);
            final int i = MathHelper.floor((this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D);
            final int k = MathHelper.floor(this.posZ);

            final IBlockState state = this.world.getBlockState(new BlockPos(j, i, k));
            final Material mat = state.getMaterial();
            final IGrowableCrystal cry = (IGrowableCrystal) is.getItem();

            final float multiplier = cry.getMultiplier(state.getBlock(), mat);
            final int speed = (int) Math.max(1, this.getSpeed(j, i, k) * multiplier);

            final boolean isClient = Platform.isClient();

            if (mat.isLiquid()) {
                if (isClient) {
                    this.progress_1000++;
                } else {
                    this.progress_1000 += speed;
                }
            } else {
                this.progress_1000 = 0;
            }

            if (isClient) {
                int len = 40;

                if (speed > 2) {
                    len = 20;
                }

                if (speed > 90) {
                    len = 15;
                }

                if (speed > 150) {
                    len = 10;
                }

                if (speed > 240) {
                    len = 7;
                }

                if (speed > 360) {
                    len = 3;
                }

                if (speed > 500) {
                    len = 1;
                }

                if (this.progress_1000 >= len) {
                    this.progress_1000 = 0;
                    AppEng.proxy.spawnEffect(EffectType.Vibrant, this.world, this.posX, this.posY + 0.2, this.posZ, null);
                }
            } else {
                if (this.progress_1000 > 1000) {
                    this.progress_1000 -= 1000;
                    this.setItem(cry.triggerGrowth(is));
                }
            }
        }
    }

    private int getSpeed(final int x, final int y, final int z) {
        final int per = 80;
        final float mul = 0.3f;

        int qty = 0;

        if (this.isAccelerated(x + 1, y, z)) {
            qty += per + qty * mul;
        }

        if (this.isAccelerated(x, y + 1, z)) {
            qty += per + qty * mul;
        }

        if (this.isAccelerated(x, y, z + 1)) {
            qty += per + qty * mul;
        }

        if (this.isAccelerated(x - 1, y, z)) {
            qty += per + qty * mul;
        }

        if (this.isAccelerated(x, y - 1, z)) {
            qty += per + qty * mul;
        }

        if (this.isAccelerated(x, y, z - 1)) {
            qty += per + qty * mul;
        }

        return qty;
    }

    private boolean isAccelerated(final int x, final int y, final int z) {
        final TileEntity te = this.world.getTileEntity(new BlockPos(x, y, z));

        return te instanceof ICrystalGrowthAccelerator && ((ICrystalGrowthAccelerator) te).isPowered();
    }
}
