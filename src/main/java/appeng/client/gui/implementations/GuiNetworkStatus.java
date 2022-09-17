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


import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.me.ItemRepo;
import appeng.client.me.SlotME;
import appeng.container.implementations.ContainerNetworkStatus;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.util.Platform;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;


public class GuiNetworkStatus extends AEBaseGui implements ISortSource {

    private final ItemRepo repo;
    private final int rows = 4;
    private GuiImgButton units;
    private int tooltip = -1;

    public GuiNetworkStatus(final InventoryPlayer inventoryPlayer, final INetworkTool te) {
        super(new ContainerNetworkStatus(inventoryPlayer, te));
        final GuiScrollbar scrollbar = new GuiScrollbar();

        this.setScrollBar(scrollbar);
        this.repo = new ItemRepo(scrollbar, this);
        this.ySize = 153;
        this.xSize = 195;
        this.repo.setRowSize(5);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        super.actionPerformed(btn);

        final boolean backwards = Mouse.isButtonDown(1);

        if (btn == this.units) {
            AEConfig.instance().nextPowerUnit(backwards);
            this.units.set(AEConfig.instance().selectedPowerUnit());
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        this.units = new GuiImgButton(this.guiLeft - 18, this.guiTop + 8, Settings.POWER_UNITS, AEConfig.instance().selectedPowerUnit());
        this.buttonList.add(this.units);
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float btn) {

        final int gx = (this.width - this.xSize) / 2;
        final int gy = (this.height - this.ySize) / 2;

        this.tooltip = -1;

        int y = 0;
        int x = 0;
        for (int z = 0; z <= 4 * 5; z++) {
            final int minX = gx + 14 + x * 31;
            final int minY = gy + 41 + y * 18;

            if (minX < mouseX && minX + 28 > mouseX) {
                if (minY < mouseY && minY + 20 > mouseY) {
                    this.tooltip = z;
                    break;
                }
            }

            x++;

            if (x > 4) {
                y++;
                x = 0;
            }
        }

        super.drawScreen(mouseX, mouseY, btn);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        final ContainerNetworkStatus ns = (ContainerNetworkStatus) this.inventorySlots;

        this.fontRenderer.drawString(GuiText.NetworkDetails.getLocal(), 8, 6, 4210752);

        this.fontRenderer.drawString(GuiText.StoredPower.getLocal() + ": " + Platform.formatPowerLong(ns.getCurrentPower(), false), 13, 16, 4210752);
        this.fontRenderer.drawString(GuiText.MaxPower.getLocal() + ": " + Platform.formatPowerLong(ns.getMaxPower(), false), 13, 26, 4210752);

        this.fontRenderer.drawString(GuiText.PowerInputRate.getLocal() + ": " + Platform.formatPowerLong(ns.getAverageAddition(), true), 13, 143 - 10,
                4210752);
        this.fontRenderer.drawString(GuiText.PowerUsageRate.getLocal() + ": " + Platform.formatPowerLong(ns.getPowerUsage(), true), 13, 143 - 20, 4210752);

        final int sectionLength = 30;

        int x = 0;
        int y = 0;
        final int xo = 12;
        final int yo = 42;
        final int viewStart = 0;// myScrollBar.getCurrentScroll() * 5;
        final int viewEnd = viewStart + 5 * 4;

        String toolTip = "";
        int toolPosX = 0;
        int toolPosY = 0;

        for (int z = viewStart; z < Math.min(viewEnd, this.repo.size()); z++) {
            final IAEItemStack refStack = this.repo.getReferenceItem(z);
            if (refStack != null) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.5, 0.5, 0.5);

                String str = Long.toString(refStack.getStackSize());
                if (refStack.getStackSize() >= 10000) {
                    str = Long.toString(refStack.getStackSize() / 1000) + 'k';
                }

                final int w = this.fontRenderer.getStringWidth(str);
                this.fontRenderer.drawString(str, (int) ((x * sectionLength + xo + sectionLength - 19 - (w * 0.5)) * 2), (y * 18 + yo + 6) * 2,
                        4210752);

                GlStateManager.popMatrix();
                final int posX = x * sectionLength + xo + sectionLength - 18;
                final int posY = y * 18 + yo;

                if (this.tooltip == z - viewStart) {
                    toolTip = Platform.getItemDisplayName(refStack);

                    toolTip += ('\n' + GuiText.Installed.getLocal() + ": " + (refStack.getStackSize()));
                    if (refStack.getCountRequestable() > 0) {
                        toolTip += ('\n' + GuiText.EnergyDrain.getLocal() + ": " + Platform.formatPowerLong(refStack.getCountRequestable(), true));
                    }

                    toolPosX = x * sectionLength + xo + sectionLength - 8;
                    toolPosY = y * 18 + yo;
                }

                this.drawItem(posX, posY, refStack.asItemStackRepresentation());

                x++;

                if (x > 4) {
                    y++;
                    x = 0;
                }
            }
        }

        if (this.tooltip >= 0 && toolTip.length() > 0) {
            this.drawTooltip(toolPosX, toolPosY + 10, toolTip);
        }
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.bindTexture("guis/networkstatus.png");
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
    }

    public void postUpdate(final List<IAEItemStack> list) {
        this.repo.clear();

        for (final IAEItemStack is : list) {
            this.repo.postUpdate(is);
        }

        this.repo.updateView();
        this.setScrollBar();
    }

    private void setScrollBar() {
        final int size = this.repo.size();
        this.getScrollBar().setTop(39).setLeft(175).setHeight(78);
        this.getScrollBar().setRange(0, (size + 4) / 5 - this.rows, 1);
    }

    @Override
    protected void renderToolTip(final ItemStack stack, final int x, final int y) {
        final Slot s = this.getSlot(x, y);

        if (s instanceof SlotME && stack != null) {
            IAEItemStack myStack = null;

            try {
                final SlotME theSlotField = (SlotME) s;
                myStack = theSlotField.getAEStack();
            } catch (final Throwable ignore) {
            }

            if (myStack != null) {
                ITooltipFlag.TooltipFlags tooltipFlag = this.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;
                List<String> currentToolTip = stack.getTooltip(this.mc.player, tooltipFlag);

                while (currentToolTip.size() > 1) {
                    currentToolTip.remove(1);
                }

                currentToolTip.add(GuiText.Installed.getLocal() + ": " + (myStack.getStackSize()));
                currentToolTip.add(GuiText.EnergyDrain.getLocal() + ": " + Platform.formatPowerLong(myStack.getCountRequestable(), true));

                this.drawTooltip(x, y, currentToolTip);
            }
        }

        super.renderToolTip(stack, x, y);
    }

    @Override
    public Enum getSortBy() {
        return SortOrder.NAME;
    }

    @Override
    public Enum getSortDir() {
        return SortDir.ASCENDING;
    }

    @Override
    public Enum getSortDisplay() {
        return ViewItems.ALL;
    }
}
