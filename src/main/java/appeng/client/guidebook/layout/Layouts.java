package appeng.client.guidebook.layout;

import java.util.List;

import appeng.client.guidebook.document.LytRect;
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
            int gap) {
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
            // Account for margins of the child, and margin collapsing
            if (previousBlock != null && previousBlock.getMarginBottom() > 0) {
                innerY += Math.max(previousBlock.getMarginBottom(), child.getMarginTop())
                        - previousBlock.getMarginBottom();
            } else {
                innerY += child.getMarginTop();
            }
            // Block width is the width available for the inner content area of the child
            var blockWidth = Math.max(1, innerWidth - child.getMarginLeft() - child.getMarginRight());
            var childBounds = child.layout(context, innerX + child.getMarginLeft(), innerY, blockWidth);
            innerY += childBounds.height() + child.getMarginBottom() + gap;
            contentWidth = Math.max(contentWidth, childBounds.right() - x);
            contentHeight = Math.max(contentHeight, childBounds.bottom() - y);
            previousBlock = child;
        }

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
            int gap) {
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
            if (previousBlock != null && previousBlock.getMarginBottom() > 0) {
                innerX += Math.max(previousBlock.getMarginBottom(), child.getMarginTop())
                        - previousBlock.getMarginBottom();
            } else {
                innerX += child.getMarginTop();
            }
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

        return new LytRect(
                x, y,
                contentWidth + paddingRight,
                contentHeight + paddingBottom);
    }

}
