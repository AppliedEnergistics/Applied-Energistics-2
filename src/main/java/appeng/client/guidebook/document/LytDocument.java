package appeng.client.guidebook.document;

import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.SimpleRenderContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


/**
 * Layout document. Has a viewport and an overall size which may exceed the document size vertically, but not
 * horizontally.
 */
public class LytDocument extends LytNode implements LytBlockContainer {
    private final List<LytBlock> blocks = new ArrayList<>();

    @Nullable
    private Layout layout;

    public LytRect getBounds() {
        return layout != null ? layout.bounds() : LytRect.empty();
    }

    public List<LytBlock> getBlocks() {
        return blocks;
    }

    @Override
    public List<LytBlock> getChildren() {
        return blocks;
    }

    @Override
    public void removeChild(LytNode node) {
        if (node instanceof LytBlock block) {
            blocks.remove(block);
        }
    }

    @Override
    public void append(LytBlock block) {
        if (block.parent != null) {
            block.parent.removeChild(block);
        }
        block.parent = this;
        blocks.add(block);
    }

    public void updateLayout(LayoutContext context) {
        if (layout != null && layout.context.equals(context)) {
            return;
        }

        layout = createLayout(context);
    }

    private Layout createLayout(LayoutContext context) {
        var contentY = context.available().y();
        var contentHeight = 0;

        var currentContext = context;
        for (var block : blocks) {
            block.layout(currentContext);
            contentY += block.bounds.height();
            currentContext = currentContext.withAvailable(new LytRect(
                    context.available().x(),
                    contentY,
                    context.available().width(),
                    context.available().height() - block.bounds.height()
            ));
            contentHeight += block.bounds.height();
        }

        var bounds = context.available().withHeight(contentHeight);
        return new Layout(context, bounds);
    }

    public void render(SimpleRenderContext context) {
        for (var block : blocks) {
            block.render(context);
        }
    }

    public record Layout(LayoutContext context, LytRect bounds) {
    }
}
