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

package appeng.integration.modules.waila.part;

import net.minecraft.nbt.CompoundTag;

import mcp.mobius.waila.api.ITooltip;

import appeng.api.parts.IPart;
import appeng.integration.modules.waila.WailaText;
import appeng.parts.reporting.StorageMonitorPart;
import appeng.util.Platform;

/**
 * Displays the stack if present and if the monitor is locked.
 */
public final class StorageMonitorDataProvider implements IPartDataProvider {

    @Override
    public void appendBodyTooltip(IPart part, CompoundTag partTag, ITooltip tooltip) {
        if (part instanceof StorageMonitorPart monitor) {
            var displayed = monitor.getDisplayed();
            var isLocked = monitor.isLocked();

            tooltip.add(WailaText.Showing.textComponent().append(": ")
                    .append(Platform.getItemDisplayName(displayed)));

            tooltip.add(isLocked ? WailaText.Locked.textComponent() : WailaText.Unlocked.textComponent());
        }
    }

}
