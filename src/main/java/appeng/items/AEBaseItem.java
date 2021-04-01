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

package appeng.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public abstract class AEBaseItem extends Item {

    public AEBaseItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public String toString() {
        ResourceLocation id = Registry.ITEM.getKey(this);
        String regName = id != Registry.ITEM.getDefaultKey() ? id.getPath() : "unregistered";
        return this.getClass().getSimpleName() + "[" + regName + "]";
    }

    @Override
    public boolean getIsRepairable(ItemStack stack, ItemStack ingredient) {
        return false;
    }

}
