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


import appeng.api.util.IConfigManager;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.client.gui.widgets.GuiCustomSlot;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.fluids.client.gui.widgets.GuiFluidSlot;
import appeng.fluids.client.gui.widgets.GuiFluidTank;
import appeng.fluids.container.ContainerFluidInterface;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.fluids.util.IAEFluidTank;
import appeng.util.IConfigManagerHost;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import java.io.IOException;


public class GuiFluidInterface extends GuiUpgradeable implements IConfigManagerHost {
    public final static int ID_BUTTON_TANK = 222;

    private final IFluidInterfaceHost host;
    private final ContainerFluidInterface container;
    private GuiTabButton priority;

    public GuiFluidInterface(final InventoryPlayer ip, final IFluidInterfaceHost te) {
        super(new ContainerFluidInterface(ip, te));
        this.ySize = 231;
        this.xSize = 245;
        this.host = te;
        (this.container = (ContainerFluidInterface) this.inventorySlots).setGui(this);

    }

    @Override
    public void initGui() {
        super.initGui();

        final IAEFluidTank configFluids = this.host.getDualityFluidInterface().getConfig();
        final IAEFluidTank fluidTank = this.host.getDualityFluidInterface().getTanks();

        for (int i = 0; i < DualityFluidInterface.NUMBER_OF_TANKS; ++i) {
            this.guiSlots.add(new GuiFluidTank(fluidTank, i, DualityFluidInterface.NUMBER_OF_TANKS + i, 8 + 18 * i, 53, 16, 68));
            this.guiSlots.add(new GuiFluidSlot(configFluids, i, i, 8 + 18 * i, 35));
        }

        this.priority = new GuiTabButton(this.getGuiLeft() + 154, this.getGuiTop(), 2 + 4 * 16, GuiText.Priority.getLocal(), this.itemRender);
        this.buttonList.add(this.priority);
    }

    @Override
    protected void addButtons() {
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRenderer.drawString(this.getGuiDisplayName(GuiText.FluidInterface.getLocal()), 8, 6, 4210752);
        this.fontRenderer.drawString(GuiText.Config.getLocal(), 8, 6 + 11 + 7, 4210752);
        this.fontRenderer.drawString(GuiText.StoredFluids.getLocal(), 8, 6 + 112 + 7, 4210752);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTexture("guis/interfacefluidextendedlife.png");
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        super.actionPerformed(btn);

        if (btn == this.priority) {
            NetworkHandler.instance().sendToServer(new PacketSwitchGuis(GuiBridge.GUI_PRIORITY));
        }
    }

    @Override
    protected void mouseClicked(int xCoord, int yCoord, int btn) throws IOException {
        for (GuiCustomSlot slot : this.guiSlots) {
            if (slot instanceof GuiFluidTank) {
                if (this.isPointInRegion(slot.xPos(), slot.yPos(), slot.getWidth(), slot.getHeight(), xCoord, yCoord) && slot.canClick(this.mc.player)) {
                    this.container.setTargetStack(((GuiFluidTank) slot).getFluidStack());
                    slot.slotClicked(this.mc.player.inventory.getItemStack(), btn);
                    return;
                }
            }
        }
        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected boolean drawUpgrades() {
        return false;
    }

    @Override
    public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue) {

    }
}
