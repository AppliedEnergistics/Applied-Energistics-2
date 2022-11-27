package appeng.libs.mdast.model;

import appeng.libs.micromark.Point;
import appeng.libs.unist.UnistPoint;
import appeng.libs.unist.UnistPosition;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class MdAstPosition implements UnistPosition {
    public UnistPoint start;
    public UnistPoint end;
    int @Nullable [] indent; // number >= 1

    public MdAstPosition() {
    }

    public MdAstPosition(UnistPoint start, UnistPoint end) {
        this.start = start;
        this.end = end;
    }

    public static String stringify(UnistPoint point) {
        return point.line() + ":" + point.column();
    }

    public static String stringify(UnistPosition position) {
        return stringify(position.start(), position.end());
    }

    public static String stringify(UnistPoint start, UnistPoint end) {
        var result = new StringBuilder();
        if (start != null ) {
            result.append(start.line()).append(":").append(start.column());
        }
        if (end != null) {
            result.append("-").append(end.line()).append(":").append(end.column());
        }
        return result.toString();
    }

    @Override
    public UnistPoint start() {
        return start;
    }

    @Override
    public UnistPoint end() {
        return end;
    }

    @Override
    public int @Nullable [] indent() {
        return indent;
    }

    public MdAstPosition withStart(UnistPoint point) {
        this.start = point;
        return this;
    }

    public MdAstPosition withEnd(UnistPoint point) {
        this.end = point;
        return this;
    }

    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("start");
        start.writeJson(writer);
        writer.name("end");
        end.writeJson(writer);
        writer.endObject();
    }
}
