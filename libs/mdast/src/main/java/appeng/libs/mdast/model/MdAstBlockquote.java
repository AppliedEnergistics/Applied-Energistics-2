package appeng.libs.mdast.model;

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
public class MdAstBlockquote extends MdAstParent<MdAstFlowContent> implements MdAstFlowContent {
    public MdAstBlockquote() {
        super("blockquote");
    }

    @Override
    protected Class<MdAstFlowContent> childClass() {
        return MdAstFlowContent.class;
    }
}
