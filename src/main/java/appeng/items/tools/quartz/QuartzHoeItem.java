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

import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import appeng.api.features.AEFeature;
import appeng.util.Platform;

public class QuartzHoeItem extends HoeItem {
    private final AEFeature type;

    public QuartzHoeItem(Item.Properties props, final AEFeature type) {
        super(ItemTier.field_8923, -2, -1.0F, props);
        this.type = type;
    }

    @Override
    public boolean getIsRepairable(final ItemStack a, final ItemStack b) {
        return Platform.canRepair(this.type, a, b);
    }

}