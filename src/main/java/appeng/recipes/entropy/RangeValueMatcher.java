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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.StateHolder;

/**
 * Matches a range between a min and max value (inclusive).
 */
class RangeValueMatcher<T extends Comparable<T>> implements StateMatcher {
    private final Property<T> property;
    private final T minValue;
    private final T maxValue;

    private RangeValueMatcher(Property<T> property, String minValueName, String maxValueName) {
        this.property = Objects.requireNonNull(property, "property must not be null");
        this.minValue = PropertyUtils.getRequiredPropertyValue(property, minValueName);
        this.maxValue = PropertyUtils.getRequiredPropertyValue(property, maxValueName);
    }

    @Override
    public boolean matches(StateHolder<?, ?> state) {
        T value = state.get(property);
        return value.compareTo(minValue) >= 0 && value.compareTo(maxValue) <= 0;
    }

    @Override
    public void writeToPacket(PacketBuffer buffer) {
        buffer.writeEnumValue(MatcherType.RANGE);
        buffer.writeString(property.getName());
        buffer.writeString(property.getName(minValue));
        buffer.writeString(property.getName(maxValue));
    }

    public static StateMatcher create(StateContainer<?, ?> stateContainer, String propertyName, String minValueName,
            String maxValueName) {
        Property<?> property = PropertyUtils.getRequiredProperty(stateContainer, propertyName);
        return new RangeValueMatcher<>(property, minValueName, maxValueName);
    }

    @Environment(EnvType.CLIENT)
    public static StateMatcher readFromPacket(StateContainer<?, ?> stateContainer, PacketBuffer buffer) {
        String propertyName = buffer.readString();
        String minName = buffer.readString();
        String maxName = buffer.readString();
        return create(stateContainer, propertyName, minName, maxName);
    }
}