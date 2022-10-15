package appeng.libs.mdast;

import java.util.List;

/**
 * Blockquote (Parent) represents a section quoted from somewhere else.
 * <p>
 * Blockquote can be used where flow content is expected. Its content model is also flow content.
 * <p>
 * For example, the following markdown:
 * <p>
 * > Alpha bravo charlie.
 * <p>
 * Yields:
 * <p>
 * {
 * type: 'blockquote',
 * children: [{
 * type: 'paragraph',
 * children: [{type: 'text', value: 'Alpha bravo charlie.'}]
 * }]
 * }
 */
public interface MdAstBlockquote extends MdAstParent, MdAstFlowContent {
    default String type() {
        return "blockquote";
    }

    List<MdAstFlowContent> children();
}
