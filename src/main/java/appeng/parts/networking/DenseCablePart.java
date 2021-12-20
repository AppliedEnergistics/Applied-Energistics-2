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

import net.minecraft.core.Direction;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPartCollisionHelper;
import appeng.items.parts.ColoredPartItem;

public abstract class DenseCablePart extends CablePart {
    public DenseCablePart(ColoredPartItem<?> is) {
        super(is);

        this.getMainNode().setFlags(GridFlags.DENSE_CAPACITY, GridFlags.PREFERRED);
    }

    @Override
    public BusSupport supportsBuses() {
        return BusSupport.DENSE_CABLE;
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        updateConnections();

        final boolean noLadder = !bch.isBBCollision();
        final double min = noLadder ? 3.0 : 4.9;
        final double max = noLadder ? 13.0 : 11.1;

        bch.addBox(min, min, min, max, max, max);

        for (var of : this.getConnections()) {
            if (this.isDense(of)) {
                switch (of) {
                    case DOWN:
                        bch.addBox(min, 0.0, min, max, min, max);
                        break;
                    case EAST:
                        bch.addBox(max, min, min, 16.0, max, max);
                        break;
                    case NORTH:
                        bch.addBox(min, min, 0.0, max, max, min);
                        break;
                    case SOUTH:
                        bch.addBox(min, min, max, max, max, 16.0);
                        break;
                    case UP:
                        bch.addBox(min, max, min, max, 16.0, max);
                        break;
                    case WEST:
                        bch.addBox(0.0, min, min, min, max, max);
                        break;
                    default:
                }
            } else {
                switch (of) {
                    case DOWN:
                        bch.addBox(5.0, 0.0, 5.0, 11.0, 5.0, 11.0);
                        break;
                    case EAST:
                        bch.addBox(11.0, 5.0, 5.0, 16.0, 11.0, 11.0);
                        break;
                    case NORTH:
                        bch.addBox(5.0, 5.0, 0.0, 11.0, 11.0, 5.0);
                        break;
                    case SOUTH:
                        bch.addBox(5.0, 5.0, 11.0, 11.0, 11.0, 16.0);
                        break;
                    case UP:
                        bch.addBox(5.0, 11.0, 5.0, 11.0, 16.0, 11.0);
                        break;
                    case WEST:
                        bch.addBox(0.0, 5.0, 5.0, 5.0, 11.0, 11.0);
                        break;
                    default:
                }
            }
        }
    }

    private boolean isDense(final Direction of) {
        var adjacentHost = GridHelper.getNodeHost(getBlockEntity().getLevel(),
                getBlockEntity().getBlockPos().relative(of));

        if (adjacentHost != null) {
            var t = adjacentHost.getCableConnectionType(of.getOpposite());
            return t.isDense();
        }

        return false;
    }

}
