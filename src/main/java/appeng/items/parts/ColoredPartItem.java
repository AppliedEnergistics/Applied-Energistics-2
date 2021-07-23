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

package appeng.items.parts;

import java.util.function.Function;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.parts.IPart;
import appeng.api.util.AEColor;
import net.minecraft.world.item.Item.Properties;

public class ColoredPartItem<T extends IPart> extends PartItem<T> {

    private final AEColor color;

    public ColoredPartItem(Item.Properties properties, Function<ItemStack, T> factory, AEColor color) {
        super(properties, factory);
        this.color = color;
    }

    public AEColor getColor() {
        return color;
    }

}
