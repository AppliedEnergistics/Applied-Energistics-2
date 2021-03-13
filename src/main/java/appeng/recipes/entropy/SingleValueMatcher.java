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

import net.minecraft.network.PacketBuffer;
import net.minecraft.state.Property;
import net.minecraft.state.StateHolder;

/**
 * Matches an exact value.
 */
class SingleValueMatcher extends StatePropertyMatcher {
    private final String propertyValue;

    public SingleValueMatcher(String propertyName, String propertyValue) {
        super(propertyName);
        this.propertyValue = Objects.requireNonNull(propertyValue, "propertyValue");
    }

    @Override
    protected <T extends Comparable<T>> boolean matchProperty(StateHolder<?, ?> state, Property<T> property) {
        return propertyValue.equals(property.getName(state.get(property)));
    }

    @Override
    void writeToPacket(PacketBuffer buffer) {
        buffer.writeEnumValue(MatcherType.SINGLE);
        buffer.writeString(propertyName);
        buffer.writeString(propertyValue);
    }

    public static StatePropertyMatcher readFromPacket(PacketBuffer buffer) {
        String key = buffer.readString();
        String value = buffer.readString();

        return new SingleValueMatcher(key, value);
    }

}