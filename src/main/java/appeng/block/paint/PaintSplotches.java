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

package appeng.block.paint;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import appeng.helpers.Splotch;

/**
 * Used to transfer the state about paint splotches from the game thread to the render thread.
 */
public class PaintSplotches {

    private final List<Splotch> splotches;

    public PaintSplotches(Collection<Splotch> splotches) {
        this.splotches = ImmutableList.copyOf(splotches);
    }

    List<Splotch> getSplotches() {
        return this.splotches;
    }

}
