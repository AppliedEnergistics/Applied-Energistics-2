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

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import appeng.api.config.FuzzyMode;
import appeng.api.config.LevelType;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.client.gui.widgets.NumberBox;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.container.implementations.LevelEmitterContainer;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;

public class LevelEmitterScreen extends UpgradeableScreen<LevelEmitterContainer> {

    private NumberBox level;

    private ButtonWidget plus1;
    private ButtonWidget plus10;
    private ButtonWidget plus100;
    private ButtonWidget plus1000;
    private ButtonWidget minus1;
    private ButtonWidget minus10;
    private ButtonWidget minus100;
    private ButtonWidget minus1000;

    private SettingToggleButton<LevelType> levelMode;
    private SettingToggleButton<YesNo> craftingMode;

    public LevelEmitterScreen(LevelEmitterContainer container, PlayerInventory playerInventory, Text title) {
        super(container, playerInventory, title);
    }

    @Override
    public void init() {
        super.init();

        this.level = new NumberBox(this.textRenderer, this.x + 24, this.y + 43, 79, this.textRenderer.fontHeight,
                Long.class);
        this.level.setHasBorder(false);
        this.level.setMaxLength(16);
        this.level.setEditableColor(0xFFFFFF);
        this.level.setVisible(true);
        this.level.setFocused(true);
        handler.setTextField(this.level);
    }

    @Override
    protected void addButtons() {
        this.levelMode = new ServerSettingToggleButton<>(this.x - 18, this.y + 8, Settings.LEVEL_TYPE,
                LevelType.ITEM_LEVEL);
        this.redstoneMode = new ServerSettingToggleButton<>(this.x - 18, this.y + 28,
                Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL);
        this.fuzzyMode = new ServerSettingToggleButton<>(this.x - 18, this.y + 48, Settings.FUZZY_MODE,
                FuzzyMode.IGNORE_ALL);
        this.craftingMode = new ServerSettingToggleButton<>(this.x - 18, this.y + 48,
                Settings.CRAFT_VIA_REDSTONE, YesNo.NO);

        final int a = AEConfig.instance().levelByStackAmounts(0);
        final int b = AEConfig.instance().levelByStackAmounts(1);
        final int c = AEConfig.instance().levelByStackAmounts(2);
        final int d = AEConfig.instance().levelByStackAmounts(3);

        this.addButton(this.plus1 = new ButtonWidget(this.x + 20, this.y + 17, 22, 20, new LiteralText("+" + a), btn -> addQty(a)));
        this.addButton(
                this.plus10 = new ButtonWidget(this.x + 48, this.y + 17, 28, 20, new LiteralText("+" + b), btn -> addQty(b)));
        this.addButton(
                this.plus100 = new ButtonWidget(this.x + 82, this.y + 17, 32, 20, new LiteralText("+" + c), btn -> addQty(c)));
        this.addButton(
                this.plus1000 = new ButtonWidget(this.x + 120, this.y + 17, 38, 20, new LiteralText("+" + d), btn -> addQty(d)));

        this.addButton(
                this.minus1 = new ButtonWidget(this.x + 20, this.y + 59, 22, 20, new LiteralText("-" + a), btn -> addQty(-a)));
        this.addButton(
                this.minus10 = new ButtonWidget(this.x + 48, this.y + 59, 28, 20, new LiteralText("-" + b), btn -> addQty(-b)));
        this.addButton(
                this.minus100 = new ButtonWidget(this.x + 82, this.y + 59, 32, 20, new LiteralText("-" + c), btn -> addQty(-c)));
        this.addButton(
                this.minus1000 = new ButtonWidget(this.x + 120, this.y + 59, 38, 20, new LiteralText("-" + d), btn -> addQty(-d)));

        this.addButton(this.levelMode);
        this.addButton(this.redstoneMode);
        this.addButton(this.craftingMode);
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        final boolean notCraftingMode = this.bc.getInstalledUpgrades(Upgrades.CRAFTING) == 0;

        // configure enabled status...
        this.level.active = notCraftingMode;
        this.plus1.active = notCraftingMode;
        this.plus10.active = notCraftingMode;
        this.plus100.active = notCraftingMode;
        this.plus1000.active = notCraftingMode;
        this.minus1.active = notCraftingMode;
        this.minus10.active = notCraftingMode;
        this.minus100.active = notCraftingMode;
        this.minus1000.active = notCraftingMode;
        this.levelMode.active = notCraftingMode;
        this.redstoneMode.active = notCraftingMode;

        super.drawFG(matrices, offsetX, offsetY, mouseX, mouseY);

        if (this.craftingMode != null) {
            this.craftingMode.set(this.cvb.getCraftingMode());
        }

        if (this.levelMode != null) {
            this.levelMode.set(((LevelEmitterContainer) this.cvb).getLevelMode());
        }
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks) {
        super.drawBG(matrices, offsetX, offsetY, mouseX, mouseY, partialTicks);
        this.level.render(matrices, mouseX, mouseY, partialTicks);
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

    private void addQty(final long i) {
        try {
            String Out = this.level.getText();

            boolean Fixed = false;
            while (Out.startsWith("0") && Out.length() > 1) {
                Out = Out.substring(1);
                Fixed = true;
            }

            if (Fixed) {
                this.level.setText(Out);
            }

            if (Out.isEmpty()) {
                Out = "0";
            }

            long result = Long.parseLong(Out);
            result += i;
            if (result < 0) {
                result = 0;
            }

            this.level.setText(Out = Long.toString(result));

            NetworkHandler.instance().sendToServer(new ConfigValuePacket("LevelEmitter.Value", Out));
        } catch (final NumberFormatException e) {
            // nope..
            this.level.setText("0");
        }
    }

    @Override
    public boolean charTyped(char character, int key) {
        // Forward entered characters to the number-text-field
        return level.charTyped(character, key);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {
        if (!this.checkHotbarKeys(keyCode, scanCode)) {
            if (keyCode == 211 || keyCode == 205 || keyCode == 203 || keyCode == 14) {
                String Out = this.level.getText();

                boolean Fixed = false;
                while (Out.startsWith("0") && Out.length() > 1) {
                    Out = Out.substring(1);
                    Fixed = true;
                }

                if (Fixed) {
                    this.level.setText(Out);
                }

                if (Out.isEmpty()) {
                    Out = "0";
                }

                NetworkHandler.instance().sendToServer(new ConfigValuePacket("LevelEmitter.Value", Out));
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
    }
}
