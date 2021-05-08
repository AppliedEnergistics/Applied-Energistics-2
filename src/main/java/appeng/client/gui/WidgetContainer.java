package appeng.client.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.ITextComponent;

import appeng.client.Point;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.WidgetStyle;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.TabButton;
import appeng.container.implementations.PriorityContainer;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.SwitchGuisPacket;

/**
 * This utility class helps with positioning commonly used Minecraft {@link Widget} instances on a screen without having
 * to recreate them everytime the screen resizes in the <code>init</code> method.
 * <p/>
 * This class sources the positioning and sizing for widgets from the {@link ScreenStyle}, and correlates between the
 * screen's JSON file and the widget using a string id.
 */
public class WidgetContainer {
    private final ScreenStyle style;
    private final Map<String, Widget> widgets = new HashMap<>();
    private final Map<String, ICompositeWidget> compositeWidgets = new HashMap<>();

    public WidgetContainer(ScreenStyle style) {
        this.style = style;
    }

    public void add(String id, Widget widget) {
        Preconditions.checkState(!compositeWidgets.containsKey(id), "%s already used for composite widget", id);

        // Size the widget, as this doesn't change when the parent is resized
        WidgetStyle widgetStyle = style.getWidget(id);
        if (widgetStyle.getWidth() != 0) {
            widget.setWidth(widgetStyle.getWidth());
        }
        if (widgetStyle.getHeight() != 0) {
            widget.setHeight(widgetStyle.getHeight());
        }

        if (widget instanceof TabButton) {
            ((TabButton) widget).setHideEdge(widgetStyle.isHideEdge());
        }

        if (widgets.put(id, widget) != null) {
            throw new IllegalStateException("Duplicate id: " + id);
        }
    }

    public void add(String id, ICompositeWidget widget) {
        Preconditions.checkState(!widgets.containsKey(id), "%s already used for widget", id);

        // Size the widget, as this doesn't change when the parent is resized
        WidgetStyle widgetStyle = style.getWidget(id);
        widget.setSize(widgetStyle.getWidth(), widgetStyle.getHeight());

        if (compositeWidgets.put(id, widget) != null) {
            throw new IllegalStateException("Duplicate id: " + id);
        }
    }

    /**
     * Convenient way to add Vanilla buttons without having to specify x,y,width and height. The actual
     * position/rectangle is instead sourced from the screen style.
     */
    public Button addButton(String id, ITextComponent text, Button.IPressable action, Button.ITooltip tooltip) {
        Button button = new Button(0, 0, 0, 0, text, action, tooltip);
        add(id, button);
        return button;
    }

    public Button addButton(String id, ITextComponent text, Button.IPressable action) {
        return addButton(id, text, action, Button.field_238486_s_);
    }

    public Button addButton(String id, ITextComponent text, Runnable action, Button.ITooltip tooltip) {
        return addButton(id, text, btn -> action.run(), tooltip);
    }

    public Button addButton(String id, ITextComponent text, Runnable action) {
        return addButton(id, text, action, Button.field_238486_s_);
    }

    /**
     * Adds a {@link Scrollbar} to the screen.
     */
    public Scrollbar addScrollBar(String id) {
        Scrollbar scrollbar = new Scrollbar();
        add(id, scrollbar);
        return scrollbar;
    }

    void populateScreen(Consumer<Widget> addWidget, Rectangle2d bounds, AEBaseScreen<?> screen) {
        for (Map.Entry<String, Widget> entry : widgets.entrySet()) {
            Widget widget = entry.getValue();

            // Position the widget
            WidgetStyle widgetStyle = style.getWidget(entry.getKey());
            Point pos = widgetStyle.resolve(bounds);
            widget.x = pos.getX();
            widget.y = pos.getY();

            addWidget.accept(widget);
        }

        // For composite widgets, just position them. Positions for these widgets are generally relative to the dialog
        Rectangle2d relativeBounds = new Rectangle2d(0, 0, bounds.getWidth(), bounds.getHeight());
        for (Map.Entry<String, ICompositeWidget> entry : compositeWidgets.entrySet()) {
            ICompositeWidget widget = entry.getValue();
            WidgetStyle widgetStyle = style.getWidget(entry.getKey());
            widget.setPosition(widgetStyle.resolve(relativeBounds));

            widget.populateScreen(addWidget, bounds, screen);
        }
    }

    /**
     * Tick {@link ICompositeWidget} instances that are not automatically ticked as part of being a normal widget.
     */
    public void tick() {
        for (ICompositeWidget widget : compositeWidgets.values()) {
            widget.tick();
        }
    }

    /**
     * @see ICompositeWidget#updateBeforeRender()
     */
    public void updateBeforeRender() {
        for (ICompositeWidget widget : compositeWidgets.values()) {
            widget.updateBeforeRender();
        }
    }

    /**
     * @see ICompositeWidget#drawBackgroundLayer(MatrixStack, int, Rectangle2d, Point)
     */
    public void drawBackgroundLayer(MatrixStack matrices, int zIndex, Rectangle2d bounds, Point mouse) {
        for (ICompositeWidget widget : compositeWidgets.values()) {
            widget.drawBackgroundLayer(matrices, zIndex, bounds, mouse);
        }
    }

    /**
     * @see ICompositeWidget#drawForegroundLayer(MatrixStack, int, Rectangle2d, Point)
     */
    public void drawForegroundLayer(MatrixStack matrices, int zIndex, Rectangle2d bounds, Point mouse) {
        for (ICompositeWidget widget : compositeWidgets.values()) {
            widget.drawForegroundLayer(matrices, zIndex, bounds, mouse);
        }
    }

    /**
     * @see ICompositeWidget#onMouseDown(Point, int)
     */
    public boolean onMouseDown(Point mousePos, int btn) {
        for (ICompositeWidget widget : compositeWidgets.values()) {
            if (widget.wantsAllMouseDownEvents() || mousePos.isIn(widget.getBounds())) {
                if (widget.onMouseDown(mousePos, btn)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @see ICompositeWidget#onMouseUp(Point, int)
     */
    public boolean onMouseUp(Point mousePos, int btn) {
        for (ICompositeWidget widget : compositeWidgets.values()) {
            if (widget.wantsAllMouseUpEvents() || mousePos.isIn(widget.getBounds())) {
                if (widget.onMouseUp(mousePos, btn)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @see ICompositeWidget#onMouseDrag(Point, int)
     */
    public boolean onMouseDrag(Point mousePos, int btn) {
        for (ICompositeWidget widget : compositeWidgets.values()) {
            if (widget.onMouseDrag(mousePos, btn)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @see ICompositeWidget#onMouseWheel(Point, double)
     */
    boolean onMouseWheel(Point mousePos, double wheelDelta) {
        for (ICompositeWidget widget : compositeWidgets.values()) {
            if (widget.wantsAllMouseWheelEvents() || mousePos.isIn(widget.getBounds())) {
                if (widget.onMouseWheel(mousePos, wheelDelta)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @see ICompositeWidget#addExclusionZones(List, Rectangle2d)
     */
    public void addExclusionZones(List<Rectangle2d> exclusionZones, Rectangle2d bounds) {
        for (ICompositeWidget widget : compositeWidgets.values()) {
            widget.addExclusionZones(exclusionZones, bounds);
        }
    }

    /**
     * Adds a button named "openPriority" that opens the priority GUI for the current container host.
     */
    public void addOpenPriorityButton() {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        add("openPriority", new TabButton(Icon.WRENCH, GuiText.Priority.text(),
                itemRenderer, btn -> openPriorityGui()));
    }

    private void openPriorityGui() {
        NetworkHandler.instance().sendToServer(new SwitchGuisPacket(PriorityContainer.TYPE));
    }

    @Nullable
    public Tooltip getTooltip(int mouseX, int mouseY) {
        for (ICompositeWidget c : this.compositeWidgets.values()) {
            Rectangle2d bounds = c.getBounds();
            if (mouseX >= bounds.getX() && mouseX < bounds.getX() + bounds.getWidth()
                    && mouseY >= bounds.getY() && mouseY < bounds.getY() + bounds.getHeight()) {
                Tooltip tooltip = c.getTooltip(mouseX, mouseY);
                if (tooltip != null) {
                    return tooltip;
                }
            }
        }

        return null;
    }
}
