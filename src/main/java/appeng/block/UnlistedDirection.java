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

package appeng.block;


import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IUnlistedProperty;


public class UnlistedDirection implements IUnlistedProperty<EnumFacing> {

    private final String name;

    public UnlistedDirection(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isValid(EnumFacing value) {
        return value != null;
    }

    @Override
    public Class<EnumFacing> getType() {
        return EnumFacing.class;
    }

    @Override
    public String valueToString(EnumFacing value) {
        return value.getName();
    }

}
