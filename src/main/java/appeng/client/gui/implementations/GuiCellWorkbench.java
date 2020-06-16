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
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.ActionItems;
import appeng.api.config.CopyMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.client.gui.widgets.GuiActionButton;
import appeng.client.gui.widgets.GuiSettingToggleButton;
import appeng.client.gui.widgets.GuiToggleButton;
import appeng.container.implementations.ContainerCellWorkbench;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;

public class GuiCellWorkbench extends GuiUpgradeable<ContainerCellWorkbench> {

    private GuiToggleButton copyMode;

    public GuiCellWorkbench(ContainerCellWorkbench container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.ySize = 251;
    }

    @Override
    protected void addButtons() {
        this.fuzzyMode = this.addButton(new GuiSettingToggleButton<>(this.guiLeft - 18, this.guiTop + 68,
                Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL, this::toggleFuzzyMode));
        this.addButton(new GuiActionButton(this.guiLeft - 18, this.guiTop + 28, ActionItems.WRENCH,
                act1 -> action("Partition")));
        this.addButton(
                new GuiActionButton(this.guiLeft - 18, this.guiTop + 8, ActionItems.CLOSE, act -> action("Clear")));
        this.copyMode = this.addButton(new GuiToggleButton(this.guiLeft - 18, this.guiTop + 48, 11 * 16 + 5,
                12 * 16 + 5, GuiText.CopyMode.getLocal(), GuiText.CopyModeDesc.getLocal(), act -> action("CopyMode")));
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks) {
        this.handleButtonVisibility();

        this.bindTexture(this.getBackground());
        GuiUtils.drawTexturedModalRect(offsetX, offsetY, 0, 0, 211 - 34, this.ySize, getBlitOffset());
        if (this.drawUpgrades()) {
            if (this.container.availableUpgrades() <= 8) {
                GuiUtils.drawTexturedModalRect(offsetX + 177, offsetY, 177, 0, 35,
                        7 + this.container.availableUpgrades() * 18, getBlitOffset());
                GuiUtils.drawTexturedModalRect(offsetX + 177, offsetY + (7 + (this.container.availableUpgrades()) * 18),
                        177, 151, 35, 7, getBlitOffset());
            } else if (this.container.availableUpgrades() <= 16) {
                GuiUtils.drawTexturedModalRect(offsetX + 177, offsetY, 177, 0, 35, 7 + 8 * 18, getBlitOffset());
                GuiUtils.drawTexturedModalRect(offsetX + 177, offsetY + (7 + (8) * 18), 177, 151, 35, 7,
                        getBlitOffset());

                final int dx = this.container.availableUpgrades() - 8;
                GuiUtils.drawTexturedModalRect(offsetX + 177 + 27, offsetY, 186, 0, 35 - 8, 7 + dx * 18,
                        getBlitOffset());
                if (dx == 8) {
                    GuiUtils.drawTexturedModalRect(offsetX + 177 + 27, offsetY + (7 + (dx) * 18), 186, 151, 35 - 8, 7,
                            getBlitOffset());
                } else {
                    GuiUtils.drawTexturedModalRect(offsetX + 177 + 27 + 4, offsetY + (7 + (dx) * 18), 186 + 4, 151,
                            35 - 8, 7, getBlitOffset());
                }
            } else {
                GuiUtils.drawTexturedModalRect(offsetX + 177, offsetY, 177, 0, 35, 7 + 8 * 18, getBlitOffset());
                GuiUtils.drawTexturedModalRect(offsetX + 177, offsetY + (7 + (8) * 18), 177, 151, 35, 7,
                        getBlitOffset());

                GuiUtils.drawTexturedModalRect(offsetX + 177 + 27, offsetY, 186, 0, 35 - 8, 7 + 8 * 18,
                        getBlitOffset());
                GuiUtils.drawTexturedModalRect(offsetX + 177 + 27, offsetY + (7 + (8) * 18), 186, 151, 35 - 8, 7,
                        getBlitOffset());

                final int dx = this.container.availableUpgrades() - 16;
                GuiUtils.drawTexturedModalRect(offsetX + 177 + 27 + 18, offsetY, 186, 0, 35 - 8, 7 + dx * 18,
                        getBlitOffset());
                if (dx == 8) {
                    GuiUtils.drawTexturedModalRect(offsetX + 177 + 27 + 18, offsetY + (7 + (dx) * 18), 186, 151, 35 - 8,
                            7, getBlitOffset());
                } else {
                    GuiUtils.drawTexturedModalRect(offsetX + 177 + 27 + 18 + 4, offsetY + (7 + (dx) * 18), 186 + 4, 151,
                            35 - 8, 7, getBlitOffset());
                }
            }
        }
        if (this.hasToolbox()) {
            GuiUtils.drawTexturedModalRect(offsetX + 178, offsetY + this.ySize - 90, 178, 161, 68, 68, getBlitOffset());
        }
    }

    @Override
    protected void handleButtonVisibility() {
        this.copyMode.setState(this.container.getCopyMode() == CopyMode.CLEAR_ON_REMOVE);

        boolean hasFuzzy = false;
        final IItemHandler inv = this.container.getCellUpgradeInventory();
        for (int x = 0; x < inv.getSlots(); x++) {
            final ItemStack is = inv.getStackInSlot(x);
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
        NetworkHandler.instance().sendToServer(new PacketValueConfig("CellWorkbench.Action", type));
    }

    private void toggleFuzzyMode(GuiSettingToggleButton<FuzzyMode> button, boolean backwards) {
        FuzzyMode fz = button.getNextValue(backwards);
        NetworkHandler.instance().sendToServer(new PacketValueConfig("CellWorkbench.Fuzzy", fz.name()));
    }

}
