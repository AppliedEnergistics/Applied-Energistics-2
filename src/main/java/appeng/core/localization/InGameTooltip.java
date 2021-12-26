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

package appeng.core.localization;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * Texts used for in-game tooltip mods like WAILA, TOP, Jade, WTHIT, etc.
 */
public enum InGameTooltip {
    Channels("%1$d Channels"),
    ChannelsOf("%1$d of %2$d Channels"),
    Charged("%d%% charged"),
    Contains("Contains: %s"),
    Crafting("Crafting: %s"),
    DeviceMissingChannel("Device Missing Channel"),
    DeviceOffline("Device Offline"),
    DeviceOnline("Device Online"),
    ErrorControllerConflict("Error: Controller Conflict"),
    ErrorNestedP2PTunnel("Error: Nested P2P Tunnel"),
    ErrorTooManyChannels("Error: Too Many Channels"),
    Locked("Locked"),
    NetworkBooting("Network Booting"),
    P2PInputManyOutputs("Linked (Input Side) - %d Outputs"),
    P2PInputOneOutput("Linked (Input Side)"),
    P2POutput("Linked (Output Side)"),
    P2PUnlinked("Unlinked"),
    Showing("Showing"),
    Stored("Stored: %s / %s"),
    Unlocked("Unlocked");

    private final String englishText;

    InGameTooltip(String englishText) {
        this.englishText = englishText;
    }

    public String getTranslationKey() {
        return "waila.ae2." + name();
    }

    public String getEnglishText() {
        return englishText;
    }

    public MutableComponent textComponent() {
        return new TranslatableComponent(getTranslationKey());
    }

    public MutableComponent textComponent(Object... args) {
        return new TranslatableComponent(getTranslationKey(), args);
    }

}
