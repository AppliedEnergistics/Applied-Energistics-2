package appeng.libs.mdast.model;

import java.io.IOException;
import java.util.function.Predicate;

import com.google.gson.stream.JsonWriter;

import org.jetbrains.annotations.Nullable;

import appeng.libs.mdast.MdAstVisitor;
import appeng.libs.unist.UnistNode;
import appeng.libs.unist.UnistPosition;

public abstract class MdAstNode implements UnistNode {
    private final String type;
    public Object data;
    public MdAstPosition position;

    public MdAstNode(String type) {
        this.type = type;
    }

    @Override
    public final String type() {
        return type;
    }

    @Override
    public @Nullable Object data() {
        return data;
    }

    @Override
    public @Nullable UnistPosition position() {
        return position;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public abstract void toText(StringBuilder buffer);

    public final String toText() {
        var builder = new StringBuilder();
        toText(builder);
        return builder.toString();
    }

    public final void toJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type").value(type());
        writeJson(writer);
        if (position != null) {
            writer.name("position");
            position.writeJson(writer);
        }
        writer.endObject();
    }

    protected void writeJson(JsonWriter writer) throws IOException {
    }

    public final MdAstVisitor.Result visit(MdAstVisitor visitor) {
        var result = visitor.beforeNode(this);
        if (result == MdAstVisitor.Result.STOP) {
            return result;
        }
        if (result != MdAstVisitor.Result.SKIP_CHILDREN) {
            if (visitChildren(visitor) == MdAstVisitor.Result.STOP) {
                return MdAstVisitor.Result.STOP;
            }
        }
        return visitor.afterNode(this);
    }

    protected MdAstVisitor.Result visitChildren(MdAstVisitor visitor) {
        return MdAstVisitor.Result.CONTINUE;
    }

    /**
     * Remove children matching the given predicate.
     */
    public void removeChildren(Predicate<MdAstNode> node, boolean recursive) {
    }

}
