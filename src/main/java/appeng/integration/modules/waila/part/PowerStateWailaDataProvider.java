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

import java.util.List;

import net.minecraft.util.text.ITextComponent;

import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.parts.IPart;
import appeng.core.localization.WailaText;

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
     * @param part     part with state
     * @param tooltip  to be added to tooltip
     * @param accessor wrapper for various information
     * @param config   config settings
     */
    @Override
    public void appendBody(final IPart part, final List<ITextComponent> tooltip, final IDataAccessor accessor,
            final IPluginConfig config) {
        if (part instanceof IPowerChannelState) {
            final IPowerChannelState state = (IPowerChannelState) part;

            tooltip.add(this.getToolTip(state.isActive(), state.isPowered()));
        }
    }

    /**
     * Gets the corresponding tool tip for different values of {@code #isActive} and {@code #isPowered}
     *
     * @param isActive  if part is active
     * @param isPowered if part is powered
     * @return tooltip of the state
     */
    private ITextComponent getToolTip(final boolean isActive, final boolean isPowered) {
        if (isActive && isPowered) {
            return WailaText.DeviceOnline.textComponent();
        } else if (isPowered) {
            return WailaText.DeviceMissingChannel.textComponent();
        } else {
            return WailaText.DeviceOffline.textComponent();
        }
    }
}
