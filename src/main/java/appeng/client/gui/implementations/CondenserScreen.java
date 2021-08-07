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

package appeng.client.gui.implementations;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ProgressBar.Direction;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.localization.GuiText;
import appeng.menu.implementations.CondenserMenu;

public class CondenserScreen extends AEBaseScreen<CondenserMenu> {

    private final SettingToggleButton<CondenserOutput> mode;

    public CondenserScreen(CondenserMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.mode = new ServerSettingToggleButton<>(Settings.CONDENSER_OUTPUT, this.menu.getOutput());
        widgets.add("mode", this.mode);
        widgets.add("progressBar", new ProgressBar(this.menu, style.getImage("progressBar"),
                Direction.VERTICAL, GuiText.StoredEnergy.text()));
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.mode.set(this.menu.getOutput());
    }

}
