package appeng.client.gui.widgets;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.Rectangle2d;

import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Tooltip;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.core.localization.GuiText;

/**
 * A 3x3 toolbox panel attached to the player inventory.
 */
public class ToolboxPanel implements ICompositeWidget {

    // Backdrop for the 3x3 toolbox offered by the network-tool
    private final Blitter background;

    // Relative to the origin of the current screen (not window)
    private Rectangle2d bounds = new Rectangle2d(0, 0, 0, 0);

    public ToolboxPanel(ScreenStyle style) {
        background = style.getImage("toolbox");
    }

    @Override
    public void setPosition(Point position) {
        this.bounds = new Rectangle2d(position.getX(), position.getY(), bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public void setSize(int width, int height) {
        this.bounds = new Rectangle2d(bounds.getX(), bounds.getY(), width, height);
    }

    @Override
    public Rectangle2d getBounds() {
        return bounds;
    }

    @Override
    public void drawBackgroundLayer(MatrixStack matrices, int zIndex, Rectangle2d bounds, Point mouse) {
        background.dest(
                bounds.getX() + this.bounds.getX(),
                bounds.getY() + this.bounds.getY(),
                this.bounds.getWidth(),
                this.bounds.getHeight()).blit(matrices, zIndex);
    }

    @Nullable
    @Override
    public Tooltip getTooltip(int mouseX, int mouseY) {
        return new Tooltip(GuiText.ToolboxSlots);
    }

}
