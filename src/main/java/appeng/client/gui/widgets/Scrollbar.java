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

import java.time.Duration;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import appeng.client.gui.AEBaseScreen;

/**
 * Implements a vertical scrollbar using Vanilla's scrollbar handle texture from
 * the creative tab.
 * <p>
 * It is expected that the background of the UI contains a pre-baked scrollbar
 * track border, and that the exact rectangle of that track is set on this
 * object via {@link #setLeft(int)}, {@link #setTop(int)} and
 * {@link #setHeight(int)}. While the width of the track can also be set, the
 * drawn handle will use vanilla's sprite width (see {@link #HANDLE_WIDTH}.
 */
public class Scrollbar extends DrawableHelper implements IScrollSource {

    /**
     * Width of the scrollbar handle sprite in the source texture.
     */
    private static final int HANDLE_WIDTH = 12;

    /**
     * Height of the scrollbar handle sprite in the source texture.
     */
    private static final int HANDLE_HEIGHT = 15;

    /**
     * Texture containing the scrollbar handle sprites.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft",
            "textures/gui/container/creative_inventory/tabs.png");

    /**
     * Rectangle in the source texture that contains the sprite for an enabled
     * handle.
     */
    private static final Rectangle2d ENABLED = new Rectangle2d(232, 0, HANDLE_WIDTH, HANDLE_HEIGHT);

    /**
     * Rectangle in the source texture that contains the sprite for a disabled
     * handle.
     */
    private static final Rectangle2d DISABLED = new Rectangle2d(232 + HANDLE_WIDTH, 0, HANDLE_WIDTH, HANDLE_HEIGHT);

    /**
     * The screen x-coordinate of the scrollbar's inner track.
     */
    private int displayX = 0;

    /**
     * The screen y-coordinate of the scrollbar's inner track.
     */
    private int displayY = 0;

    /**
     * The inner width of the scrollbar track.
     */
    private int width = HANDLE_WIDTH;

    /**
     * The inner height of the scrollbar track.
     */
    private int height = 16;
    private int pageSize = 1;

    private int maxScroll = 0;
    private int minScroll = 0;
    private int currentScroll = 0;

    /**
     * True if the scrollbar's handle is currently being dragged.
     */
    private boolean dragging;
    /**
     * The y-coordinate relative to the upper edge of the scrollbar handle, where
     * the user pressed the mouse button to drag. While dragging, this is applied as
     * an offset to the effective scrollbar position.
     */
    private int dragYOffset;

    private final EventRepeater eventRepeater = new EventRepeater(Duration.ofMillis(250), Duration.ofMillis(150));

    /**
     * Draws the handle of the scrollbar.
     * <p>
     * The GUI is assumed to already contain a prebaked scrollbar track in its
     * background.
     */
    public void draw(MatrixStack matrices, final AEBaseScreen<?> g) {
        setBlitOffset(g.getBlitOffset());

        // Draw the track (nice for debugging)
        // fill(matrices, displayX, displayY, this.displayX + width, this.displayY +
        // height, 0xffff0000);

        g.bindTexture(TEXTURE);

        int yOffset;
        Rectangle2d sourceRect;
        if (this.getRange() == 0) {
            yOffset = 0;
            sourceRect = DISABLED;
        } else {
            yOffset = getHandleYOffset();
            sourceRect = ENABLED;
        }

        blit(matrices, this.displayX, this.displayY + yOffset, sourceRect.getX(), sourceRect.getY(),
                sourceRect.getWidth(), sourceRect.getHeight());
    }

    /**
     * Returns the y-position of the scrollbar handle in relation to the upper edge
     * of the scrollbar's track.
     */
    private int getHandleYOffset() {
        if (getRange() == 0) {
            return 0;
        }
        int availableHeight = this.height - HANDLE_HEIGHT;
        return (this.currentScroll - this.minScroll) * availableHeight / this.getRange();
    }

    private int getRange() {
        return this.maxScroll - this.minScroll;
    }

    public int getLeft() {
        return this.displayX;
    }

    public Scrollbar setLeft(final int v) {
        this.displayX = v;
        return this;
    }

    public int getTop() {
        return this.displayY;
    }

    public Scrollbar setTop(final int v) {
        this.displayY = v;
        return this;
    }

    public int getWidth() {
        return this.width;
    }

    public Scrollbar setWidth(final int v) {
        this.width = v;
        return this;
    }

    public int getHeight() {
        return this.height;
    }

    public Scrollbar setHeight(final int v) {
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

    public boolean mouseDown(double x, double y) {
        this.dragging = false;

        // Clicks to the left or right of the scrollbar don't do anything
        if (x < displayX || x >= displayX + width) {
            return false;
        }

        // Clicks to the top or bottom don't do anything either
        int relY = (int) Math.round(y - displayY);
        if (relY < 0 || relY >= height) {
            return false;
        }

        // Do nothing when there's no range, but swallow the event
        if (getRange() == 0) {
            return true;
        }

        int handleYOffset = getHandleYOffset();

        if (relY < handleYOffset) {
            // Clicks above the handle will page up, repeatedly
            pageUp();
            eventRepeater.repeat(this::pageUp);

        } else if (relY < handleYOffset + HANDLE_HEIGHT) {
            // Clicks on the handle will initiate dragging it
            this.dragging = true;
            this.dragYOffset = relY - handleYOffset;
        } else {
            // Clicks below the handle will page down, repeatedly
            pageDown();
            eventRepeater.repeat(this::pageDown);
        }

        return true;
    }

    public boolean mouseUp(double x, double y) {
        this.dragging = false;
        this.eventRepeater.stop();
        return false;
    }

    public void mouseDragged(double x, double y) {
        if (this.getRange() == 0 || !this.dragging || this.eventRepeater.isRepeating()) {
            return;
        }

        // Compute the position of the mouse (adjusted for where it grabbed the handle,
        // so as if it grabbed
        // the upper edge of it) within the scrollable area of the track (minus the
        // handle height).
        double handleUpperEdgeY = y - this.displayY - this.dragYOffset;
        double availableHeight = this.height - HANDLE_HEIGHT;
        double position = MathHelper.clamp(handleUpperEdgeY / availableHeight, 0.0, 1.0);

        this.currentScroll = this.minScroll + (int) Math.round(position * this.getRange());
        this.applyRange();
    }

    public void wheel(double delta) {
        // Do nothing when there's no range
        if (getRange() == 0) {
            return;
        }

        delta = Math.max(Math.min(-delta, 1), -1);
        this.currentScroll += delta * this.pageSize;
        this.applyRange();
    }

    /**
     * Ticks the scrollbar for the purposes of input-repeats (since mouse-downs are
     * not repeat-triggered), used to repeatedly page-up or page-down when the mouse
     * is held in the area above or below the scrollbar handle.
     */
    public void tick() {
        this.eventRepeater.tick();
    }

    private void pageUp() {
        this.currentScroll -= this.pageSize;
        this.applyRange();
    }

    private void pageDown() {
        this.currentScroll += this.pageSize;
        this.applyRange();
    }

}
