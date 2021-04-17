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

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import appeng.client.gui.implementations.AESubScreen;
import appeng.container.me.crafting.CraftingStatusContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;

/**
 * This specialized version of the {@link CraftingCPUScreen} allows a player to cycle through the CPUs that are
 * currently working on crafting jobs, and see their crafting status.
 */
public class CraftingStatusScreen extends CraftingCPUScreen<CraftingStatusContainer> {

    private final AESubScreen subGui;

    private Button selectCPU;

    public CraftingStatusScreen(CraftingStatusContainer container, PlayerInventory playerInventory,
            ITextComponent title) {
        super(container, playerInventory, title);
        this.subGui = new AESubScreen(this, container.getTarget());
    }

    @Override
    public void init() {
        super.init();

        this.selectCPU = new Button(this.guiLeft + 8, this.guiTop + this.ySize - 25, 150, 20, getNextCpuButtonLabel(),
                btn -> selectNextCpu());
        this.addButton(this.selectCPU);

        subGui.addBackButton(btn -> {
            addButton(btn);
            btn.setHideEdge(true);
        }, 213, -4);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.selectCPU.setMessage(getNextCpuButtonLabel());
    }

    private ITextComponent getNextCpuButtonLabel() {
        if (this.container.noCPU) {
            return GuiText.NoCraftingJobs.text();
        }
        // it's possible that the cpu name has not synchronized from server->client yet, since fields are synced
        // individually.
        ITextComponent name = container.cpuName;
        if (name == null) {
            name = StringTextComponent.EMPTY;
        }
        return GuiText.SelectedCraftingCPU.text(name);
    }

    @Override
    protected ITextComponent getGuiDisplayName(final ITextComponent in) {
        return in; // the cpu name is on the button
    }

    private void selectNextCpu() {
        final boolean backwards = isHandlingRightClick();
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("Terminal.Cpu", backwards ? "Prev" : "Next"));
    }

}
