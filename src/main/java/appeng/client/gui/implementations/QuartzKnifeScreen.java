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

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import appeng.client.ActionKey;
import appeng.client.gui.AEBaseScreen;
import appeng.container.implementations.QuartzKnifeContainer;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;

public class QuartzKnifeScreen extends AEBaseScreen<QuartzKnifeContainer> {

    private TextFieldWidget name;

    public QuartzKnifeScreen(QuartzKnifeContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.imageHeight = 184;
    }

    @Override
    public void init() {
        super.init();

        this.name = new TextFieldWidget(this.font, this.leftPos + 24, this.topPos + 32, 79, this.font.lineHeight,
                StringTextComponent.EMPTY);
        this.name.setBordered(false);
        this.name.setMaxLength(32);
        this.name.setTextColor(0xFFFFFF);
        this.name.setVisible(true);
        this.name.setFocus(true);
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        this.font.draw(matrixStack, this.getGuiDisplayName(GuiText.QuartzCuttingKnife.text()).getString(), 8, 6,
                4210752);
        this.font.draw(matrixStack, GuiText.inventory.getLocal(), 8, this.imageHeight - 96 + 3, 4210752);
    }

    @Override
    public void drawBG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        this.bindTexture("guis/quartzknife.png");
        GuiUtils.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.imageWidth, this.imageHeight, getBlitOffset());
        this.name.render(matrixStack, mouseX, mouseY, partialTicks);
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

        InputMappings.Input input = InputMappings.getKey(keyCode, scanCode);

        if (keyCode != GLFW.GLFW_KEY_ESCAPE && !this.checkHotbarKeys(input)) {
            if (AppEng.proxy.isActionKey(ActionKey.TOGGLE_FOCUS, input)) {
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
