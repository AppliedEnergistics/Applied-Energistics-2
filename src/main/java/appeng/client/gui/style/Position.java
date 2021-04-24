package appeng.client.gui.style;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.Rectangle2d;

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
    public Point resolve(Rectangle2d bounds) {
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
