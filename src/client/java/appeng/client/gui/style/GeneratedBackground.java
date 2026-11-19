package appeng.client.gui.style;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;

import appeng.client.gui.widgets.PanelBlitter;

/**
 * Describes a screen background that is assembled from GUI sprites at runtime.
 */
public class GeneratedBackground {
    /**
     * The width of the screen.
     */
    private int width;

    /**
     * The height of the screen.
     */
    private int height;

    /**
     * Optional list of rectangles relative to the screen origin, whose union defines the shape of the window. If empty,
     * the window covers the screen bounds given by {@link #width} and {@link #height}.
     */
    private final List<Rect2i> shape = new ArrayList<>();

    /**
     * Optionally replaces the nine-slice sprite used to draw the window (see {@link PanelBlitter#WINDOW_SPRITE}).
     */
    @Nullable
    private Identifier sprite;

    /**
     * Optionally replaces the sprite used to draw inner corners of the window (see
     * {@link PanelBlitter#WINDOW_INNER_SPRITE}).
     */
    @Nullable
    private Identifier innerSprite;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Nullable
    public Identifier getSprite() {
        return sprite;
    }

    @Nullable
    public Identifier getInnerSprite() {
        return innerSprite;
    }

    public List<Rect2i> getShape() {
        return shape;
    }

    /**
     * Adds the window shape to the given panel blitter.
     */
    public void addToPanel(PanelBlitter panel) {
        if (sprite != null || innerSprite != null) {
            panel.setSprites(
                    sprite != null ? sprite : PanelBlitter.WINDOW_SPRITE,
                    innerSprite != null ? innerSprite : PanelBlitter.WINDOW_INNER_SPRITE);
        }
        if (shape.isEmpty()) {
            panel.addBounds(0, 0, width, height);
        } else {
            for (var rect : shape) {
                panel.addBounds(rect);
            }
        }
    }
}
