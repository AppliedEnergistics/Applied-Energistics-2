package appeng.client.guidebook.document.block;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.layout.LayoutContext;

/**
 * Lays out its children vertically.
 */
public class LytVBox extends LytBox {
    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        // Layout children vertically
        var bounds = LytRect.empty();
        for (var child : children) {
            var childBounds = child.layout(context, x, bounds.isEmpty() ? y : bounds.bottom(), availableWidth);
            bounds = LytRect.union(bounds, childBounds);
        }
        return bounds;
    }
}
