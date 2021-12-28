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

import javax.annotation.Nullable;

import appeng.client.Point;

/**
 * Describes the appearance of terminal screens.
 */
public class TerminalStyle {

    /**
     * The top of the terminal background right up to the first row of content.
     */
    private Blitter header;

    /**
     * The area to draw for the first row in the terminal. Usually this includes the top of the scrollbar.
     */
    private Blitter firstRow;

    /**
     * The area to repeat for every row in the terminal. Should be 16px for the item + 2px for the border in size.
     */
    private Blitter row;

    /**
     * The area to draw for the last row in the terminal. Usually this includes the top of the scrollbar.
     */
    private Blitter lastRow;

    /**
     * The area to draw at the bottom of the terminal (i.e. includes the player inventory)
     */
    private Blitter bottom;

    /**
     * If specified, limits the terminal to at most this many rows rather than using up available space.
     */
    private Integer maxRows;

    /**
     * Currently only 9 is supported here.
     */
    private int slotsPerRow;

    private boolean sortable = true;

    private boolean supportsAutoCrafting = false;

    private boolean supportsMultipleTypes = false;

    /**
     * Should the terminal show item tooltips for the network inventory even if the player has an item in their hand?
     * Useful for showing fluid tooltips when the player has a bucket in hand.
     */
    private boolean showTooltipsWithItemInHand;

    public Blitter getHeader() {
        return header;
    }

    public void setHeader(Blitter header) {
        this.header = header;
    }

    public Blitter getFirstRow() {
        return firstRow;
    }

    public void setFirstRow(Blitter firstRow) {
        this.firstRow = firstRow;
    }

    public Blitter getRow() {
        return row;
    }

    public void setRow(Blitter row) {
        this.row = row;
    }

    public Blitter getLastRow() {
        return lastRow;
    }

    public void setLastRow(Blitter lastRow) {
        this.lastRow = lastRow;
    }

    public Blitter getBottom() {
        return bottom;
    }

    public void setBottom(Blitter bottom) {
        this.bottom = bottom;
    }

    public void setMaxRows(Integer maxRows) {
        this.maxRows = maxRows;
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
        // Calculate a bounding box for the screen
        int screenWidth = header.getSrcWidth();
        screenWidth = Math.max(screenWidth, firstRow.getSrcWidth());
        screenWidth = Math.max(screenWidth, row.getSrcWidth());
        screenWidth = Math.max(screenWidth, lastRow.getSrcWidth());
        screenWidth = Math.max(screenWidth, bottom.getSrcWidth());
        return screenWidth;
    }

    public int getPossibleRows(int availableHeight) {
        return (availableHeight - header.getSrcHeight()
                - bottom.getSrcHeight()) / row.getSrcHeight();
    }

    /**
     * Gets the position of one of the network grid slots. The returned position is within the slots 1px border.
     */
    public Point getSlotPos(int row, int col) {
        int x = 7 + col * 18;

        int y = header.getSrcHeight();
        if (row > 0) {
            y += firstRow.getSrcHeight();
            y += (row - 1) * this.row.getSrcHeight();
        }

        // +1 is the margin between the bounding box of the slot and the real minecraft slot. this is due to the border
        return new Point(x, y).move(1, 1);
    }

    /**
     * @return The number of rows this terminal should display (at most). If null, the player's chosen terminal style
     *         determines the number of rows.
     */
    @Nullable
    public Integer getMaxRows() {
        return maxRows;
    }

    /**
     * Calculates the height of the screen given a number of rows to display.
     */
    public int getScreenHeight(int rows) {
        int result = header.getSrcHeight();
        result += firstRow.getSrcHeight();
        result += Math.max(0, rows - 2) * row.getSrcHeight();
        result += lastRow.getSrcHeight();
        result += bottom.getSrcHeight();
        return result;
    }

    public boolean isSupportsAutoCrafting() {
        return supportsAutoCrafting;
    }

    public void setSupportsAutoCrafting(boolean supportsAutoCrafting) {
        this.supportsAutoCrafting = supportsAutoCrafting;
    }

    public boolean isSupportsMultipleTypes() {
        return supportsMultipleTypes;
    }

    public boolean isShowTooltipsWithItemInHand() {
        return showTooltipsWithItemInHand;
    }

    public void setShowTooltipsWithItemInHand(boolean showTooltipsWithItemInHand) {
        this.showTooltipsWithItemInHand = showTooltipsWithItemInHand;
    }

    public void validate() {
        if (header == null) {
            throw new RuntimeException("terminalStyle.header is missing");
        }
        if (firstRow == null) {
            throw new RuntimeException("terminalStyle.firstRow is missing");
        }
        if (row == null) {
            throw new RuntimeException("terminalStyle.row is missing");
        }
        if (lastRow == null) {
            throw new RuntimeException("terminalStyle.lastRow is missing");
        }
        if (bottom == null) {
            throw new RuntimeException("terminalStyle.bottom is missing");
        }
    }
}
