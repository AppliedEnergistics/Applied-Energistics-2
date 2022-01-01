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

package appeng.core.localization;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum PlayerMessages {
    AmmoDepleted("Ammo Depleted."),
    ChestCannotReadStorageCell("ME Chest cannot read storage cell."),
    CommunicationError("Error Communicating with Network."),
    DeviceNotLinked("Device is not linked."),
    DeviceNotPowered("Device is low on power."),
    InvalidMachine("Invalid Machine."),
    LoadedSettings("Loaded device configuration from memory card."),
    MachineNotPowered("Machine is not powered."),
    OutOfRange("Wireless Out Of Range."),
    ResetSettings("New device configuration created and copied to memory card."),
    SavedSettings("Copied current device configuration to memory card."),
    SettingCleared("Memory card cleared."),
    StationCanNotBeLocated("Station can not be located."),
    isNowLocked("Monitor is now Locked."),
    isNowUnlocked("Monitor is now Unlocked."),
    OnlyEmptyCellsCanBeDisassembled("Only empty storage cells can be disassembled."),
    UnsupportedUpgrade("This upgrade is not supported by this machine."),
    MaxUpgradesOfTypeInstalled("No further upgrade cards of this type can be installed."),
    MaxUpgradesInstalled("The upgrade capacity of this machine has been reached.");

    private final String englishText;

    PlayerMessages(String englishText) {
        this.englishText = englishText;
    }

    public Component get() {
        return new TranslatableComponent(this.getTranslationKey());
    }

    public String getEnglishText() {
        return englishText;
    }

    public String getTranslationKey() {
        return "chat.ae2." + name();
    }
}
