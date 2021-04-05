package appeng.client.gui.widgets;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Rectangle2d;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * A stacked button panel on the left or right side of our UIs.
 */
public class VerticalButtonBar {

    // Position of the button bar in relation to the GUI's top edge
    private static final int START_Y = 6;

    // Vertical space between buttons
    private static final int VERTICAL_SPACING = 4;

    // The margin between the right side of the buttons and the GUI
    private static final int MARGIN = 2;

    private final List<Button> buttons = new ArrayList<>();

    private Rectangle2d boundingRectangle = new Rectangle2d(0, 0, 0, 0);

    private int originX;
    private int originY;

    public VerticalButtonBar() {
        reset(0, 0);
    }

    public void add(Button button) {
        buttons.add(button);
    }

    public void layout() {
        int currentY = originY + START_Y + MARGIN;
        int maxWidth = 0;

        // Align the button's right edge with the UI and account for margin
        for (Button button : buttons) {
            if (!button.visible) {
                continue;
            }

            button.x = originX - MARGIN - button.getWidth();
            button.y = currentY;

            currentY += button.getHeightRealms() + VERTICAL_SPACING;
            maxWidth = Math.max(button.getWidth(), maxWidth);
        }

        // Set up a bounding rectangle for JEI exclusion zones
        int boundX = originX - maxWidth - 2 * MARGIN;
        int boundY = originY + START_Y;
        boundingRectangle = new Rectangle2d(
                boundX,
                boundY,
                maxWidth + 2 * MARGIN,
                currentY + MARGIN - boundY
        );
    }

    /**
     * Called when the parent UI is repositioned or resized. All buttons need to be re-added since
     * Vanilla clears the widgets when this happens.
     */
    public void reset(int originX, int originY) {
        this.originX = originX;
        this.originY = originY;
        buttons.clear();
    }

    @Nonnull
    public Rectangle2d getBoundingRectangle() {
        return boundingRectangle;
    }

}
