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

import java.text.NumberFormat;

import com.mojang.blaze3d.matrix.MatrixStack;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Blitter;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.widgets.Scrollbar;
import appeng.container.me.crafting.CraftConfirmContainer;
import appeng.container.me.crafting.CraftingPlanSummary;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;

/**
 * This screen shows the computed crafting plan and allows the player to select a CPU on which it should be scheduled
 * for crafting.
 */
public class CraftConfirmScreen extends AEBaseScreen<CraftConfirmContainer> {

    private static final Blitter BACKGROUND = Blitter.texture("guis/craftingreport.png").src(0, 0, 238, 206);

    private final AESubScreen subGui;
    private final CraftConfirmTableRenderer table;

    private Button start;
    private Button selectCPU;

    public CraftConfirmScreen(CraftConfirmContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title, BACKGROUND);
        this.subGui = new AESubScreen(this, container.getTarget());
        this.table = new CraftConfirmTableRenderer(this, 9, 19);

        this.setScrollBar(new Scrollbar());
    }

    @Override
    public void init() {
        super.init();

        this.start = new Button(this.guiLeft + 162, this.guiTop + this.ySize - 25, 50, 20, GuiText.Start.text(),
                btn -> start());
        this.start.active = false;
        this.addButton(this.start);

        this.selectCPU = new Button(this.guiLeft + (219 - 180) / 2, this.guiTop + this.ySize - 68, 180, 20,
                getNextCpuButtonLabel(), btn -> selectNextCpu());
        this.selectCPU.active = false;
        this.addButton(this.selectCPU);

        addButton(new Button(this.guiLeft + 6, this.guiTop + this.ySize - 25, 50, 20, GuiText.Cancel.text(),
                btn -> subGui.goBack()));

        this.getScrollBar().setTop(19).setLeft(218).setHeight(114);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.selectCPU.setMessage(getNextCpuButtonLabel());

        CraftingPlanSummary plan = container.getPlan();
        boolean planIsStartable = plan != null && !plan.isSimulation();
        this.start.active = !this.container.hasNoCPU() && planIsStartable;
        this.selectCPU.active = planIsStartable;

        // Show additional status about the selected CPU and plan when the planning is done
        ITextComponent planDetails = GuiText.CalculatingWait.text();
        ITextComponent cpuDetails = StringTextComponent.EMPTY;
        if (plan != null) {
            String byteUsed = NumberFormat.getInstance().format(plan.getUsedBytes());
            planDetails = GuiText.BytesUsed.text(byteUsed);

            if (plan.isSimulation()) {
                cpuDetails = GuiText.Simulation.text();
            } else if (this.container.getCpuAvailableBytes() > 0) {
                cpuDetails = GuiText.ConfirmCraftCpuStatus.text(
                        this.container.getCpuAvailableBytes(),
                        this.container.getCpuCoProcessors());
            } else {
                cpuDetails = GuiText.ConfirmCraftNoCpu.text();
            }
        }

        setTextContent(TEXT_ID_DIALOG_TITLE, GuiText.CraftingPlan.text(planDetails));
        setTextContent("cpu_status", cpuDetails);

        final int size = plan != null ? plan.getEntries().size() : 0;
        this.getScrollBar().setRange(0, AbstractTableRenderer.getScrollableRows(size), 1);
    }

    private ITextComponent getNextCpuButtonLabel() {
        if (this.container.hasNoCPU()) {
            return GuiText.NoCraftingCPUs.text();
        }

        ITextComponent cpuName;
        if (this.container.cpuName == null) {
            cpuName = GuiText.Automatic.text();
        } else {
            cpuName = this.container.cpuName;
        }

        return GuiText.SelectedCraftingCPU.text(cpuName);
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {

        CraftingPlanSummary plan = container.getPlan();
        if (plan != null) {
            this.table.render(matrixStack, mouseX, mouseY, plan.getEntries(), getScrollBar().getCurrentScroll());
        }

    }

    // Allow players to confirm a craft via the enter key
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {
        if (!this.checkHotbarKeys(InputMappings.getInputByCode(keyCode, scanCode))) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                this.start();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
    }

    private void selectNextCpu() {
        final boolean backwards = isHandlingRightClick();
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("Terminal.Cpu", backwards ? "Prev" : "Next"));
    }

    private void start() {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("Terminal.Start", "Start"));
    }

}
