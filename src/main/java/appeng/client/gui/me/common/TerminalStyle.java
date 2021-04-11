package appeng.client.gui.me.common;

import javax.annotation.Nullable;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;

import appeng.client.Point;
import appeng.client.gui.Blitter;

/**
 * Describes the appearance of a terminal screen.
 */
public final class TerminalStyle {

    /**
     * The top of the terminal background right up to the first row of content.
     */
    private final Blitter header;

    /**
     * The area to draw for the first row in the terminal. Usually this includes the top of the scrollbar.
     */
    private final Blitter firstRow;

    /**
     * The area to repeat for every row in the terminal. Should be 16px for the item + 2px for the border in size.
     */
    private final Blitter row;

    /**
     * The area to draw for the last row in the terminal. Usually this includes the top of the scrollbar.
     */
    private final Blitter lastRow;

    /**
     * The area to draw at the bottom of the terminal (i.e. includes the player inventory)
     */
    private final Blitter bottom;

    private final int screenWidth;

    private final Integer maxRows;

    private final int slotsPerRow;

    private final Rectangle2d searchFieldRect;

    private boolean sortByButton = true;

    private boolean supportsAutoCrafting = false;

    private StackSizeRenderer stackSizeRenderer = new StackSizeRenderer();

    /**
     * Should the terminal show item tooltips for the network inventory even if the player has an item in their hand?
     * Useful for showing fluid tooltips when the player has a bucket in hand.
     */
    private boolean showTooltipsWithItemInHand;

    public TerminalStyle(Blitter header, Blitter firstRow, Blitter row, Blitter lastRow, Blitter bottom,
            int slotsPerRow, Rectangle2d searchFieldRect, Integer maxRows) {
        this.header = header;
        this.firstRow = firstRow;
        this.row = row;
        this.lastRow = lastRow;
        this.bottom = bottom;
        this.slotsPerRow = slotsPerRow;
        this.searchFieldRect = searchFieldRect;
        this.maxRows = maxRows;

        // Calculate a bounding box for the screen
        int screenWidth = header.getSrcWidth();
        screenWidth = Math.max(screenWidth, firstRow.getSrcWidth());
        screenWidth = Math.max(screenWidth, row.getSrcWidth());
        screenWidth = Math.max(screenWidth, lastRow.getSrcWidth());
        screenWidth = Math.max(screenWidth, bottom.getSrcWidth());
        this.screenWidth = screenWidth;
    }

    public Blitter getHeader() {
        return header;
    }

    public Blitter getFirstRow() {
        return firstRow;
    }

    public Blitter getRow() {
        return row;
    }

    public Blitter getLastRow() {
        return lastRow;
    }

    public Blitter getBottom() {
        return bottom;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getSlotsPerRow() {
        return slotsPerRow;
    }

    public int getPossibleRows(int availableHeight) {
        return (availableHeight - header.getSrcHeight()
                - bottom.getSrcHeight()) / row.getSrcHeight();
    }

    /**
     * Gets the position of one of the network grid slots. The returned position is within the slots 1px border.
     */
    public Point getSlotPos(int row, int col) {
        int x = 8 + col * 18;

        int y = header.getSrcHeight();
        if (row > 0) {
            y += firstRow.getSrcHeight();
            y += (row - 1) * this.row.getSrcHeight();
        }

        // +1 is the margin between the bounding box of the slot and the real minecraft slot. this is due to the border
        return new Point(x, y).move(1, 1);
    }

    /**
     * The bounding box of the search field in the background.
     */
    public Rectangle2d getSearchFieldRect() {
        return searchFieldRect;
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

    /**
     * Creates a screen factory from a screen constructor that takes a terminal style as the first argument.
     */
    public <M extends Container, U extends Screen & IHasContainer<M>> ScreenManager.IScreenFactory<M, U> factory(
            StyledScreenFactory<M, U> factory) {
        return (container, playerInv, title) -> factory.create(this, container, playerInv, title);
    }

    public boolean hasSortByButton() {
        return sortByButton;
    }

    public TerminalStyle setSortByButton(boolean visible) {
        this.sortByButton = visible;
        return this;
    }

    public boolean isSupportsAutoCrafting() {
        return supportsAutoCrafting;
    }

    public TerminalStyle setSupportsAutoCrafting(boolean enabled) {
        this.supportsAutoCrafting = enabled;
        return this;
    }

    public boolean isShowTooltipsWithItemInHand() {
        return showTooltipsWithItemInHand;
    }

    public TerminalStyle setShowTooltipsWithItemInHand(boolean enabled) {
        this.showTooltipsWithItemInHand = enabled;
        return this;
    }

    public StackSizeRenderer getStackSizeRenderer() {
        return stackSizeRenderer;
    }

    public TerminalStyle setStackSizeRenderer(StackSizeRenderer renderer) {
        this.stackSizeRenderer = renderer;
        return this;
    }

    @FunctionalInterface
    public interface StyledScreenFactory<T extends Container, U extends Screen & IHasContainer<T>> {
        U create(TerminalStyle style, T t, PlayerInventory pi, ITextComponent title);
    }

}
