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
import appeng.client.gui.NumberEntryType;
import appeng.container.implementations.PriorityContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;

public class PriorityScreen extends AEBaseScreen<PriorityContainer> {

    private final AESubScreen subGui;

    private NumberEntryWidget priority;

    public PriorityScreen(PriorityContainer container, PlayerInventory playerInventory, Text title) {
        super(container, playerInventory, title);
        this.subGui = new AESubScreen(this, container.getPriorityHost());
    }

    @Override
    public void init() {
        super.init();

        this.priority = new NumberEntryWidget(this, 20, 30, 138, 62, NumberEntryType.PRIORITY, this::onPriorityChange);
        this.priority.setTextFieldBounds(62, 57, 50);
        this.priority.setMinValue(Integer.MIN_VALUE);
        handler.setTextField(this.priority);
        this.priority.addButtons(children::add, this::addButton);

        this.subGui.addBackButton(this::addButton, 154, 0);
    }

    private void onPriorityChange(long priority) {
        NetworkHandler.instance()
                .sendToServer(new ConfigValuePacket("PriorityHost.Priority", String.valueOf(priority)));
    }

    @Override
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.textRenderer.method_30883(matrices, GuiText.Priority.text(), 8, 6, 4210752);
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY,
            float partialTicks) {
        this.bindTexture(getBackground());
        drawTexture(matrices, offsetX, offsetY, 0, 0, this.backgroundWidth, this.backgroundHeight);

        this.priority.render(matrices, mouseX, mouseY, partialTicks);
    }

    protected String getBackground() {
        return "guis/priority.png";
    }
}
