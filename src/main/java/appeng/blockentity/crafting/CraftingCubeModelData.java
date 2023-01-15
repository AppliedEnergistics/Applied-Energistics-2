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

package appeng.blockentity.crafting;

import java.util.EnumSet;
import java.util.Objects;

import net.minecraft.core.Direction;

import appeng.client.render.model.AEModelData;

public class CraftingCubeModelData extends AEModelData {

    // Contains information on which sides of the block are connected to other parts
    // of a formed crafting cube
    private final EnumSet<Direction> connections;

    public CraftingCubeModelData(EnumSet<Direction> connections) {
        this.connections = Objects.requireNonNull(connections);
    }

    @Override
    public boolean isCacheable() {
        return false; // Too many variants
    }

    public EnumSet<Direction> getConnections() {
        return connections;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        CraftingCubeModelData that = (CraftingCubeModelData) o;
        return connections.equals(that.connections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), connections);
    }

}
