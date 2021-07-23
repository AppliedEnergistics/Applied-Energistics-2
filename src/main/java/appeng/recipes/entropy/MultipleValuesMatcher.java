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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
        return propertyValues.contains(state.getValue(property));
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buffer) {
        buffer.writeEnum(MatcherType.MULTIPLE);
        buffer.writeUtf(property.getName());
        buffer.writeInt(propertyValues.size());
        for (T value : propertyValues) {
            buffer.writeUtf(property.getName(value));
        }
    }

    public static MultipleValuesMatcher<?> create(StateDefinition<?, ?> stateContainer, String propertyName,
                                                  List<String> values) {
        Property<?> property = PropertyUtils.getRequiredProperty(stateContainer, propertyName);
        return new MultipleValuesMatcher<>(property, values);
    }

    @OnlyIn(Dist.CLIENT)
    public static MultipleValuesMatcher<?> readFromPacket(StateDefinition<?, ?> stateContainer, FriendlyByteBuf buffer) {
        String propertyName = buffer.readUtf();
        int size = buffer.readInt();
        List<String> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            values.add(buffer.readUtf());
        }

        return create(stateContainer, propertyName, values);
    }

}
