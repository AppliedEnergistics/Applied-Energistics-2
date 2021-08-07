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

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.container.implementations.PriorityContainer;

public class PriorityScreen extends AEBaseScreen<PriorityContainer> {

    private final AESubScreen subGui;

    private final NumberEntryWidget priority;

    public PriorityScreen(PriorityContainer container, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(container, playerInventory, title, style);
        this.subGui = new AESubScreen(container.getPriorityHost());
        this.subGui.addBackButton("back", widgets);

        this.priority = new NumberEntryWidget(NumberEntryType.PRIORITY);
        this.priority.setTextFieldBounds(62, 57, 50);
        this.priority.setMinValue(Integer.MIN_VALUE);
        this.priority.setValue(this.menu.getPriorityValue());
        this.priority.setOnChange(this::savePriority);
        this.priority.setOnConfirm(() -> {
            savePriority();
            this.subGui.goBack();
        });
        widgets.add("priority", priority);
    }

    private void savePriority() {
        OptionalInt priority = this.priority.getIntValue();
        if (priority.isPresent()) {
            menu.setPriority(priority.getAsInt());
        }
    }

    @Override
    public void drawBG(PoseStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        super.drawBG(matrixStack, offsetX, offsetY, mouseX, mouseY, partialTicks);

        this.priority.render(matrixStack, mouseX, mouseY, partialTicks);
    }

}
