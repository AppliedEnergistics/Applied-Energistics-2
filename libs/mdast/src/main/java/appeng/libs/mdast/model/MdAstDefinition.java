package appeng.libs.mdast.model;

import appeng.libs.unist.UnistNode;
import org.jetbrains.annotations.Nullable;

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
}
