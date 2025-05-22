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

package appeng.recipes.entropy;

import java.util.Objects;

import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

final class PropertyUtils {
    private PropertyUtils() {
    }

    static Property<?> getRequiredProperty(StateDefinition<?, ?> stateDefinition, String name) {
        Objects.requireNonNull(stateDefinition, "stateDefinition must not be null");

        Property<?> property = stateDefinition.getProperty(name);
        if (property == null) {
            throw new IllegalArgumentException("Unknown property: " + name + " on " + stateDefinition.getOwner());
        }
        return property;
    }

    static <T extends Comparable<T>> T getRequiredPropertyValue(Property<T> property, String name) {
        Objects.requireNonNull(property, "property must be not null");

        return property.getValue(name)
                .orElseThrow(() -> new IllegalArgumentException("Invalid value '" + name + "' for property "
                        + property.getName()));
    }
}
