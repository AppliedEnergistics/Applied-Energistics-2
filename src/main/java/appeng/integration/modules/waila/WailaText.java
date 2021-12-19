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

package appeng.integration.modules.waila;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public enum WailaText {
    Crafting,

    DeviceOnline, DeviceOffline, NetworkBooting, DeviceMissingChannel,

    P2PUnlinked, P2PInputOneOutput, P2PInputManyOutputs, P2POutput,

    Locked, Unlocked, Showing,

    Stored, Contains, Charged, ChannelsOf, Channels, ErrorNestedP2PTunnel;

    private final String root;

    WailaText() {
        this.root = "waila.ae2";
    }

    public MutableComponent textComponent() {
        return new TranslatableComponent(this.root + '.' + this.toString());
    }

    public MutableComponent textComponent(Object... args) {
        return new TranslatableComponent(this.root + '.' + this.toString(), args);
    }

}
