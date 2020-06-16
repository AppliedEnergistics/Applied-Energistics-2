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
import net.minecraftforge.fml.client.gui.GuiUtils;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.implementations.IUpgradeableHost;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiServerSettingToggleButton;
import appeng.client.gui.widgets.GuiSettingToggleButton;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.core.localization.GuiText;
import appeng.parts.automation.PartExportBus;
import appeng.parts.automation.PartImportBus;

public class GuiUpgradeable<T extends ContainerUpgradeable> extends AEBaseGui<T> {

    protected final ContainerUpgradeable cvb;
    protected final IUpgradeableHost bc;

    protected GuiSettingToggleButton<RedstoneMode> redstoneMode;
    protected GuiSettingToggleButton<FuzzyMode> fuzzyMode;
    protected GuiSettingToggleButton<YesNo> craftMode;
    protected GuiSettingToggleButton<SchedulingMode> schedulingMode;

    public GuiUpgradeable(T container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.cvb = container;

        this.bc = (IUpgradeableHost) container.getTarget();
        this.xSize = this.hasToolbox() ? 246 : 211;
        this.ySize = 184;
    }

    protected boolean hasToolbox() {
        return (this.container).hasToolbox();
    }

    @Override
    public void init() {
        super.init();
        this.addButtons();
    }

    protected void addButtons() {
        this.redstoneMode = new GuiServerSettingToggleButton<>(this.guiLeft - 18, this.guiTop + 8,
                Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        addButton(this.redstoneMode);
        this.fuzzyMode = new GuiServerSettingToggleButton<>(this.guiLeft - 18, this.guiTop + 28, Settings.FUZZY_MODE,
                FuzzyMode.IGNORE_ALL);
        addButton(this.fuzzyMode);
        this.craftMode = new GuiServerSettingToggleButton<>(this.guiLeft - 18, this.guiTop + 48, Settings.CRAFT_ONLY,
                YesNo.NO);
        addButton(this.craftMode);
        this.schedulingMode = new GuiServerSettingToggleButton<>(this.guiLeft - 18, this.guiTop + 68,
                Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT);
        addButton(this.schedulingMode);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.font.drawString(this.getGuiDisplayName(this.getName().getLocal()), 8, 6, 4210752);
        this.font.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);

        if (this.redstoneMode != null) {
            this.redstoneMode.set(this.cvb.getRedStoneMode());
        }

        if (this.fuzzyMode != null) {
            this.fuzzyMode.set(this.cvb.getFuzzyMode());
        }

        if (this.craftMode != null) {
            this.craftMode.set(this.cvb.getCraftingMode());
        }

        if (this.schedulingMode != null) {
            this.schedulingMode.set(this.cvb.getSchedulingMode());
        }
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks) {
        this.handleButtonVisibility();

        this.bindTexture(this.getBackground());
        GuiUtils.drawTexturedModalRect(offsetX, offsetY, 0, 0, 211 - 34, this.ySize, getBlitOffset());
        if (this.drawUpgrades()) {
            GuiUtils.drawTexturedModalRect(offsetX + 177, offsetY, 177, 0, 35, 14 + this.cvb.availableUpgrades() * 18,
                    getBlitOffset());
        }
        if (this.hasToolbox()) {
            GuiUtils.drawTexturedModalRect(offsetX + 178, offsetY + this.ySize - 90, 178, this.ySize - 90, 68, 68,
                    getBlitOffset());
        }
    }

    protected void handleButtonVisibility() {
        if (this.redstoneMode != null) {
            this.redstoneMode.setVisibility(this.bc.getInstalledUpgrades(Upgrades.REDSTONE) > 0);
        }
        if (this.fuzzyMode != null) {
            this.fuzzyMode.setVisibility(this.bc.getInstalledUpgrades(Upgrades.FUZZY) > 0);
        }
        if (this.craftMode != null) {
            this.craftMode.setVisibility(this.bc.getInstalledUpgrades(Upgrades.CRAFTING) > 0);
        }
        if (this.schedulingMode != null) {
            this.schedulingMode.setVisibility(
                    this.bc.getInstalledUpgrades(Upgrades.CAPACITY) > 0 && this.bc instanceof PartExportBus);
        }
    }

    protected String getBackground() {
        return "guis/bus.png";
    }

    protected boolean drawUpgrades() {
        return true;
    }

    protected GuiText getName() {
        return this.bc instanceof PartImportBus ? GuiText.ImportBus : GuiText.ExportBus;
    }

}
