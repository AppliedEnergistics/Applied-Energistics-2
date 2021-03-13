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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.network.PacketBuffer;
import net.minecraft.state.Property;
import net.minecraft.state.StateHolder;

/**
 * Matches against a list of values.
 */
class MultipleValuesMatcher extends StatePropertyMatcher {

    private final Set<String> propertyValues;

    public MultipleValuesMatcher(String propertyName, List<String> propertyValues) {
        super(propertyName);
        this.propertyValues = ImmutableSet.copyOf(propertyValues);
    }

    protected <T extends Comparable<T>> boolean matchProperty(StateHolder<?, ?> state, Property<T> property) {
        String valueString = property.getName(state.get(property));
        return propertyValues.contains(valueString);
    }

    @Override
    void writeToPacket(PacketBuffer buffer) {
        buffer.writeEnumValue(MatcherType.MULTIPLE);
        buffer.writeString(propertyName);
        buffer.writeInt(propertyValues.size());
        for (String value : propertyValues) {
            buffer.writeString(value);
        }
    }

    public static StatePropertyMatcher readFromPacket(PacketBuffer buffer) {
        String propertyName = buffer.readString();
        int size = buffer.readInt();

        List<String> values = new ArrayList<String>(size);
        for (int i = 0; i < size; i++) {
            values.add(buffer.readString());
        }
        return new MultipleValuesMatcher(propertyName, values);
    }

}