package appeng.client.gui.widgets;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.Rectangle2d;

import appeng.client.gui.Blitter;
import appeng.client.gui.Rects;
import appeng.container.slot.AppEngSlot;

/**
 * A 3x3 toolbox panel attached to the player inventory.
 */
public class ToolboxPanel {

    // Padding within TOOLBOX to position the actual slots
    private static final int TOOLBOX_PADDING = 7;
    // Margin around slots in TOOLBOX (slots are assumed to be 16x16)
    private static final int TOOLBOX_SLOT_MARGIN = 1;

    // Backdrop for the 3x3 toolbox offered by the network-tool
    private static final Blitter BACKGROUND = Blitter.texture("guis/extra_panels.png", 128, 128)
            .src(60, 60, 68, 68);

    private final List<AppEngSlot> toolboxSlots;

    // Relative to the origin of the current screen (not window)
    private int x;
    private int y;

    public ToolboxPanel(int x, int y, List<AppEngSlot> toolboxSlots) {
        this.toolboxSlots = toolboxSlots;
        this.setPos(x, y);
    }

    public void addExclusionZones(int offsetX, int offsetY, List<Rectangle2d> zones) {
        zones.add(Rects.expand(
                new Rectangle2d(offsetX + x, offsetY + y, BACKGROUND.getSrcWidth(), BACKGROUND.getSrcHeight()), 2));
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
        positionToolboxSlots();
    }

    public void draw(MatrixStack matrices, int blitOffset, int offsetX, int offsetY) {
        BACKGROUND.dest(
                offsetX + x,
                offsetY + y).blit(matrices, blitOffset);
    }

    private void positionToolboxSlots() {
        int slotOriginX = x + TOOLBOX_PADDING;
        int slotOriginY = y + TOOLBOX_PADDING;

        // Toolbox slots are organized in a 3x3 grid left-to-right,top-to-bottom
        int sizeWithMargin = 16 + TOOLBOX_SLOT_MARGIN * 2;
        for (int i = 0; i < toolboxSlots.size(); i++) {
            int row = i / 3;
            int col = i % 3;

            AppEngSlot slot = toolboxSlots.get(i);
            slot.xPos = slotOriginX + row * sizeWithMargin + TOOLBOX_SLOT_MARGIN;
            slot.yPos = slotOriginY + col * sizeWithMargin + TOOLBOX_SLOT_MARGIN;
        }
    }

}
