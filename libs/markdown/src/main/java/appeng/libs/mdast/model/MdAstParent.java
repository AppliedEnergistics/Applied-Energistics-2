package appeng.libs.mdast.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.google.gson.stream.JsonWriter;

import org.jetbrains.annotations.Nullable;

import appeng.libs.mdast.MdAstVisitor;
import appeng.libs.unist.UnistParent;

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

    public void removeChild(MdAstNode node) {
        children.remove(childClass().cast(node));
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

    public void replaceChild(MdAstNode child, @Nullable MdAstNode replacement) {
        var replacementChild = replacement != null ? childClass().cast(replacement) : null;

        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) == child) {
                if (replacement == null) {
                    children.remove(i);
                } else {
                    children.set(i, replacementChild);
                }
                return;
            }
        }

        throw new IllegalStateException("Child " + child + " not found");
    }

    @Override
    protected MdAstVisitor.Result visitChildren(MdAstVisitor visitor) {
        for (var child : children()) {
            if (child instanceof MdAstNode childNode && childNode.visit(visitor) == MdAstVisitor.Result.STOP) {
                return MdAstVisitor.Result.STOP;
            }
        }
        return MdAstVisitor.Result.CONTINUE;
    }

    /**
     * Remove children matching the given predicate.
     */
    @Override
    public void removeChildren(Predicate<MdAstNode> predicate, boolean recursive) {
        for (var it = children.iterator(); it.hasNext();) {
            var child = it.next();
            if (child instanceof MdAstNode astNode) {
                if (predicate.test(astNode)) {
                    it.remove();
                } else {
                    astNode.removeChildren(predicate, recursive);
                }
            }
        }
    }
}
