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

package appeng.client.gui.me.crafting;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Blitter;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.implementations.NumberEntryWidget;
import appeng.container.me.crafting.CraftAmountContainer;
import appeng.core.localization.GuiText;

/**
 * When requesting to auto-craft, this dialog allows the player to enter the desired number of items to craft.
 */
public class CraftAmountScreen extends AEBaseScreen<CraftAmountContainer> {

    private static final Blitter BACKGROUND = Blitter.texture("guis/craft_amt.png").src(0, 0, 176, 107);

    private final AESubScreen subGui;

    private NumberEntryWidget amountToCraft;

    private Button next;

    public CraftAmountScreen(CraftAmountContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title, BACKGROUND);
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
                new Button(this.guiLeft + 128, this.guiTop + 51, 38, 20, GuiText.Next.text(), this::confirm));
        this.amountToCraft.setOnConfirm(() -> this.confirm(this.next));

        subGui.addBackButton(this::addButton, 154, 0);

        changeFocus(true);
    }

    private void confirm(Button button) {
        int amount = this.amountToCraft.getIntValue().orElse(0);
        if (amount <= 0) {
            return;
        }
        container.confirm(amount, hasShiftDown());
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        this.font.drawString(matrices, GuiText.SelectAmount.text().getString(), 8, 6, COLOR_DARK_GRAY);
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY,
            float partialTicks) {
        super.drawBG(matrices, offsetX, offsetY, mouseX, mouseY, partialTicks);

        this.next.setMessage(hasShiftDown() ? GuiText.Start.text() : GuiText.Next.text());
        this.next.active = this.amountToCraft.getIntValue().orElse(0) > 0;
        this.amountToCraft.render(matrices, offsetX, offsetY, partialTicks);
    }

}
