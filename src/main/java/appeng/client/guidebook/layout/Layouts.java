package appeng.client.guidebook.layout;

import java.util.List;

import org.joml.Vector2i;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.block.AlignItems;
import appeng.client.guidebook.document.block.LytAxis;
import appeng.client.guidebook.document.block.LytBlock;

public final class Layouts {
    private Layouts() {
    }

    /**
     * Lays out all children along the vertical axis, and returns the bounding box of the content area.
     */
    public static LytRect verticalLayout(
            LayoutContext context,
            List<LytBlock> children,
            int x, int y, int availableWidth,
            int paddingLeft, int paddingTop, int paddingRight, int paddingBottom,
            int gap,
            AlignItems alignItems) {
        // Margins have been applied outside
        // Paddings need to be considered here
        var innerX = x + paddingLeft;
        var innerY = y + paddingTop;
        var innerWidth = availableWidth - paddingLeft - paddingRight;

        // Layout children vertically, without padding
        LytBlock previousBlock = null;
        var contentWidth = paddingLeft;
        var contentHeight = paddingTop;
        for (var child : children) {
            innerY = offsetIntoContentArea(LytAxis.VERTICAL, innerY, previousBlock, child);
            // Block width is the width available for the inner content area of the child
            var blockWidth = Math.max(1, innerWidth - child.getMarginLeft() - child.getMarginRight());
            var childBounds = child.layout(context, innerX + child.getMarginLeft(), innerY, blockWidth);
            innerY += childBounds.height() + child.getMarginBottom() + gap;
            contentWidth = Math.max(contentWidth, childBounds.right() - x);
            contentHeight = Math.max(contentHeight, childBounds.bottom() - y);
            previousBlock = child;
        }

        // Align on the orthogonal axis
        alignChildren(LytAxis.HORIZONTAL, children, alignItems, x + paddingLeft, x + contentWidth);

        return new LytRect(
                x, y,
                contentWidth + paddingRight,
                contentHeight + paddingBottom);
    }

    /**
     * Lays out all children along the horizontal axis, and returns the bounding box of the content area.
     */
    public static LytRect horizontalLayout(
            LayoutContext context,
            List<LytBlock> children,
            int x, int y, int availableWidth,
            int paddingLeft, int paddingTop, int paddingRight, int paddingBottom,
            int gap,
            AlignItems alignItems) {
        // Margins have been applied outside
        // Paddings need to be considered here
        var innerX = x + paddingLeft;
        var innerY = y + paddingTop;
        var innerWidth = availableWidth - paddingLeft - paddingRight;

        // Layout children horizontally, without padding
        LytBlock previousBlock = null;
        var contentWidth = paddingLeft;
        var contentHeight = paddingTop;
        for (var child : children) {
            // Account for margins of the child, and margin collapsing
            innerX = offsetIntoContentArea(LytAxis.HORIZONTAL, innerX, previousBlock, child);
            var blockWidth = Math.max(1, innerWidth - contentWidth - child.getMarginLeft() - child.getMarginRight());
            var childBounds = child.layout(context, innerX, innerY + child.getMarginTop(), blockWidth);
            innerX += childBounds.width() + child.getMarginRight() + gap;
            contentWidth = Math.max(contentWidth, childBounds.right() - x);
            contentHeight = Math.max(contentHeight, childBounds.bottom() - y);
            previousBlock = child;

            if (innerX > innerWidth) {
                innerX = x + paddingLeft;
                innerY = y + contentHeight + gap;
            }
        }

        // Align on the orthogonal axis
        alignChildren(LytAxis.VERTICAL, children, alignItems, y + paddingTop, y + contentHeight);

        return new LytRect(
                x, y,
                contentWidth + paddingRight,
                contentHeight + paddingBottom);
    }

    private static void alignChildren(LytAxis axis, List<LytBlock> children, AlignItems alignItems, int start,
            int end) {
        var space = end - start;

        // Pass 2, align items
        for (var child : children) {
            var bounds = child.getBounds();
            var childSize = size(bounds, axis) + child.getMarginStart(axis) + child.getMarginEnd(axis);

            switch (alignItems) {
                case CENTER -> child.setLayoutPos(set(bounds.point(), axis, start + (space - childSize) / 2));
                case END -> child.setLayoutPos(set(bounds.point(), axis, end - childSize));
            }
        }
    }

    private static Vector2i set(Vector2i point, LytAxis axis, int pos) {
        return switch (axis) {
            case HORIZONTAL -> point.set(pos, point.y);
            case VERTICAL -> point.set(point.x, pos);
        };
    }

    private static int size(LytRect rect, LytAxis axis) {
        return switch (axis) {
            case HORIZONTAL -> rect.width();
            case VERTICAL -> rect.height();
        };
    }

    /**
     * Offsets position on the given axis into the content area of the child by adding the appropriate margin, while
     * accounting for potential collapsing of the margin with the previous block element.
     */
    private static int offsetIntoContentArea(LytAxis axis, int pos, LytBlock previousBlock, LytBlock child) {
        var previousMarginEnd = previousBlock != null ? previousBlock.getMarginEnd(axis) : 0;
        var childMarginStart = child.getMarginStart(axis);

        // Account for margins of the child, and margin collapsing
        if (previousMarginEnd > 0) {
            pos += Math.max(previousMarginEnd, childMarginStart) - previousMarginEnd;
        } else {
            pos += childMarginStart;
        }
        return pos;
    }
}
