package appeng.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.Rect2i;

import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.style.Blitter;

/**
 * Renders a simple panel with a background an no interactivity.
 */
public class BackgroundPanel implements ICompositeWidget {
    private final Blitter background;

    // Relative to current screen origin (not window)
    private int x;
    private int y;

    public BackgroundPanel(Blitter background) {
        this.background = background;
    }

    @Override
    public void setPosition(Point position) {
        x = position.getX();
        y = position.getY();
    }

    @Override
    public void setSize(int width, int height) {
        // Size of panels is implied by the background
    }

    @Override
    public Rect2i getBounds() {
        return new Rect2i(x, y, background.getSrcWidth(), background.getSrcHeight());
    }

    @Override
    public void drawBackgroundLayer(PoseStack poseStack, Rect2i bounds, Point mouse) {
        background.dest(bounds.getX() + x, bounds.getY() + y).blit(poseStack);
    }
}
