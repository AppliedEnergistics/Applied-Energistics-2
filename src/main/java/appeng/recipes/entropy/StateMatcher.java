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
import net.minecraft.state.StateContainer;
import net.minecraft.state.StateHolder;

public interface StateMatcher {
    boolean matches(StateHolder<?, ?> state);

    void writeToPacket(PacketBuffer buffer);

    static StateMatcher read(StateContainer<?, ?> stateContainer, PacketBuffer buffer) {
        MatcherType type = buffer.readEnum(MatcherType.class);

        switch (type) {
            case SINGLE:
                return SingleValueMatcher.readFromPacket(stateContainer, buffer);
            case MULTIPLE:
                return MultipleValuesMatcher.readFromPacket(stateContainer, buffer);
            case RANGE:
                return RangeValueMatcher.readFromPacket(stateContainer, buffer);
        }

        return null;
    }

    enum MatcherType {
        SINGLE, MULTIPLE, RANGE;
    }
}
