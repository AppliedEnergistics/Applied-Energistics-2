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


import appeng.api.config.Actionable;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.sync.packets.PacketLightning;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.Platform;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;


public class ToolChargedStaff extends AEBasePoweredItem {

    public ToolChargedStaff() {
        super(AEConfig.instance().getChargedStaffBattery());
    }

    @Override
    public boolean hitEntity(final ItemStack item, final EntityLivingBase target, final EntityLivingBase hitter) {
        if (this.getAECurrentPower(item) > 300) {
            this.extractAEPower(item, 300, Actionable.MODULATE);
            if (Platform.isServer()) {
                for (int x = 0; x < 2; x++) {
                    final AxisAlignedBB entityBoundingBox = target.getEntityBoundingBox();
                    final float dx = (float) (Platform.getRandomFloat() * target.width + entityBoundingBox.minX);
                    final float dy = (float) (Platform.getRandomFloat() * target.height + entityBoundingBox.minY);
                    final float dz = (float) (Platform.getRandomFloat() * target.width + entityBoundingBox.minZ);
                    AppEng.proxy.sendToAllNearExcept(null, dx, dy, dz, 32.0, target.world, new PacketLightning(dx, dy, dz));
                }
            }
            target.attackEntityFrom(DamageSource.MAGIC, 6);
            return true;
        }

        return false;
    }
}
