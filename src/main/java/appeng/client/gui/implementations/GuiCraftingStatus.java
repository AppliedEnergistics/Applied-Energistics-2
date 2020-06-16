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

/**
 *
 */

package appeng.client.gui.implementations;

import java.io.IOException;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.container.implementations.ContainerCraftingStatus;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;

public class GuiCraftingStatus extends GuiCraftingCPU<ContainerCraftingStatus> {

    private final AESubGui subGui;

    private Button selectCPU;

    public GuiCraftingStatus(ContainerCraftingStatus container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.subGui = new AESubGui(this, container.getTarget());
    }

    @Override
    public void init() {
        super.init();

        this.selectCPU = new Button(this.guiLeft + 8, this.guiTop + this.ySize - 25, 150, 20,
                GuiText.CraftingCPU.getLocal() + ": " + GuiText.NoCraftingCPUs, btn -> selectNextCpu());
        this.addButton(this.selectCPU);

        subGui.addBackButton(btn -> {
            addButton(btn);
            btn.setHideEdge(13);
        }, 213, -4);
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float btn) {
        this.updateCPUButtonText();
        super.render(mouseX, mouseY, btn);
    }

    private void updateCPUButtonText() {
        String btnTextText = GuiText.NoCraftingJobs.getLocal();

        if (this.container.selectedCpu >= 0)// && status.selectedCpu < status.cpus.size() )
        {
            if (this.container.myName.length() > 0) {
                final String name = this.container.myName.substring(0, Math.min(20, this.container.myName.length()));
                btnTextText = GuiText.CPUs.getLocal() + ": " + name;
            } else {
                btnTextText = GuiText.CPUs.getLocal() + ": #" + this.container.selectedCpu;
            }
        }

        if (this.container.noCPU) {
            btnTextText = GuiText.NoCraftingJobs.getLocal();
        }

        this.selectCPU.setMessage(btnTextText);
    }

    @Override
    protected String getGuiDisplayName(final String in) {
        return in; // the cup name is on the button
    }

    // FIXME: Extract to separate class? Shared with GuiCraftConfirm
    private void selectNextCpu() {
        final boolean backwards = minecraft.mouseHelper.isRightDown();
        NetworkHandler.instance().sendToServer(new PacketValueConfig("Terminal.Cpu", backwards ? "Prev" : "Next"));
    }

}
