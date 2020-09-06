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

import java.util.OptionalInt;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.theme.ThemeColor;
import appeng.container.implementations.PriorityContainer;
import appeng.core.localization.GuiText;

public class PriorityScreen extends AEBaseScreen<PriorityContainer> {

    private final AESubScreen subGui;

    private NumberEntryWidget priority;

    public PriorityScreen(PriorityContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.subGui = new AESubScreen(this, container.getPriorityHost());

        // This is the effective size of the background image
        xSize = 175;
        ySize = 128;
    }

    @Override
    public void init() {
        super.init();

        this.priority = new NumberEntryWidget(this, 20, 30, 138, 62, NumberEntryType.PRIORITY);
        this.priority.setTextFieldBounds(62, 57, 50);
        this.priority.setMinValue(Integer.MIN_VALUE);
        this.priority.setValue(this.container.getPriorityValue());
        this.priority.addButtons(children::add, this::addButton);

        this.subGui.addBackButton(this::addButton, 154, 0);

        this.priority.setOnChange(this::savePriority);
        this.priority.setOnConfirm(() -> {
            savePriority();
            this.subGui.goBack();
        });

        changeFocus(true);
    }

    private void savePriority() {
        OptionalInt priority = this.priority.getIntValue();
        if (priority.isPresent()) {
            container.setPriority(priority.getAsInt());
        }
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        this.font.drawString(matrixStack, GuiText.Priority.getLocal(), 8, 6, ThemeColor.TEXT_TITLE.argb());
    }

    @Override
    public void drawBG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        this.bindTexture("guis/priority.png");
        blit(matrixStack, offsetX, offsetY, 0, 0, this.xSize, this.ySize);

        this.priority.render(matrixStack, mouseX, mouseY, partialTicks);
    }

}
