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

import appeng.api.config.FuzzyMode;
import appeng.api.config.LevelType;
import appeng.api.config.PowerUnits;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.theme.ThemeColor;
import appeng.container.implementations.LevelEmitterContainer;
import appeng.core.localization.GuiText;

public class LevelEmitterScreen extends UpgradeableScreen<LevelEmitterContainer> {

    private NumberEntryWidget level;
    private SettingToggleButton<LevelType> levelMode;
    private SettingToggleButton<YesNo> craftingMode;

    public LevelEmitterScreen(LevelEmitterContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
    }

    @Override
    public void init() {
        super.init();

        this.level = new NumberEntryWidget(this, 20, 17, 138, 62, NumberEntryType.LEVEL_ITEM_COUNT);
        this.level.setTextFieldBounds(25, 44, 75);
        this.level.addButtons(children::add, this::addButton);
        this.level.setValue(container.getReportingValue());
        this.level.setOnChange(this::saveReportingValue);
        this.level.setOnConfirm(this::closeScreen);

        this.changeFocus(true);
    }

    private void saveReportingValue() {
        this.level.getLongValue().ifPresent(container::setReportingValue);
    }

    @Override
    protected void addButtons() {
        this.levelMode = new ServerSettingToggleButton<>(this.guiLeft - 18, this.guiTop + 8, Settings.LEVEL_TYPE,
                LevelType.ITEM_LEVEL);
        this.redstoneMode = new ServerSettingToggleButton<>(this.guiLeft - 18, this.guiTop + 28,
                Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL);
        this.fuzzyMode = new ServerSettingToggleButton<>(this.guiLeft - 18, this.guiTop + 48, Settings.FUZZY_MODE,
                FuzzyMode.IGNORE_ALL);
        this.craftingMode = new ServerSettingToggleButton<>(this.guiLeft - 18, this.guiTop + 48,
                Settings.CRAFT_VIA_REDSTONE, YesNo.NO);
        this.addButton(this.levelMode);
        this.addButton(this.redstoneMode);
        this.addButton(this.craftingMode);
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        final boolean notCraftingMode = this.bc.getInstalledUpgrades(Upgrades.CRAFTING) == 0;

        // configure enabled status...
        this.level.setActive(notCraftingMode);
        this.levelMode.active = notCraftingMode;
        this.redstoneMode.active = notCraftingMode;

        super.drawFG(matrixStack, offsetX, offsetY, mouseX, mouseY);

        if (this.craftingMode != null) {
            this.craftingMode.set(this.cvb.getCraftingMode());
        }

        if (this.levelMode != null) {
            LevelType currentLevelMode = ((LevelEmitterContainer) this.cvb).getLevelMode();
            this.levelMode.set(currentLevelMode);

            if (notCraftingMode) {
                if (currentLevelMode == LevelType.ENERGY_LEVEL) {
                    this.font.drawString(matrixStack, PowerUnits.AE.textComponent().getString(), 110, 44,
                            ThemeColor.TEXT_HEADING.argb());
                }
            }
        }
    }

    @Override
    public void drawBG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        super.drawBG(matrixStack, offsetX, offsetY, mouseX, mouseY, partialTicks);
        this.level.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void handleButtonVisibility() {
        this.craftingMode.setVisibility(this.bc.getInstalledUpgrades(Upgrades.CRAFTING) > 0);
        this.fuzzyMode.setVisibility(this.bc.getInstalledUpgrades(Upgrades.FUZZY) > 0);
    }

    @Override
    protected String getBackground() {
        return "guis/lvlemitter.png";
    }

    @Override
    protected GuiText getName() {
        return GuiText.LevelEmitter;
    }

}
