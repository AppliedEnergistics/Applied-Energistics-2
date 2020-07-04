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

import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;


import appeng.client.ActionKey;
import appeng.client.gui.AEBaseScreen;
import appeng.container.implementations.QuartzKnifeContainer;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;

public class QuartzKnifeScreen extends AEBaseScreen<QuartzKnifeContainer> {

    private TextFieldWidget name;

    public QuartzKnifeScreen(QuartzKnifeContainer container, PlayerInventory playerInventory, Text title) {
        super(container, playerInventory, title);
        this.backgroundHeight = 184;
    }

    @Override
    public void init() {
        super.init();

        this.name = new TextFieldWidget(this.textRenderer, this.x + 24, this.y + 32, 79, this.textRenderer.FONT_HEIGHT, "");
        this.name.setHasBorder(false);
        this.name.setMaxLength(32);
        this.name.setEditableColor(0xFFFFFF);
        this.name.setVisible(true);
        this.name.setFocused(true);
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.textRenderer.draw(matrices, this.getGuiDisplayName(GuiText.QuartzCuttingKnife.getLocal()), 8, 6, 4210752);
        this.textRenderer.draw(matrices, GuiText.inventory.getLocal(), 8, this.backgroundHeight - 96 + 3, 4210752);
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks) {
        this.bindTexture("guis/quartzknife.png");
        drawTexture(matrices, offsetX, offsetY, 0, 0, this.backgroundWidth, this.backgroundHeight);
        this.name.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean charTyped(char character, int key) {
        if (this.name.isFocused() && this.name.charTyped(character, key)) {
            final String Out = this.name.getText();
            container.setName(Out);
            NetworkHandler.instance().sendToServer(new ConfigValuePacket("QuartzKnife.Name", Out));
            return true;
        }

        return super.charTyped(character, key);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {

        if (keyCode != GLFW.GLFW_KEY_ESCAPE && !this.checkHotbarKeys(keyCode, scanCode)) {
            if (AppEng.instance().isActionKey(ActionKey.TOGGLE_FOCUS, keyCode, scanCode)) {
                this.name.setFocused(!this.name.isFocused());
                return true;
            }

            if (this.name.isFocused()) {
                if (keyCode == GLFW.GLFW_KEY_ENTER) {
                    this.name.setFocused(false);
                    return true;
                }

                if (this.name.keyPressed(keyCode, scanCode, p_keyPressed_3_)) {
                    final String Out = this.name.getText();
                    container.setName(Out);
                    NetworkHandler.instance().sendToServer(new ConfigValuePacket("QuartzKnife.Name", Out));
                    return true;
                }

                // We need to swallow key presses if the field is focused because typing 'e'
                // would otherwise close
                // the screen
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
    }
}
