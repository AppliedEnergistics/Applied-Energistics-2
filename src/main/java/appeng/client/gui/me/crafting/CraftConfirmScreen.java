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

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Blitter;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.widgets.Scrollbar;
import appeng.container.me.crafting.CraftConfirmContainer;
import appeng.container.me.crafting.CraftingPlanSummary;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

import java.text.NumberFormat;

/**
 * This screen shows the computed crafting plan and allows the player to select a CPU on which it should be scheduled
 * for crafting.
 */
public class CraftConfirmScreen extends AEBaseScreen<CraftConfirmContainer> {

    private static final Blitter BACKGROUND = Blitter.texture("guis/craftingreport.png").src(0, 0, 238, 206);

    private final AESubScreen subGui;
    private final CraftingPlanTable table;

    private Button start;
    private Button selectCPU;

    public CraftConfirmScreen(CraftConfirmContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title, BACKGROUND);
        this.subGui = new AESubScreen(this, container.getTarget());
        this.table = new CraftingPlanTable(this, 9, 19);

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

        this.setScrollBar();
    }

    @Override
    public void render(MatrixStack matrixStack, final int mouseX, final int mouseY, final float btn) {
        this.updateCPUButtonText();

        CraftingPlanSummary plan = container.getPlan();
        boolean planIsStartable = plan != null && !plan.isSimulation();
        this.start.active = !this.container.hasNoCPU() && planIsStartable;
        this.selectCPU.active = planIsStartable;

        super.render(matrixStack, mouseX, mouseY, btn);
    }

    private void updateCPUButtonText() {
        this.selectCPU.setMessage(getNextCpuButtonLabel());
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

        return GuiText.CraftingCPU.withSuffix(": ").append(cpuName);
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
                       final int mouseY) {
        String titleSuffix;
        CraftingPlanSummary plan = this.container.getPlan();
        if (plan == null) {
            titleSuffix = GuiText.CalculatingWait.getLocal();
        } else {
            String byteUsed = NumberFormat.getInstance().format(plan.getUsedBytes());
            titleSuffix = byteUsed + ' ' + GuiText.BytesUsed.getLocal();
        }
        this.font.drawString(matrixStack, GuiText.CraftingPlan.getLocal() + " - " + titleSuffix, 8, 7, COLOR_DARK_GRAY);

        if (plan != null) {
            this.table.render(matrixStack, mouseX, mouseY, plan, getScrollBar().getCurrentScroll());

            // Show additional status about the selected CPU
            String cpuStatus;
            if (plan.isSimulation()) {
                cpuStatus = GuiText.Simulation.getLocal();
            } else {
                cpuStatus = this.container.getCpuAvailableBytes() > 0
                        ? (GuiText.Bytes.getLocal() + ": " + this.container.getCpuAvailableBytes() + " : "
                        + GuiText.CoProcessors.getLocal() + ": " + this.container.getCpuCoProcessors())
                        : GuiText.Bytes.getLocal() + ": N/A : " + GuiText.CoProcessors.getLocal() + ": N/A";
            }

            final int offset = (219 - this.font.getStringWidth(cpuStatus)) / 2;
            this.font.drawString(matrixStack, cpuStatus, offset, 165, COLOR_DARK_GRAY);
        }

    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX,
                       final int mouseY, float partialTicks) {
        this.setScrollBar();
        super.drawBG(matrices, offsetX, offsetY, mouseX, mouseY, partialTicks);
    }

    private void setScrollBar() {
        CraftingPlanSummary plan = container.getPlan();
        final int size = plan != null ? plan.getEntries().size() : 0;

        this.getScrollBar().setTop(19).setLeft(218).setHeight(114);
        this.getScrollBar().setRange(0, CraftingPlanTable.getScrollableRows(size), 1);
    }

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
