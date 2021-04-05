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

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.implementations.IUpgradeableHost;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Blitter;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.ToolboxPanel;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.container.implementations.UpgradeableContainer;
import appeng.core.localization.GuiText;
import appeng.parts.automation.ExportBusPart;
import appeng.parts.automation.ImportBusPart;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class UpgradeableScreen<T extends UpgradeableContainer> extends AEBaseScreen<T> {

    // Margin used to position additional elements to the right of the UI
    private static final int MARGIN = 2;

    // Default background for import/export buses
    public static final Blitter BACKGROUND = Blitter.texture("guis/bus.png")
            .src(0, 0, 176, 184);

    private final Blitter background;

    private final UpgradesPanel upgradesPanel;

    private final ToolboxPanel toolboxPanel;

    protected final IUpgradeableHost bc;

    protected SettingToggleButton<RedstoneMode> redstoneMode;
    protected SettingToggleButton<FuzzyMode> fuzzyMode;
    protected SettingToggleButton<YesNo> craftMode;
    protected SettingToggleButton<SchedulingMode> schedulingMode;

    /**
     * Used for simple import-/export buses
     */
    public UpgradeableScreen(T container, PlayerInventory playerInventory, ITextComponent title) {
        this(container, playerInventory, title, BACKGROUND);
    }

    public UpgradeableScreen(T container, PlayerInventory playerInventory, ITextComponent title, Blitter background) {
        super(container, playerInventory, title);
        this.background = background;

        this.bc = (IUpgradeableHost) container.getTarget();

        this.xSize = background.getSrcWidth();
        this.ySize = background.getSrcHeight();

        upgradesPanel = new UpgradesPanel(xSize + MARGIN, 0, container.getUpgradeSlots());
        toolboxPanel = new ToolboxPanel(xSize + MARGIN, ySize - 90, container.getToolboxSlots());
    }

    @Override
    public void init() {
        super.init();
        this.addButtons();
    }

    protected void addButtons() {
        this.redstoneMode = new ServerSettingToggleButton<>(0, 0, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        addToLeftToolbar(this.redstoneMode);
        this.fuzzyMode = new ServerSettingToggleButton<>(0, 0, Settings.FUZZY_MODE,
                FuzzyMode.IGNORE_ALL);
        addToLeftToolbar(this.fuzzyMode);
        this.craftMode = new ServerSettingToggleButton<>(0, 0, Settings.CRAFT_ONLY,
                YesNo.NO);
        addToLeftToolbar(this.craftMode);
        this.schedulingMode = new ServerSettingToggleButton<>(0, 0,
                Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT);
        addToLeftToolbar(this.schedulingMode);
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
                       final int mouseY) {
        this.font.drawString(matrixStack, this.getGuiDisplayName(this.getName().text()).getString(), 8, 6,
                COLOR_DARK_GRAY);
        this.font.drawString(matrixStack, GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, COLOR_DARK_GRAY);

        if (this.redstoneMode != null) {
            this.redstoneMode.set(this.container.getRedStoneMode());
        }

        if (this.fuzzyMode != null) {
            this.fuzzyMode.set(this.container.getFuzzyMode());
        }

        if (this.craftMode != null) {
            this.craftMode.set(this.container.getCraftingMode());
        }

        if (this.schedulingMode != null) {
            this.schedulingMode.set(this.container.getSchedulingMode());
        }
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY,
                       float partialTicks) {
        this.handleButtonVisibility();

        fillRect(matrices, background.getDestRect(), 0xFFFFFFFF);

        background.dest(offsetX, offsetY).blit(matrices, getBlitOffset());

        upgradesPanel.draw(matrices, getBlitOffset(), offsetX, offsetY);

        if (container.hasToolbox()) {
            toolboxPanel.draw(matrices, getBlitOffset(), offsetX, offsetY);
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
                    this.bc.getInstalledUpgrades(Upgrades.CAPACITY) > 0 && this.bc instanceof ExportBusPart);
        }
    }

    protected GuiText getName() {
        return this.bc instanceof ImportBusPart ? GuiText.ImportBus : GuiText.ExportBus;
    }

    @Override
    public List<Rectangle2d> getExclusionZones() {
        List<Rectangle2d> rects = super.getExclusionZones();
        if (container.hasToolbox()) {
            toolboxPanel.addExclusionZones(guiLeft, guiTop, rects);
        }
        if (container.availableUpgrades() > 0) {
            upgradesPanel.addExclusionZones(guiLeft, guiTop, rects);
        }
        return rects;
    }
}
