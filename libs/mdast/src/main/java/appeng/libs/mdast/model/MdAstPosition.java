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

    public static String stringify(Point start, Point end) {
        return start.line() + ":" + start.column() + "-"
                + end.line() + ":" + end.column();
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
