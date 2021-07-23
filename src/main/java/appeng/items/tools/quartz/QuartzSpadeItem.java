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

package appeng.items.tools.quartz;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tiers;

import appeng.util.Platform;
import net.minecraft.world.item.Item.Properties;

public class QuartzSpadeItem extends ShovelItem {
    private final QuartzToolType type;

    public QuartzSpadeItem(Item.Properties props, final QuartzToolType type) {
        super(Tiers.IRON, 1.5F, -3.0F, props);
        this.type = type;
    }

    @Override
    public boolean isValidRepairItem(final ItemStack a, final ItemStack b) {
        return Platform.canRepair(this.type, a, b);
    }
}
