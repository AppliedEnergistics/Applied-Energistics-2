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

import appeng.core.AELog;

/**
 * An interface to match against the passed block/fluid state
 */
abstract class StatePropertyMatcher {
    protected final String propertyName;

    public StatePropertyMatcher(String propertyName) {
        this.propertyName = propertyName;
    }

    public final boolean matches(StateContainer<?, ?> base, StateHolder<?, ?> state) {
        Property<?> baseProperty = base.getProperty(this.propertyName);
        if (baseProperty == null) {
            AELog.warn("Entropy manipulator recipe failed to find property '%s' on state container '%s'",
                    this.propertyName, base);
            return false;
        }
        return matchProperty(state, baseProperty);
    }

    protected abstract <T extends Comparable<T>> boolean matchProperty(StateHolder<?, ?> state, Property<T> property);

    abstract void writeToPacket(PacketBuffer buffer);

    static StatePropertyMatcher read(PacketBuffer buffer) {
        MatcherType type = buffer.readEnumValue(MatcherType.class);

        switch (type) {
            case SINGLE:
                return SingleValueMatcher.readFromPacket(buffer);
            case MULTIPLE:
                return MultipleValuesMatcher.readFromPacket(buffer);
            case RANGE:
                return RangeValueMatcher.readFromPacket(buffer);
        }

        return null;
    }

    enum MatcherType {
        SINGLE, MULTIPLE, RANGE;
    }
}