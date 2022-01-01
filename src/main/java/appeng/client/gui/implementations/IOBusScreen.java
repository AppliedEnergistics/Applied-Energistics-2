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

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.definitions.AEItems;
import appeng.menu.implementations.IOBusMenu;

public class IOBusScreen extends UpgradeableScreen<IOBusMenu> {

    private final SettingToggleButton<RedstoneMode> redstoneMode;
    private final SettingToggleButton<FuzzyMode> fuzzyMode;
    private final SettingToggleButton<YesNo> craftMode;
    private final SettingToggleButton<SchedulingMode> schedulingMode;

    public IOBusScreen(IOBusMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.redstoneMode = new ServerSettingToggleButton<>(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        addToLeftToolbar(this.redstoneMode);
        this.fuzzyMode = new ServerSettingToggleButton<>(Settings.FUZZY_MODE,
                FuzzyMode.IGNORE_ALL);
        addToLeftToolbar(this.fuzzyMode);

        if (menu.getHost().getConfigManager().hasSetting(Settings.CRAFT_ONLY)) {
            this.craftMode = new ServerSettingToggleButton<>(Settings.CRAFT_ONLY, YesNo.NO);
            addToLeftToolbar(this.craftMode);
        } else {
            this.craftMode = null;
        }

        if (menu.getHost().getConfigManager().hasSetting(Settings.SCHEDULING_MODE)) {
            this.schedulingMode = new ServerSettingToggleButton<>(Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT);
            addToLeftToolbar(this.schedulingMode);
        } else {
            this.schedulingMode = null;
        }
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.redstoneMode.set(menu.getRedStoneMode());
        this.redstoneMode.setVisibility(menu.hasUpgrade(AEItems.REDSTONE_CARD));
        this.fuzzyMode.set(menu.getFuzzyMode());
        this.fuzzyMode.setVisibility(menu.hasUpgrade(AEItems.FUZZY_CARD));
        if (this.craftMode != null) {
            this.craftMode.set(menu.getCraftingMode());
            this.craftMode.setVisibility(menu.hasUpgrade(AEItems.CRAFTING_CARD));
        }
        if (this.schedulingMode != null) {
            this.schedulingMode.set(menu.getSchedulingMode());
            this.schedulingMode.setVisibility(menu.hasUpgrade(AEItems.CAPACITY_CARD));
        }
    }

}
