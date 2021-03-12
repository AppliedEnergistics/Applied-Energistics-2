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

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;

/**
 * Applies a property to a {@link FluidState}
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class FluidStateApplier {

    private final String key;
    private final String value;

    public FluidStateApplier(String key, String value) {
        this.key = key;
        this.value = value;
    }

    FluidState apply(FluidState state) {
        StateContainer<Fluid, FluidState> base = state.getFluid().getStateContainer();

        Property property = base.getProperty(key);
        Comparable propertyValue = (Comparable) property.parseValue(value).orElse(null);

        return state.with(property, propertyValue);
    }

    void writeToPacket(PacketBuffer buffer) {
        buffer.writeString(key);
        buffer.writeString(value);
    }

    static FluidStateApplier read(PacketBuffer buffer) {
        String key = buffer.readString();
        String value = buffer.readString();

        return new FluidStateApplier(key, value);
    }
}