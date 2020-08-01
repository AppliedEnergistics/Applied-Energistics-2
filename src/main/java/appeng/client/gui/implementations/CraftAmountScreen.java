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
import net.minecraft.text.Text;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.container.implementations.CraftAmountContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.CraftRequestPacket;

public class CraftAmountScreen extends AEBaseScreen<CraftAmountContainer> {
    private final AESubScreen subGui;

    private NumberEntryWidget amountToCraft;

    private ButtonWidget next;

    public CraftAmountScreen(CraftAmountContainer container, PlayerInventory playerInventory, Text title) {
        super(container, playerInventory, title);
        this.subGui = new AESubScreen(this, container.getTarget());
    }

    @Override
    public void init() {
        super.init();

        this.amountToCraft = new NumberEntryWidget(this, 20, 30, 138, 62, NumberEntryType.CRAFT_ITEM_COUNT, value -> {
        });
        this.amountToCraft.setValue(1);
        this.amountToCraft.setTextFieldBounds(62, 57, 50);
        this.amountToCraft.setMinValue(1);
        this.amountToCraft.addButtons(children::add, this::addButton);

        this.next = this
                .addButton(new ButtonWidget(this.x + 128, this.y + 51, 38, 20, GuiText.Next.text(), this::confirm));

        subGui.addBackButton(this::addButton, 154, 0);
    }

    private void confirm(ButtonWidget button) {
        NetworkHandler.instance()
                .sendToServer(new CraftRequestPacket((int) this.amountToCraft.getValue(), hasShiftDown()));
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.textRenderer.method_30883(matrices, GuiText.SelectAmount.text(), 8, 6, 4210752);
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY,
            float partialTicks) {
        this.next.setMessage(hasShiftDown() ? GuiText.Start.text() : GuiText.Next.text());

        this.bindTexture("guis/craft_amt.png");
        drawTexture(matrices, offsetX, offsetY, 0, 0, this.backgroundWidth, this.backgroundHeight);

        this.next.active = this.amountToCraft.getValue() > 0;

        this.amountToCraft.render(matrices, offsetX, offsetY, partialTicks);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {
        if (keyCode == 28) {
            this.next.onPress();
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
        }
    }

    protected String getBackground() {
        return "guis/craftAmt.png";
    }

}
