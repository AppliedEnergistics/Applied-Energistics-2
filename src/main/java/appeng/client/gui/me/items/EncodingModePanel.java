package appeng.client.gui.me.items;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.WidgetContainer;
import appeng.menu.me.items.PatternEncodingTermMenu;

public abstract class EncodingModePanel implements ICompositeWidget {
    protected final PatternEncodingTermScreen<?> screen;
    protected final PatternEncodingTermMenu menu;
    protected final WidgetContainer widgets;
    protected boolean visible = false;
    protected int x;
    protected int y;

    public EncodingModePanel(PatternEncodingTermScreen<?> screen, WidgetContainer widgets) {
        this.screen = screen;
        this.menu = screen.getMenu();
        this.widgets = widgets;
    }

    abstract ItemStack getTabIconItem();

    abstract Component getTabTooltip();

    @Override
    public void setPosition(Point position) {
        x = position.getX();
        y = position.getY();
    }

    @Override
    public void setSize(int width, int height) {
    }

    @Override
    public Rect2i getBounds() {
        return new Rect2i(x, y, 126, 68);
    }

    @Override
    public final boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
