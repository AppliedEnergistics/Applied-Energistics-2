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

import appeng.api.parts.IPart;
import appeng.api.util.AEColor;

public class ColoredPartItem<T extends IPart> extends PartItem<T> {

    private final AEColor color;

    public ColoredPartItem(Properties properties, Class<T> partClass, Function<ColoredPartItem<T>, T> factory,
            AEColor color) {
        super(properties, partClass, item -> factory.apply((ColoredPartItem<T>) item));
        this.color = color;
    }

    public AEColor getColor() {
        return color;
    }

}
