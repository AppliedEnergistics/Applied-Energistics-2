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

import javax.annotation.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class AEBaseItem extends Item {

    public AEBaseItem(Item.Properties properties) {
        super(properties);
    }

    @Nullable
    public ResourceLocation getRegistryName() {
        var id = BuiltInRegistries.ITEM.getKey(this);
        return id != BuiltInRegistries.ITEM.getDefaultKey() ? id : null;
    }

    public void addToMainCreativeTab(CreativeModeTab.Output output) {
        output.accept(this);
    }

    @Override
    public String toString() {
        String regName = this.getRegistryName() != null ? this.getRegistryName().getPath() : "unregistered";
        return this.getClass().getSimpleName() + "[" + regName + "]";
    }

    @Override
    public boolean isBookEnchantable(final ItemStack itemstack1, final ItemStack itemstack2) {
        return false;
    }

}
