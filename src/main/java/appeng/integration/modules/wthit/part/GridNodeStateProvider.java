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

package appeng.integration.modules.wthit.part;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import mcp.mobius.waila.api.ITooltip;

import appeng.api.parts.IPart;
import appeng.integration.modules.wthit.GridNodeState;

/**
 * Provide info about the grid connection status of a part.
 */
public final class GridNodeStateProvider implements IPartDataProvider {
    private static final String TAG_STATE = "gridNodeState";

    @Override
    public void appendBody(IPart part, CompoundTag partTag, ITooltip tooltip) {
        if (partTag.contains(TAG_STATE, Tag.TAG_BYTE)) {
            var state = GridNodeState.values()[partTag.getByte(TAG_STATE)];
            tooltip.addLine(state.textComponent().withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void appendServerData(ServerPlayer player, IPart part, CompoundTag partTag) {
        var state = GridNodeState.fromNode(part.getGridNode());
        partTag.putByte(TAG_STATE, (byte) state.ordinal());
    }

}
