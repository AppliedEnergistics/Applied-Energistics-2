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


import appeng.api.implementations.IPowerChannelState;
import appeng.api.parts.IPart;
import appeng.core.localization.WailaText;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

import java.util.List;


/**
 * Power state provider for WAILA
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class PowerStateWailaDataProvider extends BasePartWailaDataProvider {
    /**
     * Adds state to the tooltip
     *
     * @param part           part with state
     * @param currentToolTip to be added to tooltip
     * @param accessor       wrapper for various information
     * @param config         config settings
     * @return modified tooltip
     */
    @Override
    public List<String> getWailaBody(final IPart part, final List<String> currentToolTip, final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        if (part instanceof IPowerChannelState) {
            final IPowerChannelState state = (IPowerChannelState) part;

            currentToolTip.add(this.getToolTip(state.isActive(), state.isPowered()));
        }

        return currentToolTip;
    }

    /**
     * Gets the corresponding tool tip for different values of {@code #isActive} and {@code #isPowered}
     *
     * @param isActive  if part is active
     * @param isPowered if part is powered
     * @return tooltip of the state
     */
    private String getToolTip(final boolean isActive, final boolean isPowered) {
        final String result;

        if (isActive && isPowered) {
            result = WailaText.DeviceOnline.getLocal();
        } else if (isPowered) {
            result = WailaText.DeviceMissingChannel.getLocal();
        } else {
            result = WailaText.DeviceOffline.getLocal();
        }

        return result;
    }
}
