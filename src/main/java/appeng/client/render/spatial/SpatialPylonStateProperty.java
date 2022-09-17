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

package appeng.client.render.spatial;


import net.minecraftforge.common.property.IUnlistedProperty;


/**
 * Models the rendering state of the spatial pylon, which is largely determined by the state of neighboring tiles.
 */
public class SpatialPylonStateProperty implements IUnlistedProperty<Integer> {

    @Override
    public String getName() {
        return "spatial_state";
    }

    @Override
    public boolean isValid(Integer value) {
        int val = value;
        // The lower 6 bits are used
        return (val & ~0x3F) == 0;
    }

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }

    @Override
    public String valueToString(Integer value) {
        return value.toString();
    }
}
