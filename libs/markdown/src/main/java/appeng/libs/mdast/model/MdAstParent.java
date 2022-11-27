package appeng.libs.mdast.model;

import appeng.libs.unist.UnistParent;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parent (UnistParent) represents an abstract public interface in mdast containing other nodes (said to be children).
 * <p>
 * Its content is limited to only other mdast content.
 */
public abstract class MdAstParent<T extends MdAstAnyContent> extends MdAstNode implements UnistParent {
    private final List<T> children;

    public MdAstParent(String type) {
        super(type);
        this.children = new ArrayList<>();
    }

    @Override
    public List<T> children() {
        return children;
    }

    protected abstract Class<T> childClass();

    public void addChild(MdAstNode node) {
        if (!childClass().isInstance(node)) {
            throw new IllegalArgumentException("Cannot add a node of type " + node.getClass() + " to " + this);
        }
        children.add(childClass().cast(node));
    }

    @Override
    protected void writeJson(JsonWriter writer) throws IOException {
        writer.name("children");
        writer.beginArray();
        for (T child : children) {
            ((MdAstNode) child).toJson(writer);
        }
        writer.endArray();
    }

    @Override
    public void toText(StringBuilder buffer) {
        for (var child : children) {
            if (child instanceof MdAstNode childNode) {
                childNode.toText(buffer);
            }
        }
    }

    public void replaceChild(MdAstNode child, MdAstNode replacement) {
        var replacementChild = childClass().cast(replacement);

        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) == child) {
                children.set(i, replacementChild);
                return;
            }
        }

        throw new IllegalStateException("Child " + child + " not found");
    }
}
