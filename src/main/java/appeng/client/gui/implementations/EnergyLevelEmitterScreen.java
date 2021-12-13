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

import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.menu.implementations.EnergyLevelEmitterMenu;

public class EnergyLevelEmitterScreen extends UpgradeableScreen<EnergyLevelEmitterMenu> {

    private final SettingToggleButton<RedstoneMode> redstoneMode;
    private final NumberEntryWidget level;

    public EnergyLevelEmitterScreen(EnergyLevelEmitterMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.redstoneMode = new ServerSettingToggleButton<>(
                Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL);
        this.addToLeftToolbar(this.redstoneMode);

        this.level = new NumberEntryWidget(NumberEntryType.ENERGY);
        this.level.setTextFieldStyle(style.getWidget("levelInput"));
        this.level.setLongValue(menu.getReportingValue());
        this.level.setOnChange(this::saveReportingValue);
        this.level.setOnConfirm(this::onClose);
        widgets.add("level", this.level);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.redstoneMode.active = true;
        this.redstoneMode.set(menu.getRedStoneMode());
    }

    private void saveReportingValue() {
        this.level.getLongValue().ifPresent(menu::setReportingValue);
    }

}
