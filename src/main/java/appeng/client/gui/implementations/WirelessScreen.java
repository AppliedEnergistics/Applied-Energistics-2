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

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.CommonButtons;
import appeng.core.localization.GuiText;
import appeng.menu.implementations.WirelessMenu;
import appeng.util.Platform;

public class WirelessScreen extends AEBaseScreen<WirelessMenu> {

    public WirelessScreen(WirelessMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.addToLeftToolbar(CommonButtons.togglePowerUnit());
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        Component rangeText = Component.empty();
        Component energyUseText = Component.empty();
        if (menu.getRange() > 0) {
            double rangeBlocks = menu.getRange() / 10.0;
            rangeText = GuiText.WirelessRange.text(rangeBlocks);
            energyUseText = GuiText.PowerUsageRate.text(Platform.formatPowerLong(menu.getDrain(), true));
        }

        setTextContent("range", rangeText);
        setTextContent("energy_use", energyUseText);
    }

}
