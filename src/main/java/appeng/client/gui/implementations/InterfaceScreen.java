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
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.TabButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.client.theme.ThemeColor;
import appeng.container.implementations.InterfaceContainer;
import appeng.container.implementations.PriorityContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigButtonPacket;
import appeng.core.sync.packets.SwitchGuisPacket;

public class InterfaceScreen extends UpgradeableScreen<InterfaceContainer> {

    private SettingToggleButton<YesNo> blockMode;
    private ToggleButton interfaceMode;

    public InterfaceScreen(InterfaceContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.ySize = 211;
    }

    @Override
    protected void addButtons() {
        this.addButton(new TabButton(this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.text(),
                this.itemRenderer, btn -> openPriorityGui()));

        this.blockMode = new ServerSettingToggleButton<>(this.guiLeft - 18, this.guiTop + 8, Settings.BLOCK, YesNo.NO);
        this.addButton(this.blockMode);

        this.interfaceMode = new ToggleButton(this.guiLeft - 18, this.guiTop + 26, 84, 85,
                GuiText.InterfaceTerminal.getLocal(), GuiText.InterfaceTerminalHint.getLocal(),
                btn -> selectNextInterfaceMode());
        this.addButton(this.interfaceMode);
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        if (this.blockMode != null) {
            this.blockMode.set(((InterfaceContainer) this.cvb).getBlockingMode());
        }

        if (this.interfaceMode != null) {
            this.interfaceMode.setState(((InterfaceContainer) this.cvb).getInterfaceTerminalMode() == YesNo.YES);
        }

        this.font.drawString(matrixStack, this.getGuiDisplayName(GuiText.Interface.text()).getString(), 8, 6,
                ThemeColor.TEXT_TITLE.argb());

        this.font.drawString(matrixStack, GuiText.Config.getLocal(), 8, 6 + 11 + 7, ThemeColor.TEXT_HEADING.argb());
        this.font.drawString(matrixStack, GuiText.StoredItems.getLocal(), 8, 6 + 60 + 7,
                ThemeColor.TEXT_HEADING.argb());
        this.font.drawString(matrixStack, GuiText.Patterns.getLocal(), 8, 6 + 73 + 7, ThemeColor.TEXT_HEADING.argb());

        this.font.drawString(matrixStack, GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3,
                ThemeColor.TEXT_TITLE.argb());
    }

    @Override
    protected String getBackground() {
        return "guis/interface.png";
    }

    private void openPriorityGui() {
        NetworkHandler.instance().sendToServer(new SwitchGuisPacket(PriorityContainer.TYPE));
    }

    private void selectNextInterfaceMode() {
        final boolean backwards = isHandlingRightClick();
        NetworkHandler.instance().sendToServer(new ConfigButtonPacket(Settings.INTERFACE_TERMINAL, backwards));
    }

}
