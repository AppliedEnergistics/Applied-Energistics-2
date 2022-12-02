package appeng.libs.unist;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Represents one point in the source file.
 */
public interface UnistPoint {
    /**
     * The 1-based index of the line in the source-file.
     */
    int line();

    /**
     * The 1-based index of the column in the source-file.
     */
    int column();

    /**
     * The 0-based offset to the character in the source-file.
     */
    int offset();

    default void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("line");
        writer.value(line());
        writer.name("column");
        writer.value(column());
        writer.name("offset");
        writer.value(offset());
        writer.endObject();
    }
}
