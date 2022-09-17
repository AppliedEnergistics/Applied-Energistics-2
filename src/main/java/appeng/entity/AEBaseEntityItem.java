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


import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;


public abstract class AEBaseEntityItem extends EntityItem {
    public AEBaseEntityItem(final World world) {
        super(world);
    }

    public AEBaseEntityItem(final World world, final double x, final double y, final double z, final ItemStack stack) {
        super(world, x, y, z, stack);
    }

    protected List<Entity> getCheckedEntitiesWithinAABBExcludingEntity(final AxisAlignedBB region) {
        return this.world.getEntitiesWithinAABBExcludingEntity(this, region);
    }
}
