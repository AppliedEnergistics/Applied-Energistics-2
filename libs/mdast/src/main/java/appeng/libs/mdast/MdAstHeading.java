package appeng.libs.mdast;

import java.util.List;

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
public interface MdAstHeading extends MdAstParent, MdAstFlowContent {
    default String type() {
        return "heading";
    }

    /**
     * Ranges from 1 to 6.
     * 1 is the highest level heading, 6 the lowest.
     */
    int depth();

    List<MdAstPhrasingContent> children();
}
