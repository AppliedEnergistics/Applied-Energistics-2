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
import java.util.Optional;

import net.minecraft.network.PacketBuffer;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.StateHolder;

/**
 * Matches against a list of values.
 */
class MultipleValuesMatcher extends StateMatcher {

    private final String propertyName;
    private final List<String> propertyValues;

    public MultipleValuesMatcher(String propertyName, List<String> propertyValue) {
        this.propertyName = propertyName;
        this.propertyValues = propertyValue;
    }

    @Override
    public boolean matches(StateContainer<?, ?> base, StateHolder<?, ?> state) {
        Property<?> baseProperty = base.getProperty(this.propertyName);
        Comparable<?> property = state.get(baseProperty);
        return this.propertyValues.stream().map(baseProperty::parseValue).filter(Optional::isPresent)
                .anyMatch(p -> property.equals(p));
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

    public static StateMatcher readFromPacket(PacketBuffer buffer) {
        String key = buffer.readString();
        int size = buffer.readInt();

        List<String> values = new ArrayList<String>(size);
        for (int i = 0; i < size; i++) {
            values.add(buffer.readString());
        }
        return new MultipleValuesMatcher(key, values);
    }

}