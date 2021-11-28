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
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.storage.data.AEFluidKey;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.menu.implementations.StorageLevelEmitterMenu;

public class StorageLevelEmitterScreen extends UpgradeableScreen<StorageLevelEmitterMenu> {

    private final SettingToggleButton<YesNo> craftingMode;
    private final SettingToggleButton<RedstoneMode> redstoneMode;
    private final SettingToggleButton<FuzzyMode> fuzzyMode;
    private final NumberEntryWidget level;

    public StorageLevelEmitterScreen(StorageLevelEmitterMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.redstoneMode = new ServerSettingToggleButton<>(Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL);
        this.fuzzyMode = new ServerSettingToggleButton<>(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.craftingMode = new ServerSettingToggleButton<>(Settings.CRAFT_VIA_REDSTONE, YesNo.NO);
        this.addToLeftToolbar(this.redstoneMode);
        this.addToLeftToolbar(this.craftingMode);
        this.addToLeftToolbar(this.fuzzyMode);

        this.level = new NumberEntryWidget(
                AEFluidKey.is(menu.getConfiguredFilter()) ? NumberEntryType.LEVEL_FLUID_VOLUME
                        : NumberEntryType.LEVEL_ITEM_COUNT);
        this.level.setTextFieldBounds(25, 44, 75);
        setInputValueFromHost();
        this.level.setOnChange(this::saveReportingValue);
        this.level.setOnConfirm(this::onClose);
        widgets.add("level", this.level);
    }

    private void setInputValueFromHost() {
        var value = menu.getReportingValue();
        if (AEFluidKey.is(menu.getConfiguredFilter())) {
            value = value * 1000 / AEFluidKey.AMOUNT_BUCKET;
        }
        this.level.setValue(value);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        if (AEFluidKey.is(menu.getConfiguredFilter())) {
            // Update when changing types
            if (this.level.getType() != NumberEntryType.LEVEL_FLUID_VOLUME) {
                menu.setReportingValue(menu.getReportingValue() * AEFluidKey.AMOUNT_BUCKET / 1000);
                setInputValueFromHost();
            }
            this.level.setType(NumberEntryType.LEVEL_FLUID_VOLUME);
        } else {
            // Update when changing types
            if (this.level.getType() != NumberEntryType.LEVEL_ITEM_COUNT) {
                menu.setReportingValue(menu.getReportingValue() * 1000 / AEFluidKey.AMOUNT_BUCKET);
                setInputValueFromHost();
            }

            this.level.setType(NumberEntryType.LEVEL_ITEM_COUNT);
        }

        this.fuzzyMode.set(menu.getFuzzyMode());
        this.fuzzyMode.setVisibility(menu.supportsFuzzySearch());

        // configure enabled status...
        final boolean notCraftingMode = !menu.hasUpgrade(Upgrades.CRAFTING);
        this.level.setActive(notCraftingMode);

        this.redstoneMode.active = notCraftingMode;
        this.redstoneMode.set(menu.getRedStoneMode());

        this.craftingMode.set(this.menu.getCraftingMode());
        this.craftingMode.setVisibility(!notCraftingMode);
    }

    @Override
    public void drawBG(PoseStack poseStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        super.drawBG(poseStack, offsetX, offsetY, mouseX, mouseY, partialTicks);
        this.level.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private void saveReportingValue() {
        this.level.getLongValue().ifPresent(value -> {
            // Convert to the real reporting value
            if (AEFluidKey.is(menu.getConfiguredFilter())) {
                value = value * AEFluidKey.AMOUNT_BUCKET / 1000;
            }
            menu.setReportingValue(value);
        });
    }
}
