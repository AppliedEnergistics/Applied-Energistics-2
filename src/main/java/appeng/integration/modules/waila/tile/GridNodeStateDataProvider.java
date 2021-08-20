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

package appeng.integration.modules.waila.tile;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IPluginConfig;

import appeng.integration.modules.waila.BaseDataProvider;
import appeng.integration.modules.waila.GridNodeState;
import appeng.me.helpers.IGridConnectedBlockEntity;

/**
 * Provide info about the grid connection status of a machine.
 */
public final class GridNodeStateDataProvider extends BaseDataProvider {
    private static final String TAG_STATE = "gridNodeState";

    @Override
    public void appendBody(List<Component> tooltip, IBlockAccessor accessor, IPluginConfig config) {
        var tag = accessor.getServerData();
        if (tag.contains(TAG_STATE, Tag.TAG_BYTE)) {
            var state = GridNodeState.values()[tag.getByte(TAG_STATE)];
            tooltip.add(state.textComponent().withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer player, Level level, BlockEntity blockEntity) {
        if (blockEntity instanceof IGridConnectedBlockEntity gridConnectedBlockEntity) {
            var state = GridNodeState.fromNode(gridConnectedBlockEntity.getActionableNode());
            tag.putByte(TAG_STATE, (byte) state.ordinal());
        }
    }

}
