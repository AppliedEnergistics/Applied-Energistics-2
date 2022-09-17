/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.client;


import net.minecraftforge.common.property.IUnlistedProperty;


/**
 * A generic implementation for {@link IUnlistedProperty}.
 *
 * @param <T>
 */
public class UnlistedProperty<T> implements IUnlistedProperty<T> {

    private final String name;

    private final Class<T> clazz;

    public UnlistedProperty(String name, Class<T> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isValid(T value) {
        return value != null;
    }

    @Override
    public Class<T> getType() {
        return this.clazz;
    }

    @Override
    public String valueToString(T value) {
        return value.toString();
    }

}
