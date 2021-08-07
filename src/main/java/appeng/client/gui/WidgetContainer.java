/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.Button.OnTooltip;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;

import appeng.client.Point;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.WidgetStyle;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.TabButton;
import appeng.menu.implementations.PriorityMenu;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.SwitchGuisPacket;

/**
 * This utility class helps with positioning commonly used Minecraft {@link AbstractWidget} instances on a screen
 * without having to recreate them everytime the screen resizes in the <code>init</code> method.
 * <p/>
 * This class sources the positioning and sizing for widgets from the {@link ScreenStyle}, and correlates between the
 * screen's JSON file and the widget using a string id.
 */
public class WidgetContainer {
    private final ScreenStyle style;
    private final Map<String, AbstractWidget> widgets = new HashMap<>();
    private final Map<String, ICompositeWidget> compositeWidgets = new HashMap<>();

    public WidgetContainer(ScreenStyle style) {
        this.style = style;
    }

    public void add(String id, AbstractWidget widget) {
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
    public Button addButton(String id, Component text, OnPress action, OnTooltip tooltip) {
        Button button = new Button(0, 0, 0, 0, text, action, tooltip);
        add(id, button);
        return button;
    }

    public Button addButton(String id, Component text, OnPress action) {
        return addButton(id, text, action, Button.NO_TOOLTIP);
    }

    public Button addButton(String id, Component text, Runnable action, OnTooltip tooltip) {
        return addButton(id, text, btn -> action.run(), tooltip);
    }

    public Button addButton(String id, Component text, Runnable action) {
        return addButton(id, text, action, Button.NO_TOOLTIP);
    }

    /**
     * Adds a {@link Scrollbar} to the screen.
     */
    public Scrollbar addScrollBar(String id) {
        Scrollbar scrollbar = new Scrollbar();
        add(id, scrollbar);
        return scrollbar;
    }

    void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
        for (Map.Entry<String, AbstractWidget> entry : widgets.entrySet()) {
            AbstractWidget widget = entry.getValue();

            // Position the widget
            WidgetStyle widgetStyle = style.getWidget(entry.getKey());
            Point pos = widgetStyle.resolve(bounds);
            widget.x = pos.getX();
            widget.y = pos.getY();

            addWidget.accept(widget);
        }

        // For composite widgets, just position them. Positions for these widgets are generally relative to the dialog
        Rect2i relativeBounds = new Rect2i(0, 0, bounds.getWidth(), bounds.getHeight());
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
     * @see ICompositeWidget#drawBackgroundLayer(PoseStack, int, Rect2i, Point)
     */
    public void drawBackgroundLayer(PoseStack matrices, int zIndex, Rect2i bounds, Point mouse) {
        for (ICompositeWidget widget : compositeWidgets.values()) {
            widget.drawBackgroundLayer(matrices, zIndex, bounds, mouse);
        }
    }

    /**
     * @see ICompositeWidget#drawForegroundLayer(PoseStack, int, Rect2i, Point)
     */
    public void drawForegroundLayer(PoseStack matrices, int zIndex, Rect2i bounds, Point mouse) {
        for (ICompositeWidget widget : compositeWidgets.values()) {
            widget.drawForegroundLayer(matrices, zIndex, bounds, mouse);
        }
    }

    /**
     * @see ICompositeWidget#onMouseDown(Point, int)
     */
    public boolean onMouseDown(Point mousePos, int btn) {
        for (ICompositeWidget widget : compositeWidgets.values()) {
            if ((widget.wantsAllMouseDownEvents() || mousePos.isIn(widget.getBounds()))
                    && widget.onMouseDown(mousePos, btn)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @see ICompositeWidget#onMouseUp(Point, int)
     */
    public boolean onMouseUp(Point mousePos, int btn) {
        for (ICompositeWidget widget : compositeWidgets.values()) {
            if ((widget.wantsAllMouseUpEvents() || mousePos.isIn(widget.getBounds()))
                    && widget.onMouseUp(mousePos, btn)) {
                return true;
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
            if ((widget.wantsAllMouseWheelEvents() || mousePos.isIn(widget.getBounds()))
                    && widget.onMouseWheel(mousePos, wheelDelta)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @see ICompositeWidget#addExclusionZones(List, Rect2i)
     */
    public void addExclusionZones(List<Rect2i> exclusionZones, Rect2i bounds) {
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
        NetworkHandler.instance().sendToServer(new SwitchGuisPacket(PriorityMenu.TYPE));
    }

    @Nullable
    public Tooltip getTooltip(int mouseX, int mouseY) {
        for (ICompositeWidget c : this.compositeWidgets.values()) {
            Rect2i bounds = c.getBounds();
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
