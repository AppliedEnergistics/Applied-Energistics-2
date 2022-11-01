package appeng.libs.mdast.model;

import appeng.libs.unist.UnistLiteral;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Literal (UnistLiteral) represents an abstract public interface in mdast containing a value.
 * <p>
 * Its value field is a string.
 */
public class MdAstLiteral extends MdAstNode implements UnistLiteral {
    public String value = "";

    public MdAstLiteral(String type) {
        super(type);
    }

    @Override
    public void toText(StringBuilder buffer) {
        buffer.append(value);
    }

    @Override
    public String value() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.name("value").value(value);
    }
}
