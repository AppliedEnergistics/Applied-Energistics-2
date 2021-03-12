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
import net.minecraft.state.StateContainer;
import net.minecraft.state.StateHolder;

/**
 * Matches a range between a min and max value (inclusive).
 */
class RangeValueMatcher extends StateMatcher {

    private final String propertyName;
    private final String propertyMinValue;
    private final String propertyMaxValue;

    public RangeValueMatcher(String propertyName, String propertyMinValue, String propertyMaxValue) {
        this.propertyName = propertyName;
        this.propertyMinValue = propertyMinValue;
        this.propertyMaxValue = propertyMaxValue;
    }

    @Override
    public boolean matches(StateContainer<?, ?> base, StateHolder<?, ?> state) {
        Property<?> baseProperty = base.getProperty(this.propertyName);
        Comparable property = state.get(baseProperty);
        Comparable minValue = baseProperty.parseValue(this.propertyMinValue).orElse(null);
        Comparable maxValue = baseProperty.parseValue(this.propertyMaxValue).orElse(null);

        return property.compareTo(minValue) >= 0 && property.compareTo(maxValue) <= 0;
    }

    @Override
    void writeToPacket(PacketBuffer buffer) {
        buffer.writeEnumValue(MatcherType.RANGE);
        buffer.writeString(propertyName);
        buffer.writeString(propertyMinValue);
        buffer.writeString(propertyMaxValue);
    }

    public static StateMatcher readFromPacket(PacketBuffer buffer) {
        String key = buffer.readString();
        String min = buffer.readString();
        String max = buffer.readString();

        return new RangeValueMatcher(key, min, max);
    }
}