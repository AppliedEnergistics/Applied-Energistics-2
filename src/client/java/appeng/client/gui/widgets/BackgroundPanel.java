package appeng.client.gui.widgets;

import net.minecraft.client.renderer.Rect2i;

import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;

/**
 * A panel that extends the window of a screen. Its size is taken from the widget style. The background of the panel is
 * drawn as part of the screen's window (see {@link PanelBlitter}).
 */
public class BackgroundPanel implements ICompositeWidget {
    private int x;
    private int y;
    private int width;
    private int height;

    @Override
    public void setPosition(Point position) {
        x = position.getX();
        y = position.getY();
    }

    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public Rect2i getBounds() {
        return new Rect2i(x, y, width, height);
    }

    @Override
    public void addBackgroundPanels(PanelBlitter panels) {
        panels.addBounds(getBounds());
    }
}
