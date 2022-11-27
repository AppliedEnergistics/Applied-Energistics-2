package appeng.libs.mdast.model;

import appeng.libs.unist.UnistNode;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Represents a resource.
 * <p>
 * Definition can be used where content is expected. It has no content model.
 * <p>
 * Definition should be associated with LinkReferences and ImageReferences.
 * <p>
 * For example, the following markdown:
 * <p>
 * [Alpha]: https://example.com
 * <p>
 * Yields:
 * <p>
 * {
 * type: 'definition',
 * identifier: 'alpha',
 * label: 'Alpha',
 * url: 'https://example.com',
 * title: null
 * }
 */
public class MdAstDefinition extends MdAstNode implements MdAstAssociation, MdAstResource, MdAstContent {
    public String identifier = "";
    public String label;
    public String url = "";
    public String title;

    public MdAstDefinition() {
        super("definition");
    }

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public @Nullable String label() {
        return label;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public @Nullable String title() {
        return title;
    }

    @Override
    public void toText(StringBuilder buffer) {
        buffer.append(label);
    }

    @Override
    protected void writeJson(JsonWriter writer) throws IOException {
        if (identifier != null) {
            writer.name("identifier").value(identifier);
        }
        if (label != null) {
            writer.name("label").value(label);
        }
        if (title != null) {
            writer.name("title").value(title);
        }
        if (url != null) {
            writer.name("url").value(url);
        }

        super.writeJson(writer);
    }
}
