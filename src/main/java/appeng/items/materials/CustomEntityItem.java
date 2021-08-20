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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.hooks.AECustomEntityItem;
import appeng.items.AEBaseItem;

/**
 * Used for items that use a different entity for when they're dropped.
 */
public class CustomEntityItem extends AEBaseItem implements AECustomEntityItem {
    private final EntityFactory factory;

    public CustomEntityItem(Item.Properties properties, EntityFactory factory) {
        super(properties);
        this.factory = factory;
    }

    @Override
    public Entity replaceItemEntity(ServerLevel level, ItemEntity location, ItemStack itemStack) {
        ItemEntity eqi = factory.create(level, location.getX(), location.getY(), location.getZ(),
                itemStack);

        eqi.setDeltaMovement(location.getDeltaMovement());
        eqi.setDefaultPickUpDelay();

        return eqi;
    }

    @FunctionalInterface
    public interface EntityFactory {
        ItemEntity create(Level level, double x, double y, double z, ItemStack is);
    }

}
