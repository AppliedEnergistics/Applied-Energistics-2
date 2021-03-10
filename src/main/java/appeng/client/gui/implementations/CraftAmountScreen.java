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

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.container.implementations.CraftAmountContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.CraftRequestPacket;

public class CraftAmountScreen extends AEBaseScreen<CraftAmountContainer> {
    private final AESubScreen subGui;

    private NumberEntryWidget amountToCraft;

    private Button next;

    public CraftAmountScreen(CraftAmountContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.subGui = new AESubScreen(this, container.getTarget());
    }

    @Override
    public void init() {
        super.init();

        this.amountToCraft = new NumberEntryWidget(this, 20, 30, 138, 62, NumberEntryType.CRAFT_ITEM_COUNT);
        this.amountToCraft.setValue(1);
        this.amountToCraft.setTextFieldBounds(62, 57, 50);
        this.amountToCraft.setMinValue(1);
        this.amountToCraft.setHideValidationIcon(true);
        this.amountToCraft.addButtons(children::add, this::addButton);

        this.next = this.addButton(
                new Button(this.leftPos + 128, this.topPos + 51, 38, 20, GuiText.Next.text(), this::confirm));
        this.amountToCraft.setOnConfirm(() -> this.confirm(this.next));

        subGui.addBackButton(this::addButton, 154, 0);

        changeFocus(true);
    }

    private void confirm(Button button) {
        int amount = this.amountToCraft.getIntValue().orElse(0);
        if (amount <= 0) {
            return;
        }
        NetworkHandler.instance().sendToServer(new CraftRequestPacket(amount, hasShiftDown()));
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        this.font.draw(matrixStack, GuiText.SelectAmount.text().getString(), 8, 6, 4210752);
    }

    @Override
    public void drawBG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        this.next.setMessage(hasShiftDown() ? GuiText.Start.text() : GuiText.Next.text());

        this.bindTexture("guis/craft_amt.png");
        GuiUtils.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.imageWidth, this.imageHeight, getBlitOffset());

        this.next.active = this.amountToCraft.getIntValue().orElse(0) > 0;

        this.amountToCraft.render(matrixStack, offsetX, offsetY, partialTicks);
    }

}
