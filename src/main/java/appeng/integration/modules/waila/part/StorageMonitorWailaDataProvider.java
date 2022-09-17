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

package appeng.integration.modules.waila.part;


import appeng.api.implementations.parts.IPartStorageMonitor;
import appeng.api.parts.IPart;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.core.localization.WailaText;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

import java.util.List;


/**
 * Storage monitor provider for WAILA
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class StorageMonitorWailaDataProvider extends BasePartWailaDataProvider {
    /**
     * Displays the stack if present and if the monitor is locked.
     * Can handle fluids and items.
     *
     * @param part           maybe storage monitor
     * @param currentToolTip to be written to tooltip
     * @param accessor       information wrapper
     * @param config         config option
     * @return modified tooltip
     */
    @Override
    public List<String> getWailaBody(final IPart part, final List<String> currentToolTip, final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        if (part instanceof IPartStorageMonitor) {
            final IPartStorageMonitor monitor = (IPartStorageMonitor) part;

            final IAEStack<?> displayed = monitor.getDisplayed();
            final boolean isLocked = monitor.isLocked();

            // TODO: generalize
            if (displayed instanceof IAEItemStack) {
                final IAEItemStack ais = (IAEItemStack) displayed;
                currentToolTip.add(WailaText.Showing.getLocal() + ": " + ais.asItemStackRepresentation().getDisplayName());
            } else if (displayed instanceof IAEFluidStack) {
                final IAEFluidStack ais = (IAEFluidStack) displayed;
                currentToolTip.add(WailaText.Showing.getLocal() + ": " + ais.getFluid().getLocalizedName(ais.getFluidStack()));
            }

            currentToolTip.add((isLocked) ? WailaText.Locked.getLocal() : WailaText.Unlocked.getLocal());
        }

        return currentToolTip;
    }
}
