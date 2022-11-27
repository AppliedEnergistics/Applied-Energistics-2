package appeng.libs.mdast.model;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Heading (Parent) represents a heading of a section.
 * <p>
 * Heading can be used where flow content is expected. Its content model is phrasing content.
 * <p>
 * For example, the following markdown:
 * <p>
 * # Alpha
 * <p>
 * Yields:
 * <p>
 * {
 * type: 'heading',
 * depth: 1,
 * children: [{type: 'text', value: 'Alpha'}]
 * }
 */
public class MdAstHeading extends MdAstParent<MdAstPhrasingContent> implements MdAstFlowContent {
    /**
     * Ranges from 1 to 6.
     * 1 is the highest level heading, 6 the lowest.
     */
    public int depth;

    public MdAstHeading() {
        super("heading");
    }

    @Override
    protected void writeJson(JsonWriter writer) throws IOException {
        writer.name("depth").value(depth);
        super.writeJson(writer);
    }

    @Override
    protected Class<MdAstPhrasingContent> childClass() {
        return MdAstPhrasingContent.class;
    }
}
