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

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.NumberBox;
import appeng.container.implementations.PriorityContainer;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;

public class PriorityScreen extends AEBaseScreen<PriorityContainer> {

    private final AESubScreen subGui;

    private NumberBox priority;

    public PriorityScreen(PriorityContainer container, PlayerInventory playerInventory, Text title) {
        super(container, playerInventory, title);
        this.subGui = new AESubScreen(this, container.getPriorityHost());
    }

    @Override
    public void init() {
        super.init();

        final int a = AEConfig.instance().priorityByStacksAmounts(0);
        final int b = AEConfig.instance().priorityByStacksAmounts(1);
        final int c = AEConfig.instance().priorityByStacksAmounts(2);
        final int d = AEConfig.instance().priorityByStacksAmounts(3);

        this.addButton(new ButtonWidget(this.x + 20, this.y + 32, 22, 20, new LiteralText("+" + a), btn -> addQty(a)));
        this.addButton(new ButtonWidget(this.x + 48, this.y + 32, 28, 20, new LiteralText("+" + b), btn -> addQty(b)));
        this.addButton(new ButtonWidget(this.x + 82, this.y + 32, 32, 20, new LiteralText("+" + c), btn -> addQty(c)));
        this.addButton(new ButtonWidget(this.x + 120, this.y + 32, 38, 20, new LiteralText("+" + d), btn -> addQty(d)));

        this.addButton(new ButtonWidget(this.x + 20, this.y + 69, 22, 20, new LiteralText("-" + a), btn -> addQty(-a)));
        this.addButton(new ButtonWidget(this.x + 48, this.y + 69, 28, 20, new LiteralText("-" + b), btn -> addQty(-b)));
        this.addButton(new ButtonWidget(this.x + 82, this.y + 69, 32, 20, new LiteralText("-" + c), btn -> addQty(-c)));
        this.addButton(new ButtonWidget(this.x + 120, this.y + 69, 38, 20, new LiteralText("-" + d), btn -> addQty(-d)));

        this.subGui.addBackButton(this::addButton, 154, 0);

        this.priority = new NumberBox(this.textRenderer, this.x + 62, this.y + 57, 59, this.textRenderer.fontHeight,
                Long.class);
        this.priority.setHasBorder(false);
        this.priority.setMaxLength(16);
        this.priority.setEditableColor(0xFFFFFF);
        this.priority.setVisible(true);
        this.priority.setFocused(true);
        handler.setTextField(this.priority);
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.textRenderer.draw(matrices, GuiText.Priority.text(), 8, 6, 4210752);
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks) {
        this.bindTexture(getBackground());
        drawTexture(matrices, offsetX, offsetY, 0, 0, this.backgroundWidth, this.backgroundHeight);

        this.priority.render(matrices, mouseX, mouseY, partialTicks);
    }

    private void addQty(final int i) {
        try {
            String out = this.priority.getText();

            boolean fixed = false;
            while (out.startsWith("0") && out.length() > 1) {
                out = out.substring(1);
                fixed = true;
            }

            if (fixed) {
                this.priority.setText(out);
            }

            if (out.isEmpty()) {
                out = "0";
            }

            long result = Long.parseLong(out);
            result += i;

            this.priority.setText(out = Long.toString(result));

            NetworkHandler.instance().sendToServer(new ConfigValuePacket("PriorityHost.Priority", out));
        } catch (final NumberFormatException e) {
            // nope..
            this.priority.setText("0");
        }
    }

    @Override
    public boolean charTyped(char character, int key) {
        if (priority.charTyped(character, key)) {
            String out = this.priority.getText();

            boolean fixed = false;
            while (out.startsWith("0") && out.length() > 1) {
                out = out.substring(1);
                fixed = true;
            }

            if (fixed) {
                this.priority.setText(out);
            }

            if (out.isEmpty()) {
                out = "0";
            }

            NetworkHandler.instance().sendToServer(new ConfigValuePacket("PriorityHost.Priority", out));
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {
        if (!this.checkHotbarKeys(keyCode, scanCode)) {
            if ((keyCode == 211 || keyCode == 205 || keyCode == 203 || keyCode == 14)
                    && this.priority.keyPressed(keyCode, scanCode, p_keyPressed_3_)) {
                String out = this.priority.getText();

                boolean fixed = false;
                while (out.startsWith("0") && out.length() > 1) {
                    out = out.substring(1);
                    fixed = true;
                }

                if (fixed) {
                    this.priority.setText(out);
                }

                if (out.isEmpty()) {
                    out = "0";
                }

                NetworkHandler.instance().sendToServer(new ConfigValuePacket("PriorityHost.Priority", out));
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
    }

    protected String getBackground() {
        return "guis/priority.png";
    }
}
