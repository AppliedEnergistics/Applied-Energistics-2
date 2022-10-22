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

package appeng.client.gui.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;

/**
 * A stacked button panel on the left or right side of our UIs.
 */
public class VerticalButtonBar implements ICompositeWidget {

    // Vertical space between buttons
    private static final int VERTICAL_SPACING = 4;

    // The margin between the right side of the buttons and the GUI
    private static final int MARGIN = 2;

    private final List<Button> buttons = new ArrayList<>();

    // The origin of the last initialized screen in window coordinates
    private Point screenOrigin = Point.ZERO;
    // This bounding rectangle relative to the screens origin
    private Rect2i bounds = new Rect2i(0, 0, 0, 0);

    private Point position;

    public VerticalButtonBar() {
    }

    public void add(Button button) {
        buttons.add(button);
    }

    @Override
    public void setPosition(Point position) {
        this.position = position;
    }

    @Override
    public void setSize(int width, int height) {
        // Setting the size for this control is not supported
    }

    @Override
    public Rect2i getBounds() {
        return bounds;
    }

    /**
     * We need to update every frame because buttons can become visible/invisible at any point in time.
     */
    @Override
    public void updateBeforeRender() {
        int currentY = position.getY() + MARGIN;
        int maxWidth = 0;

        // Align the button's right edge with the UI and account for margin
        for (Button button : buttons) {
            if (!button.visible) {
                continue;
            }

            // Vanilla widgets need to be in window space
            button.setX(screenOrigin.getX() + position.getX() - MARGIN - button.getWidth());
            button.setY(screenOrigin.getY() + currentY);

            currentY += button.getHeight() + VERTICAL_SPACING;
            maxWidth = Math.max(button.getWidth(), maxWidth);
        }

        // Set up a bounding rectangle for JEI exclusion zones
        if (maxWidth == 0) {
            bounds = new Rect2i(0, 0, 0, 0);
        } else {
            int boundX = position.getX() - maxWidth - 2 * MARGIN;
            int boundY = position.getY();
            bounds = new Rect2i(
                    boundX,
                    boundY,
                    maxWidth + 2 * MARGIN,
                    currentY - boundY);
        }
    }

    /**
     * Called when the parent UI is repositioned or resized. All buttons need to be re-added since Vanilla clears the
     * widgets when this happens.
     */
    @Override
    public void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
        this.screenOrigin = Point.fromTopLeft(bounds);
        for (var button : this.buttons) {
            if (button.isFocused()) {
                button.changeFocus(false);
            }
            addWidget.accept(button);
        }
    }
}
