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

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.api.config.ActionItems;
import appeng.api.config.CopyMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.container.implementations.CellWorkbenchContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;

public class CellWorkbenchScreen extends UpgradeableScreen<CellWorkbenchContainer> {

    private ToggleButton copyMode;

    public CellWorkbenchScreen(CellWorkbenchContainer container, PlayerInventory playerInventory,
            Text title) {
        super(container, playerInventory, title);
        this.backgroundHeight = 251;
    }

    @Override
    protected void addButtons() {
        this.fuzzyMode = this.addButton(new SettingToggleButton<>(this.x - 18, this.y + 68,
                Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL, this::toggleFuzzyMode));
        this.addButton(
                new ActionButton(this.x - 18, this.y + 28, ActionItems.WRENCH, act1 -> action("Partition")));
        this.addButton(new ActionButton(this.x - 18, this.y + 8, ActionItems.CLOSE, act -> action("Clear")));
        this.copyMode = this.addButton(new ToggleButton(this.x - 18, this.y + 48, 11 * 16 + 5, 12 * 16 + 5,
                GuiText.CopyMode.text(), GuiText.CopyModeDesc.text(), act -> action("CopyMode")));
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks) {
        this.handleButtonVisibility();

        this.bindTexture(this.getBackground());
        drawTexture(matrices, offsetX, offsetY, 0, 0, 211 - 34, this.backgroundHeight);
        if (this.drawUpgrades()) {
            if (this.handler.availableUpgrades() <= 8) {
                drawTexture(matrices, offsetX + 177, offsetY, 177, 0, 35,
                        7 + this.handler.availableUpgrades() * 18);
                drawTexture(matrices, offsetX + 177, offsetY + (7 + (this.handler.availableUpgrades()) * 18),
                        177, 151, 35, 7);
            } else if (this.handler.availableUpgrades() <= 16) {
                drawTexture(matrices, offsetX + 177, offsetY, 177, 0, 35, 7 + 8 * 18);
                drawTexture(matrices, offsetX + 177, offsetY + (7 + (8) * 18), 177, 151, 35, 7);

                final int dx = this.handler.availableUpgrades() - 8;
                drawTexture(matrices, offsetX + 177 + 27, offsetY, 186, 0, 35 - 8, 7 + dx * 18);
                if (dx == 8) {
                    drawTexture(matrices, offsetX + 177 + 27, offsetY + (7 + (dx) * 18), 186, 151, 35 - 8, 7);
                } else {
                    drawTexture(matrices, offsetX + 177 + 27 + 4, offsetY + (7 + (dx) * 18), 186 + 4, 151,
                            35 - 8, 7);
                }
            } else {
                drawTexture(matrices, offsetX + 177, offsetY, 177, 0, 35, 7 + 8 * 18);
                drawTexture(matrices, offsetX + 177, offsetY + (7 + (8) * 18), 177, 151, 35, 7);

                drawTexture(matrices, offsetX + 177 + 27, offsetY, 186, 0, 35 - 8, 7 + 8 * 18);
                drawTexture(matrices, offsetX + 177 + 27, offsetY + (7 + (8) * 18), 186, 151, 35 - 8, 7);

                final int dx = this.handler.availableUpgrades() - 16;
                drawTexture(matrices, offsetX + 177 + 27 + 18, offsetY, 186, 0, 35 - 8, 7 + dx * 18);
                if (dx == 8) {
                    drawTexture(matrices, offsetX + 177 + 27 + 18, offsetY + (7 + (dx) * 18), 186, 151, 35 - 8,
                            7);
                } else {
                    drawTexture(matrices, offsetX + 177 + 27 + 18 + 4, offsetY + (7 + (dx) * 18), 186 + 4, 151,
                            35 - 8, 7);
                }
            }
        }
        if (this.hasToolbox()) {
            drawTexture(matrices, offsetX + 178, offsetY + this.backgroundHeight - 90, 178, 161, 68, 68);
        }
    }

    @Override
    protected void handleButtonVisibility() {
        this.copyMode.setState(this.handler.getCopyMode() == CopyMode.CLEAR_ON_REMOVE);

        boolean hasFuzzy = false;
        final FixedItemInv inv = this.handler.getCellUpgradeInventory();
        for (int x = 0; x < inv.getSlotCount(); x++) {
            final ItemStack is = inv.getInvStack(x);
            if (!is.isEmpty() && is.getItem() instanceof IUpgradeModule) {
                if (((IUpgradeModule) is.getItem()).getType(is) == Upgrades.FUZZY) {
                    hasFuzzy = true;
                }
            }
        }
        this.fuzzyMode.setVisibility(hasFuzzy);
    }

    @Override
    protected String getBackground() {
        return "guis/cellworkbench.png";
    }

    @Override
    protected boolean drawUpgrades() {
        return this.handler.availableUpgrades() > 0;
    }

    @Override
    protected GuiText getName() {
        return GuiText.CellWorkbench;
    }

    private void action(String type) {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("CellWorkbench.Action", type));
    }

    private void toggleFuzzyMode(SettingToggleButton<FuzzyMode> button, boolean backwards) {
        FuzzyMode fz = button.getNextValue(backwards);
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("CellWorkbench.Fuzzy", fz.name()));
    }

}
