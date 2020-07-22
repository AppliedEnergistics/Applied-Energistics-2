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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Joiner;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import org.apache.commons.lang3.time.DurationFormatUtils;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEColor;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.gui.widgets.Scrollbar;
import appeng.container.implementations.CraftingCPUContainer;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.util.Platform;
import appeng.util.ReadableNumberConverter;

public class CraftingCPUScreen<T extends CraftingCPUContainer> extends AEBaseScreen<T> implements ISortSource {
    private static final int GUI_HEIGHT = 184;
    private static final int GUI_WIDTH = 238;

    private static final int DISPLAYED_ROWS = 6;

    private static final int TEXT_COLOR = 0x404040;
    private static final int BACKGROUND_ALPHA = 0x5A000000;

    private static final int SECTION_LENGTH = 67;

    private static final int SCROLLBAR_TOP = 19;
    private static final int SCROLLBAR_LEFT = 218;
    private static final int SCROLLBAR_HEIGHT = 137;

    private static final int CANCEL_LEFT_OFFSET = 163;
    private static final int CANCEL_TOP_OFFSET = 25;
    private static final int CANCEL_HEIGHT = 20;
    private static final int CANCEL_WIDTH = 50;

    private static final int TITLE_TOP_OFFSET = 7;
    private static final int TITLE_LEFT_OFFSET = 8;

    private static final int ITEMSTACK_LEFT_OFFSET = 9;
    private static final int ITEMSTACK_TOP_OFFSET = 22;

    private IItemList<IAEItemStack> storage = Api.instance().storage().getStorageChannel(IItemStorageChannel.class)
            .createList();
    private IItemList<IAEItemStack> active = Api.instance().storage().getStorageChannel(IItemStorageChannel.class)
            .createList();
    private IItemList<IAEItemStack> pending = Api.instance().storage().getStorageChannel(IItemStorageChannel.class)
            .createList();

    private List<IAEItemStack> visual = new ArrayList<>();
    private Button cancel;
    private int tooltip = -1;

    public CraftingCPUScreen(T container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.ySize = GUI_HEIGHT;
        this.xSize = GUI_WIDTH;

        final Scrollbar scrollbar = new Scrollbar();
        this.setScrollBar(scrollbar);
    }

    public void clearItems() {
        this.storage = Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
        this.active = Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
        this.pending = Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
        this.visual = new ArrayList<>();
    }

    private void cancel() {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("TileCrafting.Cancel", "Cancel"));
    }

    @Override
    public void init() {
        super.init();
        this.setScrollBar();
        this.cancel = new Button(this.guiLeft + CANCEL_LEFT_OFFSET, this.guiTop + this.ySize - CANCEL_TOP_OFFSET,
                CANCEL_WIDTH, CANCEL_HEIGHT, GuiText.Cancel.textComponent(), btn -> cancel());
        this.addButton(this.cancel);
    }

    private void setScrollBar() {
        final int size = this.visual.size();

        this.getScrollBar().setTop(SCROLLBAR_TOP).setLeft(SCROLLBAR_LEFT).setHeight(SCROLLBAR_HEIGHT);
        this.getScrollBar().setRange(0, (size + 2) / 3 - DISPLAYED_ROWS, 1);
    }

    @Override
    public void render(MatrixStack matrixStack, final int mouseX, final int mouseY, final float btn) {
        this.cancel.active = !this.visual.isEmpty();

        final int gx = (this.width - this.xSize) / 2;
        final int gy = (this.height - this.ySize) / 2;

        this.tooltip = -1;

        final int offY = 23;
        int y = 0;
        int x = 0;
        for (int z = 0; z <= 4 * 5; z++) {
            final int minX = gx + 9 + x * 67;
            final int minY = gy + 22 + y * offY;

            if (minX < mouseX && minX + 67 > mouseX) {
                if (minY < mouseY && minY + offY - 2 > mouseY) {
                    this.tooltip = z;
                    break;
                }
            }

            x++;

            if (x > 2) {
                y++;
                x = 0;
            }
        }

        super.render(matrixStack, mouseX, mouseY, btn);
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        String title = this.getGuiDisplayName(GuiText.CraftingStatus.textComponent()).getString();

        if (this.container.getEstimatedTime() > 0 && !this.visual.isEmpty()) {
            final long etaInMilliseconds = TimeUnit.MILLISECONDS.convert(this.container.getEstimatedTime(),
                    TimeUnit.NANOSECONDS);
            final String etaTimeText = DurationFormatUtils.formatDuration(etaInMilliseconds,
                    GuiText.ETAFormat.getLocal());
            title += " - " + etaTimeText;
        }

        this.font.drawString(matrixStack, title, TITLE_LEFT_OFFSET, TITLE_TOP_OFFSET, TEXT_COLOR);

        int x = 0;
        int y = 0;
        final int viewStart = this.getScrollBar().getCurrentScroll() * 3;
        final int viewEnd = viewStart + 3 * 6;

        String dspToolTip = "";
        final List<String> lineList = new ArrayList<>();
        int toolPosX = 0;
        int toolPosY = 0;

        final int offY = 23;

        final ReadableNumberConverter converter = ReadableNumberConverter.INSTANCE;
        for (int z = viewStart; z < Math.min(viewEnd, this.visual.size()); z++) {
            final IAEItemStack refStack = this.visual.get(z);// repo.getReferenceItem( z );
            if (refStack != null) {
                RenderSystem.pushMatrix();
                RenderSystem.scalef(0.5f, 0.5f, 0.5f);

                final IAEItemStack stored = this.storage.findPrecise(refStack);
                final IAEItemStack activeStack = this.active.findPrecise(refStack);
                final IAEItemStack pendingStack = this.pending.findPrecise(refStack);

                int lines = 0;

                if (stored != null && stored.getStackSize() > 0) {
                    lines++;
                }
                boolean active = false;
                if (activeStack != null && activeStack.getStackSize() > 0) {
                    lines++;
                    active = true;
                }
                boolean scheduled = false;
                if (pendingStack != null && pendingStack.getStackSize() > 0) {
                    lines++;
                    scheduled = true;
                }

                if (AEConfig.instance().isUseColoredCraftingStatus() && (active || scheduled)) {
                    final int bgColor = (active ? AEColor.GREEN.blackVariant : AEColor.YELLOW.blackVariant)
                            | BACKGROUND_ALPHA;
                    final int startX = (x * (1 + SECTION_LENGTH) + ITEMSTACK_LEFT_OFFSET) * 2;
                    final int startY = ((y * offY + ITEMSTACK_TOP_OFFSET) - 3) * 2;
                    fill(matrixStack, startX, startY, startX + (SECTION_LENGTH * 2), startY + (offY * 2) - 2, bgColor);
                }

                final int negY = ((lines - 1) * 5) / 2;
                int downY = 0;

                if (stored != null && stored.getStackSize() > 0) {
                    final String str = GuiText.Stored.getLocal() + ": "
                            + converter.toWideReadableForm(stored.getStackSize());
                    final int w = 4 + this.font.getStringWidth(str);
                    this.font.drawString(matrixStack, str,
                            (int) ((x * (1 + SECTION_LENGTH) + ITEMSTACK_LEFT_OFFSET + SECTION_LENGTH - 19 - (w * 0.5))
                                    * 2),
                            (y * offY + ITEMSTACK_TOP_OFFSET + 6 - negY + downY) * 2, TEXT_COLOR);

                    if (this.tooltip == z - viewStart) {
                        lineList.add(GuiText.Stored.getLocal() + ": " + Long.toString(stored.getStackSize()));
                    }

                    downY += 5;
                }

                if (activeStack != null && activeStack.getStackSize() > 0) {
                    final String str = GuiText.Crafting.getLocal() + ": "
                            + converter.toWideReadableForm(activeStack.getStackSize());
                    final int w = 4 + this.font.getStringWidth(str);

                    this.font.drawString(matrixStack, str,
                            (int) ((x * (1 + SECTION_LENGTH) + ITEMSTACK_LEFT_OFFSET + SECTION_LENGTH - 19 - (w * 0.5))
                                    * 2),
                            (y * offY + ITEMSTACK_TOP_OFFSET + 6 - negY + downY) * 2, TEXT_COLOR);

                    if (this.tooltip == z - viewStart) {
                        lineList.add(GuiText.Crafting.getLocal() + ": " + Long.toString(activeStack.getStackSize()));
                    }

                    downY += 5;
                }

                if (pendingStack != null && pendingStack.getStackSize() > 0) {
                    final String str = GuiText.Scheduled.getLocal() + ": "
                            + converter.toWideReadableForm(pendingStack.getStackSize());
                    final int w = 4 + this.font.getStringWidth(str);

                    this.font.drawString(matrixStack, str,
                            (int) ((x * (1 + SECTION_LENGTH) + ITEMSTACK_LEFT_OFFSET + SECTION_LENGTH - 19 - (w * 0.5))
                                    * 2),
                            (y * offY + ITEMSTACK_TOP_OFFSET + 6 - negY + downY) * 2, TEXT_COLOR);

                    if (this.tooltip == z - viewStart) {
                        lineList.add(GuiText.Scheduled.getLocal() + ": " + Long.toString(pendingStack.getStackSize()));
                    }
                }

                RenderSystem.popMatrix();
                final int posX = x * (1 + SECTION_LENGTH) + ITEMSTACK_LEFT_OFFSET + SECTION_LENGTH - 19;
                final int posY = y * offY + ITEMSTACK_TOP_OFFSET;

                final ItemStack is = refStack.asItemStackRepresentation();

                if (this.tooltip == z - viewStart) {
                    dspToolTip = Platform.getItemDisplayName(refStack).getString();

                    if (lineList.size() > 0) {
                        dspToolTip = dspToolTip + '\n' + Joiner.on("\n").join(lineList);
                    }

                    toolPosX = x * (1 + SECTION_LENGTH) + ITEMSTACK_LEFT_OFFSET + SECTION_LENGTH - 8;
                    toolPosY = y * offY + ITEMSTACK_TOP_OFFSET;
                }

                this.drawItem(posX, posY, is);

                x++;

                if (x > 2) {
                    y++;
                    x = 0;
                }
            }
        }

        if (this.tooltip >= 0 && !dspToolTip.isEmpty()) {
            this.drawTooltip(matrixStack, toolPosX, toolPosY + 10, new StringTextComponent(dspToolTip));
        }
    }

    @Override
    public void drawBG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        this.bindTexture("guis/craftingcpu.png");
        GuiUtils.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize, getBlitOffset());
    }

    public void postUpdate(final List<IAEItemStack> list, final byte ref) {
        switch (ref) {
            case 0:
                for (final IAEItemStack l : list) {
                    this.handleInput(this.storage, l);
                }
                break;

            case 1:
                for (final IAEItemStack l : list) {
                    this.handleInput(this.active, l);
                }
                break;

            case 2:
                for (final IAEItemStack l : list) {
                    this.handleInput(this.pending, l);
                }
                break;
        }

        for (final IAEItemStack l : list) {
            final long amt = this.getTotal(l);

            if (amt <= 0) {
                this.deleteVisualStack(l);
            } else {
                final IAEItemStack is = this.findVisualStack(l);
                is.setStackSize(amt);
            }
        }

        this.setScrollBar();
    }

    private void handleInput(final IItemList<IAEItemStack> s, final IAEItemStack l) {
        IAEItemStack a = s.findPrecise(l);

        if (l.getStackSize() <= 0) {
            if (a != null) {
                a.reset();
            }
        } else {
            if (a == null) {
                s.add(l.copy());
                a = s.findPrecise(l);
            }

            if (a != null) {
                a.setStackSize(l.getStackSize());
            }
        }
    }

    private long getTotal(final IAEItemStack is) {
        final IAEItemStack a = this.storage.findPrecise(is);
        final IAEItemStack b = this.active.findPrecise(is);
        final IAEItemStack c = this.pending.findPrecise(is);

        long total = 0;

        if (a != null) {
            total += a.getStackSize();
        }

        if (b != null) {
            total += b.getStackSize();
        }

        if (c != null) {
            total += c.getStackSize();
        }

        return total;
    }

    private void deleteVisualStack(final IAEItemStack l) {
        final Iterator<IAEItemStack> i = this.visual.iterator();

        while (i.hasNext()) {
            final IAEItemStack o = i.next();
            if (o.equals(l)) {
                i.remove();
                return;
            }
        }
    }

    private IAEItemStack findVisualStack(final IAEItemStack l) {
        for (final IAEItemStack o : this.visual) {
            if (o.equals(l)) {
                return o;
            }
        }

        final IAEItemStack stack = l.copy();
        this.visual.add(stack);

        return stack;
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
