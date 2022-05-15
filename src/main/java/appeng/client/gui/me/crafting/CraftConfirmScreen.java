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

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.localization.GuiText;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingPlanSummary;

/**
 * This screen shows the computed crafting plan and allows the player to select a CPU on which it should be scheduled
 * for crafting.
 */
public class CraftConfirmScreen extends AEBaseScreen<CraftConfirmMenu> {

    private final CraftConfirmTableRenderer table;

    private final Button start;
    private final Button selectCPU;
    private final Scrollbar scrollbar;

    public CraftConfirmScreen(CraftConfirmMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.table = new CraftConfirmTableRenderer(this, 9, 19);

        this.scrollbar = widgets.addScrollBar("scrollbar");

        this.start = widgets.addButton("start", GuiText.Start.text(), this::start);
        this.start.active = false;

        this.selectCPU = widgets.addButton("selectCpu", getNextCpuButtonLabel(), this::selectNextCpu);
        this.selectCPU.active = false;

        widgets.addButton("cancel", GuiText.Cancel.text(), menu::goBack);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.selectCPU.setMessage(getNextCpuButtonLabel());

        CraftingPlanSummary plan = menu.getPlan();
        boolean planIsStartable = plan != null && !plan.isSimulation();
        this.start.active = !this.menu.hasNoCPU() && planIsStartable;
        this.selectCPU.active = planIsStartable;

        // Show additional status about the selected CPU and plan when the planning is done
        Component planDetails = GuiText.CalculatingWait.text();
        Component cpuDetails = Component.empty();
        if (plan != null) {
            String byteUsed = NumberFormat.getInstance().format(plan.getUsedBytes());
            planDetails = GuiText.BytesUsed.text(byteUsed);

            if (plan.isSimulation()) {
                cpuDetails = GuiText.PartialPlan.text();
            } else if (this.menu.getCpuAvailableBytes() > 0) {
                cpuDetails = GuiText.ConfirmCraftCpuStatus.text(
                        this.menu.getCpuAvailableBytes(),
                        this.menu.getCpuCoProcessors());
            } else {
                cpuDetails = GuiText.ConfirmCraftNoCpu.text();
            }
        }

        setTextContent(TEXT_ID_DIALOG_TITLE, GuiText.CraftingPlan.text(planDetails));
        setTextContent("cpu_status", cpuDetails);

        final int size = plan != null ? plan.getEntries().size() : 0;
        scrollbar.setRange(0, AbstractTableRenderer.getScrollableRows(size), 1);
    }

    private Component getNextCpuButtonLabel() {
        if (this.menu.hasNoCPU()) {
            return GuiText.NoCraftingCPUs.text();
        }

        Component cpuName;
        if (this.menu.cpuName == null) {
            cpuName = GuiText.Automatic.text();
        } else {
            cpuName = this.menu.cpuName;
        }

        return GuiText.SelectedCraftingCPU.text(cpuName);
    }

    @Override
    public void drawFG(PoseStack poseStack, int offsetX, int offsetY, int mouseX,
            int mouseY) {

        CraftingPlanSummary plan = menu.getPlan();
        if (plan != null) {
            this.table.render(poseStack, mouseX, mouseY, plan.getEntries(), scrollbar.getCurrentScroll());
        }

    }

    @org.jetbrains.annotations.Nullable
    @Override
    public GenericStack getStackUnderMouse(double mouseX, double mouseY) {
        var hovered = table.getHoveredStack();
        if (hovered != null) {
            return hovered;
        }
        return super.getStackUnderMouse(mouseX, mouseY);
    }

    // Allow players to confirm a craft via the enter key
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {
        if (!this.checkHotbarKeys(InputConstants.getKey(keyCode, scanCode))
                && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            this.start();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
    }

    private void selectNextCpu() {
        getMenu().cycleSelectedCPU(!isHandlingRightClick());
    }

    private void start() {
        getMenu().startJob();
    }

}
