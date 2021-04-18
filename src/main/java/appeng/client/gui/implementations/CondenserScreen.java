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

import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ProgressBar;
import appeng.client.gui.widgets.ProgressBar.Direction;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.container.implementations.CondenserContainer;
import appeng.core.localization.GuiText;

public class CondenserScreen extends AEBaseScreen<CondenserContainer> {

    private static final Blitter PROGRESS_BAR = Blitter.texture("guis/condenser.png").src(178, 25, 6, 18);

    private SettingToggleButton<CondenserOutput> mode;

    public CondenserScreen(CondenserContainer container, PlayerInventory playerInventory, ITextComponent title,
            ScreenStyle style) {
        super(container, playerInventory, title, style);
    }

    @Override
    public void init() {
        super.init();

        this.mode = new ServerSettingToggleButton<>(128 + this.guiLeft, 52 + this.guiTop, Settings.CONDENSER_OUTPUT,
                this.container.getOutput());

        this.addButton(new ProgressBar(this.container, this.guiLeft + 120, this.guiTop + 25, PROGRESS_BAR,
                Direction.VERTICAL, GuiText.StoredEnergy.text()));
        this.addButton(this.mode);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.mode.set(this.container.getOutput());
        this.mode.setFillVar(String.valueOf(this.container.getOutput().requiredPower));
    }

}
