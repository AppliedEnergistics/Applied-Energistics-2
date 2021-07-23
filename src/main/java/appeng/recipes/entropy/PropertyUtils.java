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

import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;

final class PropertyUtils {
    private PropertyUtils() {
    }

    static Property<?> getRequiredProperty(StateContainer<?, ?> stateContainer, String name) {
        Objects.requireNonNull(stateContainer, "stateContainer must not be null");

        Property<?> property = stateContainer.getProperty(name);
        if (property == null) {
            throw new IllegalArgumentException("Unknown property: " + name + " on " + stateContainer.getOwner());
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
