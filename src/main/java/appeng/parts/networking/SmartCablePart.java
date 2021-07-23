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

import net.minecraft.world.item.ItemStack;

import appeng.api.networking.IGridNodeListener;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.util.AECableType;

public class SmartCablePart extends CablePart implements IUsedChannelProvider {
    public SmartCablePart(final ItemStack is) {
        super(is);
    }

    /**
     * Send info about changed channels/power to client to update the on-cable display of channels/power.
     * 
     * @param reason
     */
    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.getHost().markForUpdate();
    }

    @Override
    public AECableType getCableConnectionType() {
        return AECableType.SMART;
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        updateConnections();

        bch.addBox(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);

        for (var of : this.getConnections()) {
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
