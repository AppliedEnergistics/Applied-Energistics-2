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

package appeng.client.gui.me;

import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Blitter;
import appeng.client.gui.me.items.VirtualItemSlot;
import appeng.client.gui.widgets.CommonButtons;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.gui.widgets.Scrollbar;
import appeng.container.me.NetworkStatus;
import appeng.container.me.NetworkStatusContainer;
import appeng.core.localization.GuiText;
import appeng.util.Platform;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class NetworkStatusScreen extends AEBaseScreen<NetworkStatusContainer> implements ISortSource {

    private static final int ROWS = 4;
    private static final int COLUMNS = 5;

    private int tooltip = -1;

    private NetworkStatus status = new NetworkStatus();

    private static final Blitter BACKGROUND = Blitter.texture("guis/networkstatus.png")
            .src(0, 0, 195, 153);

    public NetworkStatusScreen(NetworkStatusContainer container, PlayerInventory playerInventory,
                               ITextComponent title) {
        super(container, playerInventory, title);
        final Scrollbar scrollbar = new Scrollbar();

        this.setScrollBar(scrollbar);
        this.ySize = BACKGROUND.getSrcWidth();
        this.xSize = BACKGROUND.getSrcHeight();
    }

    @Override
    public void init() {
        super.init();

        this.addButton(CommonButtons.togglePowerUnit(this.guiLeft - 18, this.guiTop + 8));
    }

    @Override
    public void render(MatrixStack matrixStack, final int mouseX, final int mouseY, final float btn) {

        final int gx = (this.width - this.xSize) / 2;
        final int gy = (this.height - this.ySize) / 2;

        this.tooltip = -1;

        int y = 0;
        int x = 0;
        for (int i = 0; i <= ROWS * COLUMNS; i++) {
            final int minX = gx + 14 + x * 31;
            final int minY = gy + 41 + y * 18;

            if (minX < mouseX && minX + 28 > mouseX) {
                if (minY < mouseY && minY + 20 > mouseY) {
                    this.tooltip = i;
                    break;
                }
            }

            x++;

            if (x > 4) {
                y++;
                x = 0;
            }
        }

        super.render(matrixStack, mouseX, mouseY, btn);
    }

    @Override
    public void drawBG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY,
                       float partialTicks) {
        BACKGROUND.dest(offsetX, offsetY).blit(matrices, getBlitOffset());
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
                       final int mouseY) {
        this.font.drawString(matrixStack, GuiText.NetworkDetails.getLocal(), 8, 6, COLOR_DARK_GRAY);

        this.font.drawString(matrixStack,
                GuiText.StoredPower.getLocal() + ": " + Platform.formatPower(status.getStoredPower(), false),
                13, 16, COLOR_DARK_GRAY);
        this.font.drawString(matrixStack,
                GuiText.MaxPower.getLocal() + ": " + Platform.formatPower(status.getMaxStoredPower(), false), 13, 26,
                COLOR_DARK_GRAY);

        this.font.drawString(matrixStack, GuiText.PowerInputRate.getLocal() + ": "
                + Platform.formatPower(status.getAveragePowerInjection(), true), 13, 143 - 10, COLOR_DARK_GRAY);
        this.font.drawString(matrixStack,
                GuiText.PowerUsageRate.getLocal() + ": " + Platform.formatPower(status.getAveragePowerUsage(), true),
                13, 143 - 20, COLOR_DARK_GRAY);

        final int sectionLength = 30;

        int x = 0;
        int y = 0;
        final int xo = 12;
        final int yo = 42;
        final int viewStart = getScrollBar().getCurrentScroll() * COLUMNS;
        final int viewEnd = viewStart + COLUMNS * ROWS;

        List<ITextComponent> tooltip = null;
        int toolPosX = 0;
        int toolPosY = 0;

        List<NetworkStatus.MachineEntry> machines = status.getMachines();
        for (int i = viewStart; i < Math.min(viewEnd, machines.size()); i++) {
            NetworkStatus.MachineEntry entry = machines.get(i);

            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.5f, 0.5f, 0.5f);

            String str = Long.toString(entry.getCount());
            if (entry.getCount() >= 10000) {
                str = Long.toString(entry.getCount() / 1000) + 'k';
            }

            final int w = this.font.getStringWidth(str);
            this.font.drawString(matrixStack, str,
                    (int) ((x * sectionLength + xo + sectionLength - 19 - (w * 0.5)) * 2), (y * 18 + yo + 6) * 2,
                    COLOR_DARK_GRAY);

            RenderSystem.popMatrix();
            final int posX = x * sectionLength + xo + sectionLength - 18;
            final int posY = y * 18 + yo;

            if (this.tooltip == i - viewStart) {
                tooltip = new ArrayList<>();
                tooltip.add(entry.getDisplay().getDisplayName());

                tooltip.add(GuiText.Installed.withSuffix(": " + entry.getCount()));
                if (entry.getIdlePowerUsage() > 0) {
                    tooltip.add(GuiText.EnergyDrain.withSuffix(": " + Platform.formatPower(entry.getIdlePowerUsage(), true)));
                }

                toolPosX = x * sectionLength + xo + sectionLength - 8;
                toolPosY = y * 18 + yo;
            }

            this.drawItem(posX, posY, entry.getDisplay());

            if (++x >= COLUMNS) {
                y++;
                x = 0;
            }
        }

        if (tooltip != null) {
            this.drawTooltip(matrixStack, toolPosX, toolPosY + 10, tooltip);
        }
    }

    public void postUpdate(NetworkStatus status) {
        this.status = status;
        this.setScrollBar();
    }

    private void setScrollBar() {
        final int size = this.status.getMachines().size();
        this.getScrollBar().setTop(39).setLeft(175).setHeight(78);
        int overflowRows = (size + (COLUMNS - 1)) / COLUMNS - ROWS;
        this.getScrollBar().setRange(0, overflowRows, 1);
    }

    @Override
    protected void renderTooltip(MatrixStack matrixStack, final ItemStack stack, final int x, final int y) {
        final Slot s = this.getSlot(x, y);

        if (s instanceof VirtualItemSlot && !stack.isEmpty()) {
            IAEItemStack myStack = null;

            try {
                final VirtualItemSlot theSlotField = (VirtualItemSlot) s;
                myStack = theSlotField.getAEStack();
            } catch (final Throwable ignore) {
            }

            if (myStack != null) {
                List<ITextComponent> currentToolTip = getTooltipFromItem(stack);

                while (currentToolTip.size() > 1) {
                    currentToolTip.remove(1);
                }

                currentToolTip.add(GuiText.Installed.withSuffix(": " + (myStack.getStackSize())));
                currentToolTip.add(GuiText.EnergyDrain
                        .withSuffix(": " + Platform.formatPowerLong(myStack.getCountRequestable(), true)));

                this.drawTooltip(matrixStack, x, y, currentToolTip);
            }
        }

        super.renderTooltip(matrixStack, stack, x, y);
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
