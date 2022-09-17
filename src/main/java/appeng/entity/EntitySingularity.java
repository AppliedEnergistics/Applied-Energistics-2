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
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.helpers.Reflected;
import appeng.util.Platform;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Date;
import java.util.List;


public final class EntitySingularity extends AEBaseEntityItem {

    private static int randTickSeed = 0;

    @Reflected
    public EntitySingularity(final World w) {
        super(w);
    }

    public EntitySingularity(final World w, final double x, final double y, final double z, final ItemStack is) {
        super(w, x, y, z, is);
    }

    @Override
    public boolean attackEntityFrom(final DamageSource src, final float dmg) {
        if (src.isExplosion()) {
            this.doExplosion();
            return false;
        }

        return super.attackEntityFrom(src, dmg);
    }

    private void doExplosion() {
        if (Platform.isClient()) {
            return;
        }

        if (!AEConfig.instance().isFeatureEnabled(AEFeature.IN_WORLD_SINGULARITY)) {
            return;
        }

        final ItemStack item = this.getItem();

        final IMaterials materials = AEApi.instance().definitions().materials();

        if (materials.singularity().isSameAs(item)) {
            final AxisAlignedBB region = new AxisAlignedBB(this.posX - 4, this.posY - 4, this.posZ - 4, this.posX + 4, this.posY + 4, this.posZ + 4);
            final List<Entity> l = this.getCheckedEntitiesWithinAABBExcludingEntity(region);

            for (final Entity e : l) {
                if (e instanceof EntityItem) {
                    final ItemStack other = ((EntityItem) e).getItem();
                    if (!other.isEmpty()) {
                        boolean matches = false;
                        for (final ItemStack is : OreDictionary.getOres("dustEnder")) {
                            if (OreDictionary.itemMatches(other, is, false)) {
                                matches = true;
                                break;
                            }
                        }

                        // check... other name.
                        if (!matches) {
                            for (final ItemStack is : OreDictionary.getOres("dustEnderPearl")) {
                                if (OreDictionary.itemMatches(other, is, false)) {
                                    matches = true;
                                    break;
                                }
                            }
                        }

                        if (matches) {
                            while (item.getCount() > 0 && other.getCount() > 0) {
                                other.grow(-1);
                                if (other.getCount() == 0) {
                                    e.setDead();
                                }

                                materials.qESingularity().maybeStack(2).ifPresent(singularityStack ->
                                {
                                    final NBTTagCompound cmp = Platform.openNbtData(singularityStack);
                                    cmp.setLong("freq", (new Date()).getTime() * 100 + (randTickSeed) % 100);
                                    randTickSeed++;
                                    item.grow(-1);

                                    final EntitySingularity entity = new EntitySingularity(this.world, this.posX, this.posY, this.posZ, singularityStack);
                                    this.world.spawnEntity(entity);
                                });
                            }

                            if (item.getCount() <= 0) {
                                this.setDead();
                            }
                        }
                    }
                }
            }
        }
    }
}
