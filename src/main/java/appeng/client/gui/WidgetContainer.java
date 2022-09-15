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

import java.util.LinkedHashMap;
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
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;

import appeng.client.Point;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.WidgetStyle;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.BackgroundPanel;
import appeng.client.gui.widgets.IResizableWidget;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.TabButton;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.SwitchGuisPacket;
import appeng.menu.implementations.PriorityMenu;

/**
 * This utility class helps with positioning commonly used Minecraft {@link AbstractWidget} instances on a screen
 * without having to recreate them everytime the screen resizes in the <code>init</code> method.
 * <p/>
 * This class sources the positioning and sizing for widgets from the {@link ScreenStyle}, and correlates between the
 * screen's JSON file and the widget using a string id.
 */
public class WidgetContainer {
    private final ScreenStyle style;
    private final Map<String, AbstractWidget> widgets = new LinkedHashMap<>();
    private final Map<String, ICompositeWidget> compositeWidgets = new LinkedHashMap<>();
    private final Map<String, ResolvedTooltipArea> tooltips = new LinkedHashMap<>();

    public WidgetContainer(ScreenStyle style) {
        this.style = style;
    }

    public void add(String id, AbstractWidget widget) {
        Preconditions.checkState(!compositeWidgets.containsKey(id), "%s already used for composite widget", id);

        // Size the widget, as this doesn't change when the parent is resized
        WidgetStyle widgetStyle = style.getWidget(id);
        int width = widgetStyle.getWidth() != 0 ? widgetStyle.getWidth() : widget.getWidth();
        int height = widgetStyle.getHeight() != 0 ? widgetStyle.getHeight() : widget.getHeight();
        if (widget instanceof IResizableWidget resizableWidget) {
            resizableWidget.resize(width, height);
        } else {
            widget.setWidth(width);
            widget.setHeight(height);
        }

        if (widget instanceof TabButton tabButton) {
            if (widgetStyle.isHideEdge()) {
                tabButton.setStyle(TabButton.Style.CORNER);
            }
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
    public Button addButton(String id, Component text, OnPress action) {
        var button = Button.builder(text, action).build();
        add(id, button);
        return button;
    }

    public Button addButton(String id, Component text, Runnable action) {
        return addButton(id, text, btn -> action.run());
    }

    public AECheckbox addCheckbox(String id, Component text, Runnable changeListener) {
        var checkbox = new AECheckbox(0, 0, 0, 14, style, text);
        add(id, checkbox);
        checkbox.setChangeListener(changeListener);
        return checkbox;
    }

    public NumberEntryWidget addNumberEntryWidget(String id, NumberEntryType type) {
        var numberEntry = new NumberEntryWidget(style, type);
        add(id, numberEntry);
        return numberEntry;
    }

    /**
     * Adds a {@link Scrollbar} to the screen.
     */
    public Scrollbar addScrollBar(String id) {
        return addScrollBar(id, Scrollbar.DEFAULT);
    }

    /**
     * Adds a {@link Scrollbar} to the screen.
     */
    public Scrollbar addScrollBar(String id, Scrollbar.Style style) {
        Scrollbar scrollbar = new Scrollbar(style);
        add(id, scrollbar);
        return scrollbar;
    }

    /**
     * Adds a panel to the screen, which takes its background from the style's "images" section, and it's position from
     * the widget section.
     *
     * @param id The id used to look up the background image and bounds in the style.
     */
    public void addBackgroundPanel(String id) {
        var background = style.getImage(id).copy();
        add(id, new BackgroundPanel(background));
    }

    void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
        for (var entry : widgets.entrySet()) {
            var widget = entry.getValue();
            if (widget.isFocused()) {
                widget.changeFocus(false); // Minecraft already cleared focus on the screen
            }

            // Position the widget
            WidgetStyle widgetStyle = style.getWidget(entry.getKey());
            Point pos = widgetStyle.resolve(bounds);
            if (widget instanceof IResizableWidget resizableWidget) {
                resizableWidget.move(pos);
            } else {
                widget.setX(pos.getX());
                widget.setY(pos.getY());
            }

            addWidget.accept(widget);
        }

        // For composite widgets, just position them. Positions for these widgets are generally relative to the dialog
        Rect2i relativeBounds = new Rect2i(0, 0, bounds.getWidth(), bounds.getHeight());
        for (var entry : compositeWidgets.entrySet()) {
            var widget = entry.getValue();
            var widgetStyle = style.getWidget(entry.getKey());
            widget.setPosition(widgetStyle.resolve(relativeBounds));

            widget.populateScreen(addWidget, bounds, screen);
        }

        tooltips.clear();
        for (var entry : style.getTooltips().entrySet()) {
            var pos = entry.getValue().resolve(relativeBounds);
            var area = new Rect2i(
                    pos.getX(), pos.getY(),
                    entry.getValue().getWidth(),
                    entry.getValue().getHeight());
            tooltips.put(entry.getKey(), new ResolvedTooltipArea(
                    area, new Tooltip(entry.getValue().getTooltip())));
        }
    }

    /**
     * Tick {@link ICompositeWidget} instances that are not automatically ticked as part of being a normal widget.
     */
    public void tick() {
        for (var widget : compositeWidgets.values()) {
            if (widget.isVisible()) {
                widget.tick();
            }
        }
    }

    /**
     * @see ICompositeWidget#updateBeforeRender()
     */
    public void updateBeforeRender() {
        for (var widget : compositeWidgets.values()) {
            if (widget.isVisible()) {
                widget.updateBeforeRender();
            }
        }
    }

    /**
     * @see ICompositeWidget#drawBackgroundLayer(PoseStack, int, Rect2i, Point)
     */
    public void drawBackgroundLayer(PoseStack poseStack, int zIndex, Rect2i bounds, Point mouse) {
        for (var widget : compositeWidgets.values()) {
            if (widget.isVisible()) {
                widget.drawBackgroundLayer(poseStack, zIndex, bounds, mouse);
            }
        }
    }

    /**
     * @see ICompositeWidget#drawForegroundLayer(PoseStack, int, Rect2i, Point)
     */
    public void drawForegroundLayer(PoseStack poseStack, int zIndex, Rect2i bounds, Point mouse) {
        for (var widget : compositeWidgets.values()) {
            if (widget.isVisible()) {
                widget.drawForegroundLayer(poseStack, zIndex, bounds, mouse);
            }
        }
    }

    /**
     * @see ICompositeWidget#onMouseDown(Point, int)
     */
    public boolean onMouseDown(Point mousePos, int btn) {
        for (var widget : compositeWidgets.values()) {
            if (widget.isVisible()
                    && (widget.wantsAllMouseDownEvents() || mousePos.isIn(widget.getBounds()))
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
        for (var widget : compositeWidgets.values()) {
            if (widget.isVisible()
                    && (widget.wantsAllMouseUpEvents() || mousePos.isIn(widget.getBounds()))
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
        for (var widget : compositeWidgets.values()) {
            if (widget.isVisible() && widget.onMouseDrag(mousePos, btn)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @see ICompositeWidget#onMouseWheel(Point, double)
     */
    boolean onMouseWheel(Point mousePos, double wheelDelta) {
        // First pass: dispatch wheel event to widgets the mouse is over
        for (var widget : compositeWidgets.values()) {
            if (widget.isVisible()
                    && mousePos.isIn(widget.getBounds())
                    && widget.onMouseWheel(mousePos, wheelDelta)) {
                return true;
            }
        }

        // Second pass: send the event to capturing widgets
        for (var widget : compositeWidgets.values()) {
            if (widget.isVisible()
                    && widget.wantsAllMouseWheelEvents()
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
        for (var widget : compositeWidgets.values()) {
            if (widget.isVisible()) {
                widget.addExclusionZones(exclusionZones, bounds);
            }
        }
    }

    /**
     * Adds a button named "openPriority" that opens the priority GUI for the current menu host.
     */
    public void addOpenPriorityButton() {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        add("openPriority", new TabButton(Icon.WRENCH, GuiText.Priority.text(),
                itemRenderer, btn -> openPriorityGui()));
    }

    private void openPriorityGui() {
        NetworkHandler.instance().sendToServer(SwitchGuisPacket.openSubMenu(PriorityMenu.TYPE));
    }

    /**
     * Enables or disables a tooltip area that is defined in the widget styles.
     */
    public void setTooltipAreaEnabled(String id, boolean enabled) {
        var tooltip = tooltips.get(id);
        Preconditions.checkArgument(tooltip != null, "No tooltip with id '%s' is defined", id);
        tooltip.enabled = enabled;
    }

    @Nullable
    public Tooltip getTooltip(int mouseX, int mouseY) {
        for (var c : this.compositeWidgets.values()) {
            if (!c.isVisible()) {
                continue;
            }

            Rect2i bounds = c.getBounds();
            if (mouseX >= bounds.getX() && mouseX < bounds.getX() + bounds.getWidth()
                    && mouseY >= bounds.getY() && mouseY < bounds.getY() + bounds.getHeight()) {
                Tooltip tooltip = c.getTooltip(mouseX, mouseY);
                if (tooltip != null) {
                    return tooltip;
                }
            }
        }

        for (var tooltipArea : tooltips.values()) {
            if (tooltipArea.enabled && contains(tooltipArea.area, mouseX, mouseY)) {
                return tooltipArea.tooltip;
            }
        }

        return null;
    }

    /**
     * Check if there's any content or compound widget at the given screen-relative mouse position.
     */
    public boolean hitTest(Point mousePos) {
        for (var widget : compositeWidgets.values()) {
            if (mousePos.isIn(widget.getBounds())) {
                return true;
            }
        }
        return false;
    }

    // NOTE: Vanilla's implementation of Rect2i is broken since it uses less-than-equal to compare against x+width,
    // rather than less-than.
    private static boolean contains(Rect2i area, int mouseX, int mouseY) {
        return mouseX >= area.getX() && mouseX < area.getX() + area.getWidth()
                && mouseY >= area.getY() && mouseY < area.getY() + area.getHeight();
    }

    public AETextField addTextField(String id) {
        var searchField = new AETextField(style, Minecraft.getInstance().font,
                0, 0, 0, 0);
        searchField.setBordered(false);
        searchField.setMaxLength(25);
        searchField.setTextColor(0xFFFFFF);
        searchField.setSelectionColor(0xFF008000);
        searchField.setVisible(true);
        add(id, searchField);
        return searchField;
    }

    private static class ResolvedTooltipArea {
        private final Rect2i area;
        private final Tooltip tooltip;
        private boolean enabled = true;

        public ResolvedTooltipArea(Rect2i area, Tooltip tooltip) {
            this.area = area;
            this.tooltip = tooltip;
        }
    }
}
