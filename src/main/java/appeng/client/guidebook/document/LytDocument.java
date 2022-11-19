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

    public int getAvailableWidth() {
        return layout != null ? layout.availableWidth() : 0;
    }

    public int getContentHeight() {
        return layout != null ? layout.contentHeight() : 0;
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

    public void updateLayout(LayoutContext context, int availableWidth) {
        if (layout != null && layout.availableWidth == availableWidth) {
            return;
        }

        layout = createLayout(context, availableWidth);
    }

    private Layout createLayout(LayoutContext context, int availableWidth) {
        var contentY = 0;
        var contentHeight = 0;

        for (var block : blocks) {
            contentY += block.getMarginTop();
            var blockWidth = Math.max(1, availableWidth - block.getMarginLeft() - block.getMarginRight());
            var bounds = block.layout(context, block.getMarginLeft(), contentY, blockWidth);
            contentY += bounds.height() + block.getMarginBottom();
            contentHeight = Math.max(contentHeight, bounds.bottom());
        }

        return new Layout(availableWidth, contentHeight);
    }

    public void render(SimpleRenderContext context) {
        for (var block : blocks) {
            block.render(context);
        }
    }

    public record Layout(int availableWidth, int contentHeight) {
    }
}
