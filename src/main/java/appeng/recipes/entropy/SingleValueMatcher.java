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

import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.StateHolder;

/**
 * Matches an exact value.
 */
class SingleValueMatcher implements StateMatcher {

    private final String propertyName;
    private final String propertyValue;

    public SingleValueMatcher(String propertyName, String propertyValue) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    @Override
    public boolean matches(StateContainer<?, ?> base, StateHolder<?, ?> state) {
        Property<?> baseProperty = base.getProperty(this.propertyName);
        Comparable<?> property = state.get(baseProperty);
        Comparable<?> expectedValue = baseProperty.parseValue(this.propertyValue).orElse(null);

        return property.equals(expectedValue);
    }
}