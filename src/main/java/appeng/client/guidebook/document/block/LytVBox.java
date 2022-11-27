package appeng.client.guidebook.document.block;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.layout.Layouts;

/**
 * Lays out its children vertically.
 */
public class LytVBox extends LytBox {
    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        return Layouts.verticalLayout(context,
                children,
                x,
                y,
                availableWidth,
                paddingLeft,
                paddingTop,
                paddingRight,
                paddingBottom);
    }
}
