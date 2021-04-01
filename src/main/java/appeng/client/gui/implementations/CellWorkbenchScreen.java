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
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

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
            ITextComponent title) {
        super(container, playerInventory, title);
        this.ySize = 251;
    }

    @Override
    protected void addButtons() {
        this.fuzzyMode = this.addButton(new SettingToggleButton<>(this.guiLeft - 18, this.guiTop + 68,
                Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL, this::toggleFuzzyMode));
        this.addButton(
                new ActionButton(this.guiLeft - 18, this.guiTop + 28, ActionItems.WRENCH, act1 -> action("Partition")));
        this.addButton(new ActionButton(this.guiLeft - 18, this.guiTop + 8, ActionItems.CLOSE, act -> action("Clear")));
        this.copyMode = this.addButton(new ToggleButton(this.guiLeft - 18, this.guiTop + 48, 11 * 16 + 5, 12 * 16 + 5,
                GuiText.CopyMode.getLocal(), GuiText.CopyModeDesc.getLocal(), act -> action("CopyMode")));
    }

    @Override
    public void drawBG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        this.handleButtonVisibility();

        this.bindTexture(this.getBackground());
        blit(matrixStack, offsetX, offsetY, 0, 0, 211 - 34, this.ySize);
        if (this.drawUpgrades()) {
            if (this.container.availableUpgrades() <= 8) {
                blit(matrixStack, offsetX + 177, offsetY, 177, 0, 35, 7 + this.container.availableUpgrades() * 18);
                blit(matrixStack, offsetX + 177, offsetY + (7 + (this.container.availableUpgrades()) * 18), 177, 151,
                        35, 7);
            } else if (this.container.availableUpgrades() <= 16) {
                blit(matrixStack, offsetX + 177, offsetY, 177, 0, 35, 7 + 8 * 18);
                blit(matrixStack, offsetX + 177, offsetY + (7 + (8) * 18), 177, 151, 35, 7);

                final int dx = this.container.availableUpgrades() - 8;
                blit(matrixStack, offsetX + 177 + 27, offsetY, 186, 0, 35 - 8, 7 + dx * 18);
                if (dx == 8) {
                    blit(matrixStack, offsetX + 177 + 27, offsetY + (7 + (dx) * 18), 186, 151, 35 - 8, 7);
                } else {
                    blit(matrixStack, offsetX + 177 + 27 + 4, offsetY + (7 + (dx) * 18), 186 + 4, 151, 35 - 8, 7);
                }
            } else {
                blit(matrixStack, offsetX + 177, offsetY, 177, 0, 35, 7 + 8 * 18);
                blit(matrixStack, offsetX + 177, offsetY + (7 + (8) * 18), 177, 151, 35, 7);

                blit(matrixStack, offsetX + 177 + 27, offsetY, 186, 0, 35 - 8, 7 + 8 * 18);
                blit(matrixStack, offsetX + 177 + 27, offsetY + (7 + (8) * 18), 186, 151, 35 - 8, 7);

                final int dx = this.container.availableUpgrades() - 16;
                blit(matrixStack, offsetX + 177 + 27 + 18, offsetY, 186, 0, 35 - 8, 7 + dx * 18);
                if (dx == 8) {
                    blit(matrixStack, offsetX + 177 + 27 + 18, offsetY + (7 + (dx) * 18), 186, 151, 35 - 8, 7);
                } else {
                    blit(matrixStack, offsetX + 177 + 27 + 18 + 4, offsetY + (7 + (dx) * 18), 186 + 4, 151, 35 - 8,
                            7);
                }
            }
        }
        if (this.hasToolbox()) {
            blit(matrixStack, offsetX + 178, offsetY + this.ySize - 90, 178, 161, 68, 68);
        }
    }

    @Override
    protected void handleButtonVisibility() {
        this.copyMode.setState(this.container.getCopyMode() == CopyMode.CLEAR_ON_REMOVE);

        boolean hasFuzzy = false;
        final FixedItemInv inv = this.container.getCellUpgradeInventory();
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
        return this.container.availableUpgrades() > 0;
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
