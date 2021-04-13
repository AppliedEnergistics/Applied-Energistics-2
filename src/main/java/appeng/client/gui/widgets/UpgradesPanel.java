package appeng.client.gui.widgets;

import appeng.client.gui.Blitter;
import appeng.client.gui.Rects;
import appeng.container.slot.IOptionalSlot;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.inventory.container.Slot;

import java.util.List;

/**
 * A panel that can draw a dynamic number of upgrade slots in a vertical layout.
 */
public final class UpgradesPanel {

    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 7;
    private static final int MAX_ROWS = 8;

    private static final Blitter BACKGROUND = Blitter.texture("guis/extra_panels.png", 128, 128);
    private static final Blitter INNER_CORNER = BACKGROUND.copy().src(12, 33, SLOT_SIZE, SLOT_SIZE);

    private final List<Slot> slots;

    // Relative to current screen origin (not window)
    private int x;
    private int y;

    public UpgradesPanel(int x, int y, List<Slot> slots) {
        this.slots = slots;
        this.setPos(x, y);
    }

    public void draw(MatrixStack matrices, int zIndex, int offsetX, int offsetY) {
        int slotCount = getUpgradeSlotCount();
        if (slotCount <= 0) {
            return;
        }

        this.layoutSlots();

        // This is the absolute x,y coord of the first slot within the panel
        int slotOriginX = offsetX + this.x + PADDING;
        int slotOriginY = offsetY + this.y + PADDING;

        for (int i = 0; i < slotCount; i++) {
            // Unlike other UIs, this is drawn top-to-bottom,left-to-right
            int row = i % MAX_ROWS;
            int col = i / MAX_ROWS;

            int x = slotOriginX + col * SLOT_SIZE;
            int y = slotOriginY + row * SLOT_SIZE;

            boolean borderLeft = col == 0;
            boolean borderTop = row == 0;
            // The panel can have a "jagged" edge if the number of slots is not divisible by MAX_ROWS
            boolean lastSlot = i + 1 >= slotCount;
            boolean lastRow = row + 1 >= MAX_ROWS;
            boolean borderBottom = lastRow || lastSlot;
            boolean borderRight = i >= slotCount - MAX_ROWS;

            drawSlot(matrices, zIndex, x, y, borderLeft, borderTop, borderRight, borderBottom);

            // Cover up the inner corner when we just drew a rather ugly "inner corner"
            if (col > 0 && lastSlot && !lastRow) {
                INNER_CORNER.dest(x, y + SLOT_SIZE).blit(matrices, zIndex);
            }
        }
    }

    /**
     * Computes the exclusion zones of this widget in window coordinates, given the offset of the current screen's
     * origin in the window.
     */
    public void addExclusionZones(int offsetX, int offsetY, List<Rectangle2d> zones) {

        int slotCount = getUpgradeSlotCount();

        // Use a bit of a margin around the zone to avoid things looking too cramped
        final int margin = 2;

        // Add a single bounding rectangle for as many columns as are fully populated
        int fullCols = slotCount / MAX_ROWS;
        int rightEdge = offsetX + x;
        if (fullCols > 0) {
            int fullColWidth = PADDING * 2 + fullCols * SLOT_SIZE;
            zones.add(Rects.expand(new Rectangle2d(
                    rightEdge,
                    offsetY + y,
                    fullColWidth,
                    PADDING * 2 + MAX_ROWS * SLOT_SIZE), margin));
            rightEdge += fullColWidth;
        }

        // If there's a partially populated row at the end, add a smaller rectangle for it
        int remaining = slotCount - fullCols * MAX_ROWS;
        if (remaining > 0) {
            zones.add(Rects.expand(new Rectangle2d(
                    rightEdge,
                    offsetY + y,
                    // We need to add padding in case there's no full column that already includes it
                    SLOT_SIZE + ((fullCols > 0) ? 0 : PADDING * 2),
                    PADDING * 2 + remaining * SLOT_SIZE), margin));
        }

    }

    /**
     * Changes where the panel is positioned. Coordinates are relative to the current screen's origin.
     */
    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    private static void drawSlot(MatrixStack matrices, int zIndex, int x, int y,
                                 boolean borderLeft, boolean borderTop, boolean borderRight, boolean borderBottom) {
        int srcX = PADDING;
        int srcY = PADDING;
        int srcWidth = SLOT_SIZE;
        int srcHeight = SLOT_SIZE;

        if (borderLeft) {
            x -= PADDING;
            srcX = 0;
            srcWidth += PADDING;
        }
        if (borderRight) {
            srcWidth += PADDING;
        }
        if (borderTop) {
            y -= PADDING;
            srcY = 0;
            srcHeight += PADDING;
        }
        if (borderBottom) {
            srcHeight += PADDING;
        }

        BACKGROUND.src(srcX, srcY, srcWidth, srcHeight)
                .dest(x, y)
                .blit(matrices, zIndex);
    }

    /**
     * We need this function since the cell workbench can dynamically change how many upgrade slots are active based on
     * the cell in the workbench.
     */
    private int getUpgradeSlotCount() {
        int count = 0;
        for (Slot slot : slots) {
            if (!(slot instanceof IOptionalSlot) || ((IOptionalSlot) slot).isSlotEnabled()) {
                count++;
            }
        }
        return count;
    }

    private void layoutSlots() {
        int slotOriginX = this.x + PADDING;
        int slotOriginY = this.y + PADDING;

        for (Slot slot : slots) {
            if (!slot.isEnabled()) {
                continue;
            }

            slot.xPos = slotOriginX + 1;
            slot.yPos = slotOriginY + 1;
            slotOriginY += SLOT_SIZE;
        }
    }

}
