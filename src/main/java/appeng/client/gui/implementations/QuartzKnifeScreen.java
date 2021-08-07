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

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.vertex.PoseStack;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.ActionKey;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.core.AppEngClient;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.menu.implementations.QuartzKnifeMenu;

public class QuartzKnifeScreen extends AEBaseScreen<QuartzKnifeMenu> {

    private EditBox name;

    public QuartzKnifeScreen(QuartzKnifeMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Override
    public void init() {
        super.init();

        this.name = new EditBox(this.font, this.leftPos + 24, this.topPos + 32, 79, this.font.lineHeight,
                TextComponent.EMPTY);
        this.name.setBordered(false);
        this.name.setMaxLength(32);
        this.name.setTextColor(0xFFFFFF);
        this.name.setVisible(true);
        this.name.setFocus(true);
    }

    @Override
    public void drawBG(PoseStack poseStack, final int offsetX, final int offsetY, final int mouseX, final int mouseY,
            float partialTicks) {
        super.drawBG(poseStack, offsetX, offsetY, mouseX, mouseY, partialTicks);
        this.name.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean charTyped(char character, int key) {
        if (this.name.isFocused() && this.name.charTyped(character, key)) {
            final String Out = this.name.getValue();
            menu.setName(Out);
            NetworkHandler.instance().sendToServer(new ConfigValuePacket("QuartzKnife.Name", Out));
            return true;
        }

        return super.charTyped(character, key);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {

        Key input = InputConstants.getKey(keyCode, scanCode);

        if (keyCode != GLFW.GLFW_KEY_ESCAPE && !this.checkHotbarKeys(input)) {
            if (AppEngClient.instance().isActionKey(ActionKey.TOGGLE_FOCUS, input)) {
                this.name.setFocus(!this.name.isFocused());
                return true;
            }

            if (this.name.isFocused()) {
                if (keyCode == GLFW.GLFW_KEY_ENTER) {
                    this.name.setFocus(false);
                    return true;
                }

                if (this.name.keyPressed(keyCode, scanCode, p_keyPressed_3_)) {
                    final String Out = this.name.getValue();
                    menu.setName(Out);
                    NetworkHandler.instance().sendToServer(new ConfigValuePacket("QuartzKnife.Name", Out));
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
