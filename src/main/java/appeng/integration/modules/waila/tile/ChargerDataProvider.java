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

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;

import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.integration.modules.waila.BaseDataProvider;
import appeng.integration.modules.waila.WailaText;

/**
 * Shows the tooltip of the item being charged, which usually includes a charge meter.
 */
public final class ChargerDataProvider extends BaseDataProvider {

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var blockEntity = accessor.getBlockEntity();
        if (blockEntity instanceof ChargerBlockEntity charger) {
            var chargerInventory = charger.getInternalInventory();
            var chargingItem = chargerInventory.getStackInSlot(0);

            if (!chargingItem.isEmpty()) {
                var player = accessor.getPlayer();

                var tooltipFlag = Minecraft.getInstance().options.advancedItemTooltips
                        ? TooltipFlag.Default.ADVANCED
                        : TooltipFlag.Default.NORMAL;
                var lines = chargingItem.getTooltipLines(player, tooltipFlag);
                if (lines.isEmpty()) {
                    return;
                }

                // Prepend the first line with Contains:
                lines.set(0, WailaText.Contains.textComponent(lines.get(0)));
                tooltip.addAll(lines);
            }
        }

    }
}
