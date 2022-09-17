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


import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.helpers.Reflected;
import appeng.util.Platform;
import net.minecraft.item.ItemStack;


public class PartCableSmart extends PartCable {
    @Reflected
    public PartCableSmart(final ItemStack is) {
        super(is);
    }

    @MENetworkEventSubscribe
    public void channelUpdated(final MENetworkChannelsChanged c) {
        this.getHost().markForUpdate();
    }

    @MENetworkEventSubscribe
    public void powerRender(final MENetworkPowerStatusChange c) {
        this.getHost().markForUpdate();
    }

    @Override
    public AECableType getCableConnectionType() {
        return AECableType.SMART;
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);

        if (Platform.isServer()) {
            final IGridNode n = this.getGridNode();
            if (n != null) {
                this.setConnections(n.getConnectedSides());
            } else {
                this.getConnections().clear();
            }
        }

        for (final AEPartLocation of : this.getConnections()) {
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
