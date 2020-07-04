/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.client.gui;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;


import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.widgets.TabButton;
import appeng.container.implementations.PriorityContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.SwitchGuisPacket;
import appeng.fluids.client.gui.widgets.FluidSlotWidget;
import appeng.fluids.client.gui.widgets.FluidTankWidget;
import appeng.fluids.container.FluidInterfaceContainer;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.util.IAEFluidTank;

public class FluidInterfaceScreen extends UpgradeableScreen<FluidInterfaceContainer> {
    public FluidInterfaceScreen(FluidInterfaceContainer container, PlayerInventory playerInventory,
            Text title) {
        super(container, playerInventory, title);
        this.backgroundHeight = 231;
    }

    @Override
    public void init() {
        super.init();

        final IAEFluidTank configFluids = this.handler.getFluidConfigInventory();
        final IAEFluidTank fluidTank = this.handler.getTanks();

        for (int i = 0; i < DualityFluidInterface.NUMBER_OF_TANKS; ++i) {
            final FluidTankWidget guiTank = new FluidTankWidget(fluidTank, i, this.getGuiLeft() + 35 + 18 * i,
                    this.getGuiTop() + 53, 16, 68);
            this.addButton(guiTank);
            this.guiSlots.add(new FluidSlotWidget(configFluids, i, i, 35 + 18 * i, 35));
        }

        this.addButton(new TabButton(this.getGuiLeft() + 154, this.getGuiTop(), 2 + 4 * 16, GuiText.Priority.getLocal(),
                this.itemRenderer, btn -> openPriorityGui()));
    }

    @Override
    protected void addButtons() {
    }

    @Override
    public void drawFG(MatrixStack matrices, int offsetX, int offsetY, int mouseX, int mouseY) {
        this.textRenderer.draw(matrices, this.getGuiDisplayName(GuiText.FluidInterface.getLocal()), 8, 6, 4210752);
        this.textRenderer.draw(matrices, GuiText.Config.getLocal(), 35, 6 + 11 + 7, 4210752);
        this.textRenderer.draw(matrices, GuiText.StoredFluids.getLocal(), 35, 6 + 112 + 7, 4210752);
        this.textRenderer.draw(matrices, GuiText.inventory.getLocal(), 8, this.backgroundHeight - 96 + 3, 4210752);
    }

    @Override
    public void drawBG(MatrixStack matrices, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        this.bindTexture("guis/interfacefluid.png");
        drawTexture(matrices, offsetX, offsetY, 0, 0, this.backgroundWidth, this.backgroundHeight, 0 /* FIXME ZINDEX */ );
    }

    private void openPriorityGui() {
        NetworkHandler.instance().sendToServer(new SwitchGuisPacket(PriorityContainer.TYPE));
    }

    @Override
    protected boolean drawUpgrades() {
        return false;
    }
}
