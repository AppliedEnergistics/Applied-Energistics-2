package appeng.libs.mdast.gfm.model;

import java.io.IOException;
import java.util.List;

import com.google.gson.stream.JsonWriter;

import org.jetbrains.annotations.Nullable;

import appeng.libs.mdast.model.MdAstFlowContent;
import appeng.libs.mdast.model.MdAstParent;
import appeng.libs.micromark.extensions.gfm.Align;

public class GfmTable extends MdAstParent<GfmTableRow> implements MdAstFlowContent {
    @Nullable
    public List<Align> align = null;

    public GfmTable() {
        super("table");
    }

    @Override
    protected Class<GfmTableRow> childClass() {
        return GfmTableRow.class;
    }

    @Override
    protected void writeJson(JsonWriter writer) throws IOException {
        if (align != null) {
            writer.name("align").beginArray();
            for (var value : align) {
                switch (value) {
                    case LEFT -> writer.value("left");
                    case CENTER -> writer.value("center");
                    case RIGHT -> writer.value("right");
                    case NONE -> writer.nullValue();
                }
            }
            writer.endArray();
        }

        super.writeJson(writer);
    }
}
