package appeng.client.guidebook.document.block;

import net.minecraft.client.renderer.MultiBufferSource;

import appeng.client.guidebook.document.DefaultStyles;
import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.RenderContext;
import appeng.client.guidebook.render.SymbolicColor;
import appeng.client.guidebook.style.ResolvedTextStyle;

public class LytListItem extends LytVBox {

    private static final int LEVEL_MARGIN = 10;

    private final ResolvedTextStyle style = DefaultStyles.BODY_TEXT.mergeWith(DefaultStyles.BASE_STYLE);

    private boolean isOrdered() {
        if (parent instanceof LytList list) {
            return list.isOrdered();
        }
        return false;
    }

    @Override
    protected LytRect computeBoxLayout(LayoutContext context, int x, int y, int availableWidth) {
        // Constraint child layout
        var margin = LEVEL_MARGIN;
        var bounds = super.computeBoxLayout(context, x + margin, y, availableWidth - margin);

        // Include the space we need for our list bullet in our bounds
        return bounds.expand(LEVEL_MARGIN, 0, 0, 0);
    }

    @Override
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {
        if (isOrdered()) {
            int number = getOrderedItemNumber();
            String label = number + ".";

            var width = context.getWidth(label, style);
            var bounds = getBounds();
            var x = bounds.x() + LEVEL_MARGIN - width - 2;

            context.renderTextInBatch(label,
                    style,
                    x, (float) bounds.y(), buffers);
        }

        super.renderBatch(context, buffers);
    }

    private int getOrderedItemNumber() {
        var number = 1;
        if (parent instanceof LytList list) {
            number = list.getStart();
            // Count precending list items on the same level
            for (var child : list.getChildren()) {
                if (child == this) {
                    break;
                }
                if (child instanceof LytListItem) {
                    number++;
                }
            }
        }
        return number;
    }

    @Override
    public void render(RenderContext context) {
        if (!isOrdered()) {
            var bounds = getBounds();

            context.fillRect(
                    bounds.x() + 5,
                    bounds.y() + 4,
                    2,
                    2,
                    SymbolicColor.BODY_TEXT.ref());
        }

        super.render(context);
    }
}
