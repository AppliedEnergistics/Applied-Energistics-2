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

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.me.crafting.CraftingStatusMenu;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;

/**
 * This specialized version of the {@link CraftingCPUScreen} allows a player to cycle through the CPUs that are
 * currently working on crafting jobs, and see their crafting status.
 */
public class CraftingStatusScreen extends CraftingCPUScreen<CraftingStatusMenu> {

    private final Button selectCPU;

    public CraftingStatusScreen(CraftingStatusMenu container, Inventory playerInventory,
                                Component title, ScreenStyle style) {
        super(container, playerInventory, title, style);
        this.selectCPU = widgets.addButton("selectCpu", getNextCpuButtonLabel(), this::selectNextCpu);

        AESubScreen subGui = new AESubScreen(container.getTarget());
        subGui.addBackButton("back", widgets);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.selectCPU.setMessage(getNextCpuButtonLabel());
    }

    private Component getNextCpuButtonLabel() {
        if (this.menu.noCPU) {
            return GuiText.NoCraftingJobs.text();
        }
        // it's possible that the cpu name has not synchronized from server->client yet, since fields are synced
        // individually.
        Component name = menu.cpuName;
        if (name == null) {
            name = TextComponent.EMPTY;
        }
        return GuiText.SelectedCraftingCPU.text(name);
    }

    @Override
    protected Component getGuiDisplayName(final Component in) {
        return in; // the cpu name is on the button
    }

    private void selectNextCpu() {
        final boolean backwards = isHandlingRightClick();
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("Terminal.Cpu", backwards ? "Prev" : "Next"));
    }

}
