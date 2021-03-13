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

import net.minecraft.network.PacketBuffer;
import net.minecraft.state.Property;
import net.minecraft.state.StateHolder;

import appeng.core.AELog;

/**
 * Matches a range between a min and max value (inclusive).
 */
class RangeValueMatcher extends StatePropertyMatcher {

    private final String propertyMinValue;
    private final String propertyMaxValue;

    public RangeValueMatcher(String propertyName, String propertyMinValue, String propertyMaxValue) {
        super(propertyName);
        this.propertyMinValue = propertyMinValue;
        this.propertyMaxValue = propertyMaxValue;
    }

    @Override
    protected <T extends Comparable<T>> boolean matchProperty(StateHolder<?, ?> state, Property<T> property) {
        T minValue = property.parseValue(this.propertyMinValue).orElse(null);
        if (minValue == null) {
            AELog.warn("Entropy manipulator range matcher has min-value '%s' unsupported by property '%s' on '%s'",
                    propertyMinValue, property.getName(), state);
            return false;
        }

        T maxValue = property.parseValue(this.propertyMaxValue).orElse(null);
        if (maxValue == null) {
            AELog.warn("Entropy manipulator range matcher has max-value '%s' unsupported by property '%s' on '%s'",
                    propertyMaxValue, property.getName(), state);
            return false;
        }

        T value = state.get(property);
        return value.compareTo(minValue) >= 0 && value.compareTo(maxValue) <= 0;
    }

    @Override
    void writeToPacket(PacketBuffer buffer) {
        buffer.writeEnumValue(MatcherType.RANGE);
        buffer.writeString(propertyName);
        buffer.writeString(propertyMinValue);
        buffer.writeString(propertyMaxValue);
    }

    public static StatePropertyMatcher readFromPacket(PacketBuffer buffer) {
        String key = buffer.readString();
        String min = buffer.readString();
        String max = buffer.readString();

        return new RangeValueMatcher(key, min, max);
    }
}