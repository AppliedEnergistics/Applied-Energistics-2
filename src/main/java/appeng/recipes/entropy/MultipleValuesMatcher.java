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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.StateHolder;

/**
 * Matches against a list of values.
 */
class MultipleValuesMatcher<T extends Comparable<T>> implements StateMatcher {

    private final Property<T> property;
    private final Set<T> propertyValues;

    public MultipleValuesMatcher(Property<T> property, List<String> propertyValues) {
        this.property = Objects.requireNonNull(property, "property must be not null");
        this.propertyValues = propertyValues.stream()
                .map(name -> PropertyUtils.getRequiredPropertyValue(property, name)).collect(Collectors.toSet());
    }

    @Override
    public boolean matches(StateHolder<?, ?> state) {
        return propertyValues.contains(state.get(property));
    }

    @Override
    public void writeToPacket(PacketBuffer buffer) {
        buffer.writeEnumValue(MatcherType.MULTIPLE);
        buffer.writeString(property.getName());
        buffer.writeInt(propertyValues.size());
        for (T value : propertyValues) {
            buffer.writeString(property.getName(value));
        }
    }

    public static MultipleValuesMatcher<?> create(StateContainer<?, ?> stateContainer, String propertyName,
            List<String> values) {
        Property<?> property = PropertyUtils.getRequiredProperty(stateContainer, propertyName);
        return new MultipleValuesMatcher<>(property, values);
    }

    @Environment(EnvType.CLIENT)
    public static MultipleValuesMatcher<?> readFromPacket(StateContainer<?, ?> stateContainer, PacketBuffer buffer) {
        String propertyName = buffer.readString();
        int size = buffer.readInt();
        List<String> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            values.add(buffer.readString());
        }

        return create(stateContainer, propertyName, values);
    }

}