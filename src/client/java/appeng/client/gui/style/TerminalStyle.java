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

package appeng.client.gui.style;

import appeng.client.Point;
import appeng.client.gui.widgets.PanelBlitter;

/**
 * Describes the appearance of terminal screens.
 * <p>
 * The window of a terminal consists of an upper part containing the header and the grid of network inventory slots, and
 * a lower part (i.e. the player inventory). The upper part spans the full width of the screen to make room for the
 * scrollbar, while the lower part can be narrower.
 */
public class TerminalStyle {
    /**
     * The height of each row of the network inventory grid.
     */
    public static final int ROW_HEIGHT = 18;

    /**
     * The width of the screen, including the scrollbar next to the network inventory grid.
     */
    private int screenWidth = 195;

    /**
     * The width of the window below the network inventory grid.
     */
    private int bottomWidth = 176;

    /**
     * The height of the area above the first row of the network inventory grid.
     */
    private int headerHeight = 17;

    /**
     * How far the upper part of the window extends below the network inventory grid.
     */
    private int gridBottomMargin = 9;

    /**
     * The height of the area below the network inventory grid (i.e. includes the player inventory).
     */
    private int bottomHeight;

    /**
     * Currently only 9 is supported here.
     */
    private int slotsPerRow;

    private boolean sortable = true;

    private boolean supportsAutoCrafting = false;

    /**
     * Should the terminal show item tooltips for the network inventory even if the player has an item in their hand?
     * Useful for showing fluid tooltips when the player has a bucket in hand.
     */
    private boolean showTooltipsWithItemInHand;

    public int getHeaderHeight() {
        return headerHeight;
    }

    public int getBottomHeight() {
        return bottomHeight;
    }

    public int getSlotsPerRow() {
        return slotsPerRow;
    }

    public void setSlotsPerRow(int slotsPerRow) {
        this.slotsPerRow = slotsPerRow;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getPossibleRows(int availableHeight) {
        return (availableHeight - headerHeight - bottomHeight) / ROW_HEIGHT;
    }

    /**
     * Gets the position of one of the network grid slots. The returned position is within the slots 1px border.
     */
    public Point getSlotPos(int row, int col) {
        int x = 7 + col * 18;
        int y = headerHeight + row * ROW_HEIGHT;

        // +1 is the margin between the bounding box of the slot and the real minecraft slot. this is due to the border
        return new Point(x, y).move(1, 1);
    }

    /**
     * Calculates the height of the screen given a number of rows to display.
     */
    public int getScreenHeight(int rows) {
        return headerHeight + Math.max(2, rows) * ROW_HEIGHT + bottomHeight;
    }

    /**
     * Adds the window shape for the given number of rows.
     */
    public void addToPanel(PanelBlitter panel, int rows) {
        var gridBottom = headerHeight + Math.max(2, rows) * ROW_HEIGHT;
        panel.addBounds(0, 0, screenWidth, gridBottom + gridBottomMargin);
        panel.addBounds(0, 0, bottomWidth, getScreenHeight(rows));
    }

    public boolean isSupportsAutoCrafting() {
        return supportsAutoCrafting;
    }

    public void setSupportsAutoCrafting(boolean supportsAutoCrafting) {
        this.supportsAutoCrafting = supportsAutoCrafting;
    }

    public boolean isShowTooltipsWithItemInHand() {
        return showTooltipsWithItemInHand;
    }

    public void setShowTooltipsWithItemInHand(boolean showTooltipsWithItemInHand) {
        this.showTooltipsWithItemInHand = showTooltipsWithItemInHand;
    }

    public void validate() {
        if (bottomHeight <= 0) {
            throw new RuntimeException("terminalStyle.bottomHeight is missing");
        }
    }
}
