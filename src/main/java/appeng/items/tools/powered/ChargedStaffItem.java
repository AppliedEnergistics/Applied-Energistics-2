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

package appeng.items.tools.powered;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import appeng.api.config.Actionable;
import appeng.core.AEConfig;
import appeng.core.particles.ParticleTypes;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;

public class ChargedStaffItem extends AEBasePoweredItem {

    public ChargedStaffItem(Properties props) {
        super(AEConfig.instance().getChargedStaffBattery(), props);
    }

    @Override
    public void hurtEnemy(ItemStack item, LivingEntity target, LivingEntity hitter) {
        if (this.getAECurrentPower(item) > 300) {
            this.extractAEPower(item, 300, Actionable.MODULATE);
            var level = target.level();
            if (!level.isClientSide()) {
                for (int x = 0; x < 2; x++) {
                    final AABB entityBoundingBox = target.getBoundingBox();
                    var dx = (float) (level.getRandom().nextFloat() * target.getBbWidth()
                            + entityBoundingBox.minX);
                    var dy = (float) (level.getRandom().nextFloat() * target.getBbHeight()
                            + entityBoundingBox.minY);
                    var dz = (float) (level.getRandom().nextFloat() * target.getBbWidth()
                            + entityBoundingBox.minZ);
                    level.addParticle(ParticleTypes.LIGHTNING, dx, dy, dz,
                            0.0f, 0.0f,
                            0.0f);
                }
            }
            target.hurt(level.damageSources().magic(), 6);
        }
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 32d;
    }
}
