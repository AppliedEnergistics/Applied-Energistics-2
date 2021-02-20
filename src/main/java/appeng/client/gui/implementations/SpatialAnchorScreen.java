/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.CommonButtons;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.me.ItemRepo;
import appeng.container.implementations.SpatialAnchorContainer;
import appeng.core.localization.GuiText;
import appeng.util.Platform;

public class SpatialAnchorScreen extends AEBaseScreen<SpatialAnchorContainer> implements ISortSource {

    private static final int SCROLL_AREA_WIDTH = 160;
    private static final int SCROLL_AREA_HEIGHT = 78;
    private static final int SCROLL_AREA_OFFSET_X = 0;
    private static final int SCROLL_AREA_OFFSET_Y = 0;

    private static final int SCROLL_COLUMNS = 3;
    private static final int SCROLL_ROWS = 4;

    private static final int SCROLL_ITEM_WIDTH = SCROLL_AREA_WIDTH / SCROLL_COLUMNS;
    private static final int SCROLL_ITEM_HEIGHT = SCROLL_AREA_HEIGHT / SCROLL_ROWS;
    private static final int SCROLL_ITEM_PADDING_X = 10;
    private static final int SCROLL_ITEM_PADDING_Y = 3;

    private final ItemRepo repo;
    private final int rows = 4;
    private int tooltip = -1;

    public SpatialAnchorScreen(SpatialAnchorContainer container, PlayerInventory playerInventory,
            ITextComponent title) {
        super(container, playerInventory, title);
        this.ySize = 162;
        this.xSize = 195;

        final Scrollbar scrollbar = new Scrollbar();
        this.setScrollBar(scrollbar);

        this.repo = new ItemRepo(scrollbar, this);
        this.repo.setRowSize(5);
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
        for (int z = 0; z <= SCROLL_ROWS * SCROLL_COLUMNS; z++) {
            final int minX = gx + 9 + x * SCROLL_ITEM_WIDTH;
            final int minY = gy + 40 + y * SCROLL_ITEM_HEIGHT;

            if (minX + SCROLL_ITEM_PADDING_X < mouseX && minX + SCROLL_ITEM_WIDTH - SCROLL_ITEM_PADDING_X > mouseX) {
                if (minY + SCROLL_ITEM_PADDING_Y < mouseY
                        && minY + SCROLL_ITEM_HEIGHT - SCROLL_ITEM_PADDING_Y > mouseY) {
                    this.tooltip = z;
                    break;
                }
            }

            x++;

            if (x >= SCROLL_COLUMNS) {
                y++;
                x = 0;
            }
        }

        super.render(matrixStack, mouseX, mouseY, btn);
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {

        this.font.drawString(matrixStack, this.getGuiDisplayName(GuiText.SpatialAnchor.text()).getString(), 8, 6,
                4210752);

        String usedPower = GuiText.SpatialAnchorUsedPower
                .text(Platform.formatPowerLong(this.container.powerConsumption * 100, true)).getString();
        this.font.drawString(matrixStack, usedPower, 13, 16, 4210752);
        this.font.drawString(matrixStack,
                GuiText.SpatialAnchorLoadedChunks.text(this.container.loadedChunks).getString(), 13, 26, 4210752);

        this.font.drawString(matrixStack, this.getGuiDisplayName(GuiText.SpatialAnchorStatistics.text()).getString(), 8,
                151 - 30, 4210752);

        this.font.drawString(matrixStack,
                GuiText.SpatialAnchorAllLoaded.text(this.container.allLoadedChunks, this.container.allLoadedWorlds)
                        .getString(),
                13, 151 - 20, 4210752);

        this.font.drawString(matrixStack,
                GuiText.SpatialAnchorAll.text(this.container.allChunks, this.container.allWorlds).getString(), 13,
                151 - 10, 4210752);

        int x = 0;
        int y = 0;
        final int xo = 8;
        final int yo = 42;
        final int viewStart = 0;
        final int viewEnd = viewStart + SCROLL_COLUMNS * SCROLL_ROWS;

        String toolTip = "";
        int toolPosX = 0;
        int toolPosY = 0;

        for (int z = viewStart; z < Math.min(viewEnd, this.repo.size()); z++) {
            final IAEItemStack refStack = this.repo.getReferenceItem(z);
            if (refStack != null) {
                ChunkPos chunkPos = new ChunkPos(refStack.getDefinition().getOrCreateTag().getLong("chunk"));

                RenderSystem.pushMatrix();
                RenderSystem.scalef(0.5f, 0.5f, 0.5f);

                String str1 = "x " + chunkPos.x;
                String str2 = "z " + chunkPos.z;

                final int w1 = this.font.getStringWidth(str1);
                this.font.drawString(matrixStack, str1,
                        (int) ((x * SCROLL_ITEM_WIDTH + xo + SCROLL_ITEM_WIDTH - SCROLL_ITEM_HEIGHT - (w1 * 0.5)) * 2),
                        (y * SCROLL_ITEM_HEIGHT + yo + 6) * 2 - 5,
                        4210752);

                final int w2 = this.font.getStringWidth(str2);
                this.font.drawString(matrixStack, str2,
                        (int) ((x * SCROLL_ITEM_WIDTH + xo + SCROLL_ITEM_WIDTH - SCROLL_ITEM_HEIGHT - (w2 * 0.5)) * 2),
                        (y * SCROLL_ITEM_HEIGHT + yo + 6) * 2 + 5,
                        4210752);

                RenderSystem.popMatrix();
                final int posX = x * SCROLL_ITEM_WIDTH + xo + SCROLL_ITEM_WIDTH - 18;
                final int posY = y * SCROLL_ITEM_HEIGHT + yo;

                if (this.tooltip == z - viewStart) {
                    toolTip = "Chunk " + chunkPos.toString();

                    toolTip += ('\n' + "Block " +
                            chunkPos.getXStart() + "/" + chunkPos.getZStart() + " to " + chunkPos.getXEnd() + "/"
                            + chunkPos.getZEnd());

                    toolPosX = x * SCROLL_ITEM_WIDTH + xo + SCROLL_ITEM_WIDTH - 8;
                    toolPosY = y * SCROLL_ITEM_HEIGHT + yo;
                }

                this.drawItem(posX, posY, refStack.asItemStackRepresentation());

                x++;

                if (x >= SCROLL_COLUMNS) {
                    y++;
                    x = 0;
                }
            }
        }

        if (this.tooltip >= 0 && toolTip.length() > 0) {
            this.drawTooltip(matrixStack, toolPosX, toolPosY + 10, new StringTextComponent(toolTip));
        }

    }

    @Override
    public void drawBG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY, float partialTicks) {
        this.bindTexture("guis/spatialanchor.png");
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
        this.getScrollBar().setRange(0, (size + SCROLL_ROWS) / SCROLL_COLUMNS - this.rows, 1);
    }

    @Override
    public SortOrder getSortBy() {
        return SortOrder.AMOUNT;
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
