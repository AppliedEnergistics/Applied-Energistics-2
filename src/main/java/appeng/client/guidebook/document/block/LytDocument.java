package appeng.client.guidebook.document.block;

import java.util.ArrayList;
import java.util.List;

import appeng.client.guidebook.layout.Layouts;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.MultiBufferSource;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.flow.LytFlowContainer;
import appeng.client.guidebook.document.flow.LytFlowContent;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.SimpleRenderContext;

/**
 * Layout document. Has a viewport and an overall size which may exceed the document size vertically, but not
 * horizontally.
 */
public class LytDocument extends LytNode implements LytBlockContainer {
    private final List<LytBlock> blocks = new ArrayList<>();

    @Nullable
    private Layout layout;

    @Nullable
    private HitTestResult hoveredElement;

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
    public LytRect getBounds() {
        return layout != null ? new LytRect(0, 0, layout.availableWidth, layout.contentHeight) : null;
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
        var bounds = Layouts.verticalLayout(context,
                blocks,
                0,
                0,
                availableWidth,
                5,
                5,
                5,
                5);

        return new Layout(availableWidth, bounds.height());
    }

    public void render(SimpleRenderContext context) {
        for (var block : blocks) {
            if (!block.getBounds().intersects(context.viewport())) {
                continue;
            }
            block.render(context);
        }
    }

    public void renderBatch(SimpleRenderContext context, MultiBufferSource buffers) {
        for (var block : blocks) {
            if (!block.getBounds().intersects(context.viewport())) {
                continue;
            }
            block.renderBatch(context, buffers);
        }
    }

    public HitTestResult getHoveredElement() {
        return hoveredElement;
    }

    public void setHoveredElement(HitTestResult hoveredElement) {
        if (hoveredElement != this.hoveredElement) {
            if (this.hoveredElement != null) {
                this.hoveredElement.node.onMouseLeave();
            }
            this.hoveredElement = hoveredElement;
            if (this.hoveredElement != null) {
                this.hoveredElement.node.onMouseEnter(hoveredElement.content());
            }
        }
    }

    public HitTestResult pick(int x, int y) {
        var node = pickNode(x, y);
        if (node != null) {
            LytFlowContent content = null;
            if (node instanceof LytFlowContainer container) {
                content = container.pickContent(x, y);
            }
            return new HitTestResult(node, content);
        }

        return null;
    }

    @Override
    public void onMouseEnter(@Nullable LytFlowContent hoveredContent) {
    }

    public record Layout(int availableWidth, int contentHeight) {
    }

    public record HitTestResult(LytNode node, @Nullable LytFlowContent content) {
    }
}
