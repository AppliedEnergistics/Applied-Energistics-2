package appeng.libs.mdast.model;

import appeng.libs.unist.UnistNode;
import appeng.libs.unist.UnistPosition;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

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
}
