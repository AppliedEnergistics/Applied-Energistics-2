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

package appeng.client.gui.widgets;


import appeng.client.gui.AEBaseGui;
import net.minecraft.client.renderer.GlStateManager;


public class GuiScrollbar implements IScrollSource {

    private int displayX = 0;
    private int displayY = 0;
    private int width = 12;
    private int height = 16;
    private int pageSize = 1;

    private int maxScroll = 0;
    private int minScroll = 0;
    private int currentScroll = 0;

    public void draw(final AEBaseGui g) {
        g.bindTexture("minecraft", "gui/container/creative_inventory/tabs.png");
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        if (this.getRange() == 0) {
            g.drawTexturedModalRect(this.displayX, this.displayY, 232 + this.width, 0, this.width, 15);
        } else {
            final int offset = (this.currentScroll - this.minScroll) * (this.height - 15) / this.getRange();
            g.drawTexturedModalRect(this.displayX, offset + this.displayY, 232, 0, this.width, 15);
        }
    }

    private int getRange() {
        return this.maxScroll - this.minScroll;
    }

    public int getLeft() {
        return this.displayX;
    }

    public GuiScrollbar setLeft(final int v) {
        this.displayX = v;
        return this;
    }

    public int getTop() {
        return this.displayY;
    }

    public GuiScrollbar setTop(final int v) {
        this.displayY = v;
        return this;
    }

    public int getWidth() {
        return this.width;
    }

    public GuiScrollbar setWidth(final int v) {
        this.width = v;
        return this;
    }

    public int getHeight() {
        return this.height;
    }

    public GuiScrollbar setHeight(final int v) {
        this.height = v;
        return this;
    }

    public void setRange(final int min, final int max, final int pageSize) {
        this.minScroll = min;
        this.maxScroll = max;
        this.pageSize = pageSize;

        if (this.minScroll > this.maxScroll) {
            this.maxScroll = this.minScroll;
        }

        this.applyRange();
    }

    private void applyRange() {
        this.currentScroll = Math.max(Math.min(this.currentScroll, this.maxScroll), this.minScroll);
    }

    @Override
    public int getCurrentScroll() {
        return this.currentScroll;
    }

    public void click(final AEBaseGui aeBaseGui, final int x, final int y) {
        if (this.getRange() == 0) {
            return;
        }

        if (x > this.displayX && x <= this.displayX + this.width) {
            if (y > this.displayY && y <= this.displayY + this.height) {
                this.currentScroll = (y - this.displayY);
                this.currentScroll = this.minScroll + ((this.currentScroll * 2 * this.getRange() / this.height));
                this.currentScroll = (this.currentScroll + 1) >> 1;
                this.applyRange();
            }
        }
    }

    public void wheel(int delta) {
        delta = Math.max(Math.min(-delta, 1), -1);
        this.currentScroll += delta * this.pageSize;
        this.applyRange();
    }
}
