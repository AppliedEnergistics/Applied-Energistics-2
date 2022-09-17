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

package appeng.integration.modules.theoneprobe;


import net.minecraft.util.text.translation.I18n;

import java.util.Locale;


public enum TheOneProbeText {
    CRAFTING,

    DEVICE_ONLINE,
    DEVICE_OFFLINE,
    DEVICE_MISSING_CHANNEL,

    P2P_UNLINKED,
    P2P_INPUT_ONE_OUTPUT,
    P2P_INPUT_MANY_OUTPUTS,
    P2P_OUTPUT_ONE_INPUT,
    P2P_OUTPUT_MANY_INPUTS,
    P2P_OUTPUT,
    P2P_FREQUENCY,

    LOCKED,
    UNLOCKED,
    SHOWING,

    CONTAINS,
    CHANNELS,

    STORED_ENERGY;

    private final String root;

    TheOneProbeText() {
        this.root = "theoneprobe.appliedenergistics2";
    }

    public String getLocal() {
        return I18n.translateToLocal(this.getUnlocalized());
    }

    public String getUnlocalized() {
        return this.root + '.' + this.name().toLowerCase(Locale.ENGLISH);
    }

}
