package appeng.client.guidebook.document.block;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.layout.Layouts;

/**
 * Lays out its children vertically.
 */
public class LytHBox extends LytAxisBox {
    @Override
    protected LytRect computeBoxLayout(LayoutContext context, int x, int y, int availableWidth) {
        // Padding is applied through the parent
        return Layouts.horizontalLayout(context,
                children,
                x,
                y,
                availableWidth,
                0,
                0,
                0,
                0,
                getGap(),
                getAlignItems());
    }
}
