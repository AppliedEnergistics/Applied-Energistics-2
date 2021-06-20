/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.items.materials;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import appeng.items.AEBaseItem;

/**
 * Used for items that use a different entity for when they're dropped.
 */
public class CustomEntityItem extends AEBaseItem {
    private final EntityFactory factory;

    public CustomEntityItem(Properties properties, EntityFactory factory) {
        super(properties);
        this.factory = factory;
    }

    @Override
    public boolean hasCustomEntity(final ItemStack is) {
        return true;
    }

    @Override
    public Entity createEntity(final World w, final Entity location, final ItemStack itemstack) {
        ItemEntity eqi = factory.create(w, location.getPosX(), location.getPosY(), location.getPosZ(),
                itemstack);

        eqi.setMotion(location.getMotion());

        if (location instanceof ItemEntity) {
            eqi.setDefaultPickupDelay();
        }

        return eqi;
    }

    @FunctionalInterface
    public interface EntityFactory {
        ItemEntity create(World w, double x, double y, double z, ItemStack is);
    }

}
