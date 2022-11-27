package appeng.client.guidebook.layout;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.block.LytBlock;

import java.util.List;

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
            int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
        // Margins have been applied outside
        // Paddings need to be considered here
        var innerX = x + paddingLeft;
        var innerY = y + paddingTop;
        var innerWidth = availableWidth - paddingLeft - paddingRight;

        // Layout children vertically, without padding
        LytBlock previousBlock = null;
        var contentHeight = paddingTop;
        for (var child : children) {
            // Account for margins of the child, and margin collapsing
            if (previousBlock != null && previousBlock.getMarginBottom() > 0) {
                innerY += Math.max(previousBlock.getMarginBottom(), child.getMarginTop()) - previousBlock.getMarginBottom();
            } else {
                innerY += child.getMarginTop();
            }
            var blockWidth = Math.max(1, innerWidth - child.getMarginLeft() - child.getMarginRight());
            var childBounds = child.layout(context, innerX + child.getMarginLeft(), innerY, blockWidth);
            innerY += childBounds.height() + child.getMarginBottom();
            contentHeight = Math.max(contentHeight, childBounds.bottom() - y);
            previousBlock = child;
        }

        return new LytRect(
                x, y,
                availableWidth,
                contentHeight + paddingBottom
        );
    }

}
