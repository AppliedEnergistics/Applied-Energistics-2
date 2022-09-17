/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.core.features;


import appeng.items.materials.MaterialType;
import com.google.common.base.Preconditions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


public class MaterialStackSrc implements IStackSrc {
    private final MaterialType src;
    private final boolean enabled;

    public MaterialStackSrc(final MaterialType src, boolean enabled) {
        Preconditions.checkNotNull(src);

        this.src = src;
        this.enabled = enabled;
    }

    @Override
    public ItemStack stack(final int stackSize) {
        return this.src.stack(stackSize);
    }

    @Override
    public Item getItem() {
        return this.src.getItemInstance();
    }

    @Override
    public int getDamage() {
        return this.src.getDamageValue();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
