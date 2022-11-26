package appeng.client.guidebook.document.block;

import appeng.client.guidebook.render.RenderContext;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.ArrayList;
import java.util.List;

public abstract class LytBox extends LytBlock implements LytBlockContainer {
    protected final List<LytBlock> children = new ArrayList<>();

    @Override
    public void removeChild(LytNode node) {
        if (node instanceof LytBlock block && block.parent == this) {
            children.remove(block);
            block.parent = null;
        }
    }

    @Override
    public void append(LytBlock block) {
        if (block.parent != null) {
            block.parent.removeChild(block);
        }
        block.parent = this;
        children.add(block);
    }

    @Override
    public List<? extends LytNode> getChildren() {
        return children;
    }

    @Override
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {
        for (var child : children) {
            child.renderBatch(context, buffers);
        }
    }

    @Override
    public void render(RenderContext context) {
        for (var child : children) {
            child.render(context);
        }
    }
}
