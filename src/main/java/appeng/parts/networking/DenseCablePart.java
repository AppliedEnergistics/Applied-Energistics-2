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

package appeng.parts.networking;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPartCollisionHelper;
import appeng.items.parts.ColoredPartItem;

public abstract class DenseCablePart extends CablePart {
    public DenseCablePart(ColoredPartItem<?> partItem) {
        super(partItem);

        this.getMainNode().setFlags(GridFlags.DENSE_CAPACITY);
    }

    @Override
    public BusSupport supportsBuses() {
        return BusSupport.DENSE_CABLE;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch, Predicate<@Nullable Direction> filterConnections) {
        updateConnections();

        final boolean noLadder = !bch.isBBCollision();
        final double min = noLadder ? 3.0 : 4.9;
        final double max = noLadder ? 13.0 : 11.1;

        if (filterConnections.test(null)) {
            bch.addBox(min, min, min, max, max, max);
        }

        for (var of : this.getConnections()) {
            if (!filterConnections.test(of)) {
                continue;
            }

            if (this.isDense(of)) {
                addConnectionBox(bch, of, min, max, 0.0);
            } else {
                addConnectionBox(bch, of, 5.0, 11.0, 0.0);
            }
        }
    }

    private boolean isDense(Direction of) {
        var adjacentPos = getBlockEntity().getBlockPos().relative(of);

        if (!getLevel().hasChunkAt(adjacentPos)) {
            // Avoid loading chunk for this.
            return false;
        }

        var adjacentHost = GridHelper.getNodeHost(getBlockEntity().getLevel(), adjacentPos);

        if (adjacentHost != null) {
            var t = adjacentHost.getCableConnectionType(of.getOpposite());
            return t.isDense();
        }

        return false;
    }

}
