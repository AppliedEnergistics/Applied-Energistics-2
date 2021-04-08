package appeng.client.gui.me.crafting;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import appeng.client.gui.AEBaseScreen;

/**
 * Renders a 3x5 table where each cell displays an item and some text next to it.
 */
public abstract class AbstractTableRenderer<T> {

    private static final int WIDTH = 203;
    private static final int HEIGHT = 137;

    private static final int CELL_WIDTH = 67;
    private static final int CELL_HEIGHT = 22;

    private static final int ROWS = 5;
    private static final int COLS = 3;

    // This border is only shown in-between cells, not around
    private static final int CELL_BORDER = 1;

    private static final int LINE_SPACING = 1;

    private static final float TEXT_SCALE = 0.5f;
    private static final float INV_TEXT_SCALE = 2.0f;

    protected final AEBaseScreen<?> screen;
    private final FontRenderer fontRenderer;
    private final float lineHeight;
    private final int x;
    private final int y;

    public AbstractTableRenderer(AEBaseScreen<?> screen, int x, int y) {
        this.screen = screen;
        this.x = x;
        this.y = y;
        this.fontRenderer = Minecraft.getInstance().fontRenderer;
        this.lineHeight = this.fontRenderer.FONT_HEIGHT * TEXT_SCALE;
    }

    public final void render(MatrixStack matrixStack, int mouseX, int mouseY, List<T> entries, int scrollOffset) {
        mouseX -= screen.getGuiLeft();
        mouseY -= screen.getGuiTop();

        List<ITextComponent> tooltipLines = null;
        int tooltipX = 0;
        int tooltipY = 0;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int i = (row + scrollOffset) * COLS + col;
                if (i >= entries.size()) {
                    break;
                }

                T entry = entries.get(i);

                int cellX = x + col * (CELL_WIDTH + CELL_BORDER);
                int cellY = y + row * (CELL_HEIGHT + CELL_BORDER);

                int background = getEntryBackgroundColor(entry);
                if (background != 0) {
                    AbstractGui.fill(matrixStack, cellX, cellY, cellX + CELL_WIDTH, cellY + CELL_HEIGHT, background);
                }

                List<String> lines = getEntryDescription(entry);

                float textHeight = lines.size() * lineHeight;
                if (lines.size() > 1) {
                    textHeight += (lines.size() - 1) * LINE_SPACING;
                }

                int rightEdge = cellX + CELL_WIDTH;
                // Center text vertically
                int itemX = rightEdge - 19;
                float textY = Math.round(cellY + (CELL_HEIGHT - textHeight) / 2.0f);

                matrixStack.push();
                matrixStack.scale(TEXT_SCALE, TEXT_SCALE, 1.0f);
                for (String line : lines) {
                    final int w = fontRenderer.getStringWidth(line);
                    fontRenderer.drawString(matrixStack, line,
                            (int) ((itemX - 2 - (w * TEXT_SCALE)) * INV_TEXT_SCALE),
                            textY * INV_TEXT_SCALE, AEBaseScreen.COLOR_DARK_GRAY);
                    textY += lineHeight + LINE_SPACING;
                }
                matrixStack.pop();

                ItemStack is = getEntryItem(entry);

                int itemY = cellY + (CELL_HEIGHT - 16) / 2;
                screen.drawItem(itemX, itemY, is);

                int overlay = getEntryOverlayColor(entry);
                if (overlay != 0) {
                    AbstractGui.fill(matrixStack, cellX, cellY, cellX + CELL_WIDTH, cellY + CELL_HEIGHT, overlay);
                }

                if (mouseX >= cellX && mouseX <= cellX + CELL_WIDTH) {
                    if (mouseY >= cellY && mouseY <= cellY + CELL_HEIGHT) {
                        tooltipLines = getEntryTooltip(entry);
                        tooltipX = cellX + CELL_WIDTH - 8;
                        tooltipY = itemY + 8;
                    }
                }
            }
        }

        if (tooltipLines != null) {
            screen.drawTooltip(matrixStack, tooltipX, tooltipY, tooltipLines);
        }
    }

    /**
     * @return The number of rows that are off-screen given a number of entries.
     */
    public static int getScrollableRows(int size) {
        return (size + COLS - 1) / COLS - ROWS;
    }

    /**
     * Implement in subclass to determine the text to show next to an entry.
     */
    protected abstract List<String> getEntryDescription(T entry);

    /**
     * Get the item to show for an entry.
     */
    protected abstract ItemStack getEntryItem(T entry);

    /**
     * Get the tooltip lines to show for an entry.
     */
    protected abstract List<ITextComponent> getEntryTooltip(T entry);

    /**
     * Override and return a color to draw a colored rectangle behind an entry. Return 0 to not draw a rectangle.
     */
    protected int getEntryBackgroundColor(T entry) {
        return 0;
    }

    /**
     * Override and return a color to draw a colored rectangle above an entry. Return 0 to not draw a rectangle.
     */
    protected int getEntryOverlayColor(T entry) {
        return 0;
    }

}
