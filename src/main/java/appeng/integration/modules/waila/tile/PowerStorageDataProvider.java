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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;

import appeng.api.networking.energy.IAEPowerStorage;
import appeng.core.localization.InGameTooltip;
import appeng.integration.modules.waila.BaseDataProvider;
import appeng.util.Platform;

/**
 * Shows stored power and max stored power for an {@link IAEPowerStorage} block entity.
 */
public final class PowerStorageDataProvider extends BaseDataProvider {
    /**
     * Power key used for the transferred {@link net.minecraft.nbt.CompoundTag}
     */
    private static final String TAG_CURRENT_POWER = "currentPower";
    private static final String TAG_MAX_POWER = "maxPower";

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var tag = accessor.getServerData();
        if (tag.contains(TAG_MAX_POWER, Tag.TAG_DOUBLE)) {
            var currentPower = tag.getDouble(TAG_CURRENT_POWER);
            var maxPower = tag.getDouble(TAG_MAX_POWER);

            var formatCurrentPower = Platform.formatPower(currentPower, false);
            var formatMaxPower = Platform.formatPower(maxPower, false);

            tooltip.add(InGameTooltip.Stored.text(formatCurrentPower, formatMaxPower));
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer player, Level level, BlockEntity blockEntity,
            boolean showDetails) {
        if (blockEntity instanceof IAEPowerStorage storage) {
            if (storage.getAEMaxPower() > 0) {
                tag.putDouble(TAG_CURRENT_POWER, storage.getAECurrentPower());
                tag.putDouble(TAG_MAX_POWER, storage.getAEMaxPower());
            }
        }
    }

}
