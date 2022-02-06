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

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;

import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.core.localization.InGameTooltip;
import appeng.integration.modules.waila.BaseDataProvider;

/**
 * Shows the name of the item being crafted.
 */
public final class CraftingMonitorDataProvider extends BaseDataProvider {

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var blockEntity = accessor.getBlockEntity();
        if (blockEntity instanceof CraftingMonitorBlockEntity monitor) {
            var displayStack = monitor.getJobProgress();

            if (displayStack != null) {
                tooltip.add(InGameTooltip.Crafting.text(displayStack.what().getDisplayName()));
            }
        }
    }
}
