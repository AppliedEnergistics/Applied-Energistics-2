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

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.CommonButtons;
import appeng.container.implementations.WirelessContainer;
import appeng.core.localization.GuiText;
import appeng.util.Platform;

public class WirelessScreen extends AEBaseScreen<WirelessContainer> {

    public WirelessScreen(WirelessContainer container, PlayerInventory playerInventory, ITextComponent title,
            ScreenStyle style) {
        super(container, playerInventory, title, style);
    }

    @Override
    public void init() {
        super.init();

        this.addToLeftToolbar(CommonButtons.togglePowerUnit(0, 0));
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        ITextComponent rangeText = StringTextComponent.EMPTY;
        ITextComponent energyUseText = StringTextComponent.EMPTY;
        if (container.getRange() > 0) {
            double rangeBlocks = container.getRange() / 10.0;
            rangeText = GuiText.WirelessRange.text(rangeBlocks);
            energyUseText = GuiText.PowerUsageRate.text(Platform.formatPowerLong(container.getDrain(), true));
        }

        setTextContent("range", rangeText);
        setTextContent("energy_use", energyUseText);
    }

}
