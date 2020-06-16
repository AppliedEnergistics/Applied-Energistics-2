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

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.CommonButtons;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.me.ItemRepo;
import appeng.client.me.SlotME;
import appeng.container.implementations.ContainerNetworkStatus;
import appeng.core.localization.GuiText;
import appeng.util.Platform;

public class GuiNetworkStatus extends AEBaseGui<ContainerNetworkStatus> implements ISortSource {

    private final ItemRepo repo;
    private final int rows = 4;
    private int tooltip = -1;

    public GuiNetworkStatus(ContainerNetworkStatus container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        final GuiScrollbar scrollbar = new GuiScrollbar();

        this.setScrollBar(scrollbar);
        this.repo = new ItemRepo(scrollbar, this);
        this.ySize = 153;
        this.xSize = 195;
        this.repo.setRowSize(5);
    }

    @Override
    public void init() {
        super.init();

        this.addButton(CommonButtons.togglePowerUnit(this.guiLeft - 18, this.guiTop + 8));
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float btn) {

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

        super.render(mouseX, mouseY, btn);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.font.drawString(GuiText.NetworkDetails.getLocal(), 8, 6, 4210752);

        this.font.drawString(
                GuiText.StoredPower.getLocal() + ": " + Platform.formatPowerLong(container.getCurrentPower(), false),
                13, 16, 4210752);
        this.font.drawString(
                GuiText.MaxPower.getLocal() + ": " + Platform.formatPowerLong(container.getMaxPower(), false), 13, 26,
                4210752);

        this.font.drawString(GuiText.PowerInputRate.getLocal() + ": "
                + Platform.formatPowerLong(container.getAverageAddition(), true), 13, 143 - 10, 4210752);
        this.font.drawString(
                GuiText.PowerUsageRate.getLocal() + ": " + Platform.formatPowerLong(container.getPowerUsage(), true),
                13, 143 - 20, 4210752);

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
                RenderSystem.pushMatrix();
                RenderSystem.scalef(0.5f, 0.5f, 0.5f);

                String str = Long.toString(refStack.getStackSize());
                if (refStack.getStackSize() >= 10000) {
                    str = Long.toString(refStack.getStackSize() / 1000) + 'k';
                }

                final int w = this.font.getStringWidth(str);
                this.font.drawString(str, (int) ((x * sectionLength + xo + sectionLength - 19 - (w * 0.5)) * 2),
                        (y * 18 + yo + 6) * 2, 4210752);

                RenderSystem.popMatrix();
                final int posX = x * sectionLength + xo + sectionLength - 18;
                final int posY = y * 18 + yo;

                if (this.tooltip == z - viewStart) {
                    toolTip = Platform.getItemDisplayName(refStack).getFormattedText();

                    toolTip += ('\n' + GuiText.Installed.getLocal() + ": " + (refStack.getStackSize()));
                    if (refStack.getCountRequestable() > 0) {
                        toolTip += ('\n' + GuiText.EnergyDrain.getLocal() + ": "
                                + Platform.formatPowerLong(refStack.getCountRequestable(), true));
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
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks) {
        this.bindTexture("guis/networkstatus.png");
        GuiUtils.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize, getBlitOffset());
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
    protected void renderTooltip(final ItemStack stack, final int x, final int y) {
        final Slot s = this.getSlot(x, y);

        if (s instanceof SlotME && !stack.isEmpty()) {
            IAEItemStack myStack = null;

            try {
                final SlotME theSlotField = (SlotME) s;
                myStack = theSlotField.getAEStack();
            } catch (final Throwable ignore) {
            }

            if (myStack != null) {
                List<String> currentToolTip = getTooltipFromItem(stack);

                while (currentToolTip.size() > 1) {
                    currentToolTip.remove(1);
                }

                currentToolTip.add(GuiText.Installed.getLocal() + ": " + (myStack.getStackSize()));
                currentToolTip.add(GuiText.EnergyDrain.getLocal() + ": "
                        + Platform.formatPowerLong(myStack.getCountRequestable(), true));

                this.drawTooltip(x, y, currentToolTip);
            }
        }

        super.renderTooltip(stack, x, y);
    }

    @Override
    public SortOrder getSortBy() {
        return SortOrder.NAME;
    }

    @Override
    public SortDir getSortDir() {
        return SortDir.ASCENDING;
    }

    @Override
    public ViewItems getSortDisplay() {
        return ViewItems.ALL;
    }
}
