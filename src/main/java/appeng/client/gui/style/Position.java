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

package appeng.client.gui.style;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.Rect2i;

import appeng.client.Point;

/**
 * Describes positioning for a slot.
 */
public class Position {

    @Nullable
    private Integer left;

    @Nullable
    private Integer top;

    @Nullable
    private Integer right;

    @Nullable
    private Integer bottom;

    public Integer getLeft() {
        return left;
    }

    public void setLeft(Integer left) {
        this.left = left;
    }

    public Integer getTop() {
        return top;
    }

    public void setTop(Integer top) {
        this.top = top;
    }

    public Integer getRight() {
        return right;
    }

    public void setRight(Integer right) {
        this.right = right;
    }

    public Integer getBottom() {
        return bottom;
    }

    public void setBottom(Integer bottom) {
        this.bottom = bottom;
    }

    /**
     * Resolves this relative position against the given bounds, and makes it absolute.
     */
    public Point resolve(Rect2i bounds) {
        // Start by computing the x,y position
        int x, y;
        if (left != null) {
            x = left;
        } else if (right != null) {
            x = bounds.getWidth() - right;
        } else {
            x = 0;
        }
        if (top != null) {
            y = top;
        } else if (bottom != null) {
            y = bounds.getHeight() - bottom;
        } else {
            y = 0;
        }

        return new Point(x, y).move(bounds.getX(), bounds.getY());
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (left != null) {
            result.append("left=").append(left).append(",");
        }
        if (top != null) {
            result.append("top=").append(top).append(",");
        }
        if (right != null) {
            result.append("right=").append(right).append(",");
        }
        if (bottom != null) {
            result.append("bottom=").append(bottom).append(",");
        }
        if (result.length() > 0) {
            result.deleteCharAt(result.length() - 1);
        }
        return result.toString();
    }
}
