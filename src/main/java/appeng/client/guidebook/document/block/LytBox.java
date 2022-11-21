package appeng.client.guidebook.document.block;

import appeng.client.guidebook.render.SimpleRenderContext;

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
    public void append(LytBlock node) {
        children.add(node);
    }

    @Override
    public List<? extends LytNode> getChildren() {
        return children;
    }

    @Override
    public void render(SimpleRenderContext context) {
        for (var child : children) {
            child.render(context);
        }
    }
}
