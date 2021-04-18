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
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.client.gui.Blitter;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.container.implementations.LevelEmitterContainer;

public class LevelEmitterScreen extends UpgradeableScreen<LevelEmitterContainer> {

    private static final Blitter BACKGROUND = Blitter.texture("guis/lvlemitter.png")
            .src(0, 0, 176, 184);

    private NumberEntryWidget level;
    private SettingToggleButton<LevelType> levelMode;
    private SettingToggleButton<YesNo> craftingMode;
    private SettingToggleButton<RedstoneMode> redstoneMode;
    private SettingToggleButton<FuzzyMode> fuzzyMode;

    public LevelEmitterScreen(LevelEmitterContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title, BACKGROUND);
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

        this.levelMode = new ServerSettingToggleButton<>(0, 0, Settings.LEVEL_TYPE,
                LevelType.ITEM_LEVEL);
        this.redstoneMode = new ServerSettingToggleButton<>(0, 0,
                Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL);
        this.fuzzyMode = new ServerSettingToggleButton<>(0, 0, Settings.FUZZY_MODE,
                FuzzyMode.IGNORE_ALL);
        this.craftingMode = new ServerSettingToggleButton<>(0, 0,
                Settings.CRAFT_VIA_REDSTONE, YesNo.NO);
        this.addToLeftToolbar(this.levelMode);
        this.addToLeftToolbar(this.redstoneMode);
        this.addToLeftToolbar(this.craftingMode);
        this.addToLeftToolbar(this.fuzzyMode);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.fuzzyMode.set(container.getFuzzyMode());
        this.fuzzyMode.setVisibility(container.hasUpgrade(Upgrades.FUZZY));

        // configure enabled status...
        final boolean notCraftingMode = !container.hasUpgrade(Upgrades.CRAFTING);
        this.level.setActive(notCraftingMode);

        this.redstoneMode.active = notCraftingMode;
        this.redstoneMode.set(container.getRedStoneMode());

        LevelType currentLevelMode = this.container.getLevelMode();
        this.levelMode.active = notCraftingMode;
        this.levelMode.set(currentLevelMode);

        this.craftingMode.set(this.container.getCraftingMode());
        this.craftingMode.setVisibility(!notCraftingMode);

        setTextHidden("energy_unit", !notCraftingMode || currentLevelMode != LevelType.ENERGY_LEVEL);
    }

    @Override
    public void drawBG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        super.drawBG(matrixStack, offsetX, offsetY, mouseX, mouseY, partialTicks);
        this.level.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private void saveReportingValue() {
        this.level.getLongValue().ifPresent(container::setReportingValue);
    }

}
