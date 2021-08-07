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

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.config.FuzzyMode;
import appeng.api.config.LevelType;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.menu.implementations.ItemLevelEmitterMenu;

public class ItemLevelEmitterScreen extends UpgradeableScreen<ItemLevelEmitterMenu> {

    private final SettingToggleButton<LevelType> levelMode;
    private final SettingToggleButton<YesNo> craftingMode;
    private final SettingToggleButton<RedstoneMode> redstoneMode;
    private final SettingToggleButton<FuzzyMode> fuzzyMode;
    private final NumberEntryWidget level;

    public ItemLevelEmitterScreen(ItemLevelEmitterMenu container, Inventory playerInventory, Component title,
                                  ScreenStyle style) {
        super(container, playerInventory, title, style);

        this.levelMode = new ServerSettingToggleButton<>(Settings.LEVEL_TYPE,
                LevelType.ITEM_LEVEL);
        this.redstoneMode = new ServerSettingToggleButton<>(
                Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL);
        this.fuzzyMode = new ServerSettingToggleButton<>(Settings.FUZZY_MODE,
                FuzzyMode.IGNORE_ALL);
        this.craftingMode = new ServerSettingToggleButton<>(
                Settings.CRAFT_VIA_REDSTONE, YesNo.NO);
        this.addToLeftToolbar(this.levelMode);
        this.addToLeftToolbar(this.redstoneMode);
        this.addToLeftToolbar(this.craftingMode);
        this.addToLeftToolbar(this.fuzzyMode);

        this.level = new NumberEntryWidget(NumberEntryType.LEVEL_ITEM_COUNT);
        this.level.setTextFieldBounds(25, 44, 75);
        this.level.setValue(container.getReportingValue());
        this.level.setOnChange(this::saveReportingValue);
        this.level.setOnConfirm(this::onClose);
        widgets.add("level", this.level);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.fuzzyMode.set(menu.getFuzzyMode());
        this.fuzzyMode.setVisibility(menu.hasUpgrade(Upgrades.FUZZY));

        // configure enabled status...
        final boolean notCraftingMode = !menu.hasUpgrade(Upgrades.CRAFTING);
        this.level.setActive(notCraftingMode);

        this.redstoneMode.active = notCraftingMode;
        this.redstoneMode.set(menu.getRedStoneMode());

        LevelType currentLevelMode = this.menu.getLevelMode();
        this.levelMode.active = notCraftingMode;
        this.levelMode.set(currentLevelMode);

        this.craftingMode.set(this.menu.getCraftingMode());
        this.craftingMode.setVisibility(!notCraftingMode);

        setTextHidden("energy_unit", !notCraftingMode || currentLevelMode != LevelType.ENERGY_LEVEL);
    }

    @Override
    public void drawBG(PoseStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        super.drawBG(matrixStack, offsetX, offsetY, mouseX, mouseY, partialTicks);
        this.level.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private void saveReportingValue() {
        this.level.getLongValue().ifPresent(menu::setReportingValue);
    }

}
