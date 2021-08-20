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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Matches an exact value.
 */
class SingleValueMatcher<T extends Comparable<T>> implements StateMatcher {

    private final Property<T> property;
    private final T value;

    private SingleValueMatcher(Property<T> property, String value) {
        this.property = Objects.requireNonNull(property, "property must not be null");
        this.value = PropertyUtils.getRequiredPropertyValue(property, value);
    }

    @Override
    public boolean matches(StateHolder<?, ?> state) {
        return value.equals(state.getValue(property));
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buffer) {
        buffer.writeEnum(MatcherType.SINGLE);
        buffer.writeUtf(property.getName());
        buffer.writeUtf(property.getName(value));
    }

    public String getValueName() {
        return property.getName(value);
    }

    @Override
    public Property<T> getProperty() {
        return property;
    }

    public static SingleValueMatcher<?> create(StateDefinition<?, ?> stateDefinition, String propertyName,
            String value) {
        Property<?> property = PropertyUtils.getRequiredProperty(stateDefinition, propertyName);
        return new SingleValueMatcher<>(property, value);
    }

    @Environment(EnvType.CLIENT)
    public static SingleValueMatcher<?> readFromPacket(StateDefinition<?, ?> stateDefinition, FriendlyByteBuf buffer) {
        String propertyName = buffer.readUtf();
        String value = buffer.readUtf();
        return create(stateDefinition, propertyName, value);
    }
}
