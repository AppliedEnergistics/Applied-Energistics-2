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

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.Blitter;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.TabButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.container.implementations.InterfaceContainer;
import appeng.container.implementations.PriorityContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigButtonPacket;
import appeng.core.sync.packets.SwitchGuisPacket;

public class InterfaceScreen extends UpgradeableScreen<InterfaceContainer> {

    private static final Blitter BACKGROUND = Blitter.texture("guis/interface.png")
            .src(0, 0, 176, 211);

    private SettingToggleButton<YesNo> blockMode;
    private ToggleButton interfaceMode;

    public InterfaceScreen(InterfaceContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title, BACKGROUND);
    }

    @Override
    public void init() {
        super.init();

        this.addButton(new TabButton(this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.text(),
                this.itemRenderer, btn -> openPriorityGui()));

        this.blockMode = new ServerSettingToggleButton<>(0, 0, Settings.BLOCK, YesNo.NO);
        this.addToLeftToolbar(this.blockMode);

        this.interfaceMode = new ToggleButton(0, 0, 84, 85,
                GuiText.InterfaceTerminal.getLocal(), GuiText.InterfaceTerminalHint.getLocal(),
                btn -> selectNextInterfaceMode());
        this.addToLeftToolbar(this.interfaceMode);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.blockMode.set(this.container.getBlockingMode());
        this.interfaceMode.setState(this.container.getInterfaceTerminalMode() == YesNo.YES);
    }

    private void openPriorityGui() {
        NetworkHandler.instance().sendToServer(new SwitchGuisPacket(PriorityContainer.TYPE));
    }

    private void selectNextInterfaceMode() {
        final boolean backwards = isHandlingRightClick();
        NetworkHandler.instance().sendToServer(new ConfigButtonPacket(Settings.INTERFACE_TERMINAL, backwards));
    }

}
