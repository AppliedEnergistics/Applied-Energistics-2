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

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.CommonButtons;
import appeng.container.implementations.SpatialIOPortContainer;
import appeng.core.localization.GuiText;
import appeng.util.Platform;

public class SpatialIOPortScreen extends AEBaseScreen<SpatialIOPortContainer> {

    public SpatialIOPortScreen(SpatialIOPortContainer container, PlayerInventory playerInventory,
            ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title, style);
        this.addToLeftToolbar(CommonButtons.togglePowerUnit());
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        setTextContent("stored_power",
                GuiText.StoredPower.text(Platform.formatPowerLong(this.container.getCurrentPower(), false)));
        setTextContent("max_power",
                GuiText.MaxPower.text(Platform.formatPowerLong(this.container.getMaxPower(), false)));
        setTextContent("required_power",
                GuiText.RequiredPower.text(Platform.formatPowerLong(this.container.getRequiredPower(), false)));
        setTextContent("efficiency", GuiText.Efficiency.text((float) this.container.getEfficency() / 100));

        ITextComponent scsSizeText;
        if (this.container.xSize != 0 && this.container.ySize != 0 && this.container.zSize != 0) {
            scsSizeText = GuiText.SCSSize.text(this.container.xSize, this.container.ySize, this.container.zSize);
        } else {
            scsSizeText = GuiText.SCSInvalid.text();
        }
        setTextContent("scs_size", scsSizeText);
    }

}
