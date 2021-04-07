package appeng.client.gui.me.crafting;

import appeng.client.gui.AEBaseScreen;
import appeng.container.me.crafting.CraftingPlanSummary;
import appeng.container.me.crafting.CraftingPlanSummaryEntry;
import appeng.core.localization.GuiText;
import appeng.util.ReadableNumberConverter;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the crafting job details in a 3x5 Grid
 */
public class CraftingPlanTable {

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

    private final AEBaseScreen<?> screen;
    private final FontRenderer fontRenderer;
    private final float lineHeight;
    private final int x;
    private final int y;

    public CraftingPlanTable(AEBaseScreen<?> screen, int x, int y) {
        this.screen = screen;
        this.x = x;
        this.y = y;
        this.fontRenderer = Minecraft.getInstance().fontRenderer;
        this.lineHeight = this.fontRenderer.FONT_HEIGHT * TEXT_SCALE;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, CraftingPlanSummary plan, int scrollOffset) {
        List<CraftingPlanSummaryEntry> entries = plan.getEntries();

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

                CraftingPlanSummaryEntry entry = entries.get(i);

                int cellX = x + col * (CELL_WIDTH + CELL_BORDER);
                int cellY = y + row * (CELL_HEIGHT + CELL_BORDER);

                List<String> lines = new ArrayList<>(3);

                if (entry.getStoredAmount() > 0) {
                    String str = ReadableNumberConverter.INSTANCE.toWideReadableForm(entry.getStoredAmount());
                    str = GuiText.FromStorage.getLocal() + ": " + str;
                    lines.add(str);
                }

                if (entry.getMissingAmount() > 0) {
                    String str = ReadableNumberConverter.INSTANCE.toWideReadableForm(entry.getMissingAmount());
                    str = GuiText.Missing.text().getString() + ": " + str;
                    lines.add(str);
                }

                if (entry.getCraftAmount() > 0) {
                    String str = ReadableNumberConverter.INSTANCE.toWideReadableForm(entry.getCraftAmount());
                    str = GuiText.ToCraft.getLocal() + ": " + str;
                    lines.add(str);
                }

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

                final ItemStack is = entry.getItem();

                int itemY = cellY + (CELL_HEIGHT - 16) / 2;
                screen.drawItem(itemX, itemY, is);

                if (entry.getMissingAmount() > 0) {
                    AbstractGui.fill(matrixStack, cellX, cellY, cellX + CELL_WIDTH, cellY + CELL_HEIGHT, 0x1AFF0000);
                }

                if (mouseX >= cellX && mouseX <= cellX + CELL_WIDTH) {
                    if (mouseY >= cellY && mouseY <= cellY + CELL_HEIGHT) {
                        tooltipLines = getTooltipLines(entry);
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

    private List<ITextComponent> getTooltipLines(CraftingPlanSummaryEntry entry) {
        List<ITextComponent> lines = new ArrayList<>(screen.getTooltipFromItem(entry.getItem()));

        if (entry.getStoredAmount() > 0) {
            lines.add(GuiText.FromStorage.withSuffix(": " + entry.getStoredAmount()));
        }
        if (entry.getMissingAmount() > 0) {
            lines.add(GuiText.Missing.withSuffix(": " + entry.getMissingAmount()));
        }
        if (entry.getCraftAmount() > 0) {
            lines.add(GuiText.ToCraft.withSuffix(": " + entry.getCraftAmount()));
        }

        return lines;
    }

    /**
     * @return The number of rows that are off-screen given a number of entries.
     */
    public static int getScrollableRows(int size) {
        return (size + COLS - 1) / COLS - ROWS;
    }

}

