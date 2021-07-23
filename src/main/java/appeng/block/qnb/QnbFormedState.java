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

package appeng.block.qnb;

import java.util.Set;

import net.minecraft.core.Direction;

public class QnbFormedState {

    private final Set<net.minecraft.core.Direction> adjacentQuantumBridges;

    private final boolean corner;

    private final boolean powered;

    public QnbFormedState(Set<net.minecraft.core.Direction> adjacentQuantumBridges, boolean corner, boolean powered) {
        this.adjacentQuantumBridges = adjacentQuantumBridges;
        this.corner = corner;
        this.powered = powered;
    }

    public Set<Direction> getAdjacentQuantumBridges() {
        return this.adjacentQuantumBridges;
    }

    public boolean isCorner() {
        return this.corner;
    }

    public boolean isPowered() {
        return this.powered;
    }

}
