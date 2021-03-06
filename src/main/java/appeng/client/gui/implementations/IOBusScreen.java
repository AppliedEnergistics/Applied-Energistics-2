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

package appeng.client.gui.implementations;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.container.implementations.IOBusContainer;
import appeng.parts.automation.ExportBusPart;

public class IOBusScreen extends UpgradeableScreen<IOBusContainer> {

    private final SettingToggleButton<RedstoneMode> redstoneMode;
    private final SettingToggleButton<FuzzyMode> fuzzyMode;
    private final SettingToggleButton<YesNo> craftMode;
    private final SettingToggleButton<SchedulingMode> schedulingMode;

    public IOBusScreen(IOBusContainer container, PlayerInventory playerInventory, ITextComponent title,
            ScreenStyle style) {
        super(container, playerInventory, title, style);

        this.redstoneMode = new ServerSettingToggleButton<>(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        addToLeftToolbar(this.redstoneMode);
        this.fuzzyMode = new ServerSettingToggleButton<>(Settings.FUZZY_MODE,
                FuzzyMode.IGNORE_ALL);
        addToLeftToolbar(this.fuzzyMode);

        // Craft & Scheduling mode is only supported by export bus
        if (container.getUpgradeable() instanceof ExportBusPart) {
            this.craftMode = new ServerSettingToggleButton<>(Settings.CRAFT_ONLY,
                    YesNo.NO);
            addToLeftToolbar(this.craftMode);

            this.schedulingMode = new ServerSettingToggleButton<>(
                    Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT);
            addToLeftToolbar(this.schedulingMode);
        } else {
            this.craftMode = null;
            this.schedulingMode = null;
        }
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.redstoneMode.set(container.getRedStoneMode());
        this.redstoneMode.setVisibility(container.hasUpgrade(Upgrades.REDSTONE));
        this.fuzzyMode.set(container.getFuzzyMode());
        this.fuzzyMode.setVisibility(container.hasUpgrade(Upgrades.FUZZY));
        if (this.craftMode != null) {
            this.craftMode.set(container.getCraftingMode());
            this.craftMode.setVisibility(container.hasUpgrade(Upgrades.CRAFTING));
        }
        if (this.schedulingMode != null) {
            this.schedulingMode.set(container.getSchedulingMode());
            this.schedulingMode.setVisibility(container.hasUpgrade(Upgrades.CAPACITY));
        }
    }

}
