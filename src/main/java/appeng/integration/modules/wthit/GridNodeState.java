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

package appeng.integration.modules.wthit;

import javax.annotation.Nullable;

import net.minecraft.network.chat.MutableComponent;

import appeng.api.networking.IGridNode;
import appeng.core.localization.InGameTooltip;

public enum GridNodeState {
    OFFLINE(InGameTooltip.DeviceOffline),
    NETWORK_BOOTING(InGameTooltip.NetworkBooting),
    MISSING_CHANNEL(InGameTooltip.DeviceMissingChannel),
    ONLINE(InGameTooltip.DeviceOnline);

    private final InGameTooltip text;

    GridNodeState(InGameTooltip text) {
        this.text = text;
    }

    public MutableComponent textComponent() {
        return text.text();
    }

    public static GridNodeState fromNode(@Nullable IGridNode gridNode) {
        var state = GridNodeState.OFFLINE;
        if (gridNode != null && gridNode.isPowered()) {
            if (!gridNode.hasGridBooted()) {
                state = GridNodeState.NETWORK_BOOTING;
            } else if (!gridNode.meetsChannelRequirements()) {
                state = GridNodeState.MISSING_CHANNEL;
            } else {
                state = GridNodeState.ONLINE;
            }
        }
        return state;
    }

}
