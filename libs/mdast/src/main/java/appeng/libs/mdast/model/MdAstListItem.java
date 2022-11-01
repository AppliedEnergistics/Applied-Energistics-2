package appeng.libs.mdast.model;

import java.util.List;

/**
 * An item in a {@link MdAstList}.
 * <p>
 * It can be used where list content is expected. Its content model is flow content.
 * <p>
 * For example, the following markdown:
 * <p>
 * * bar
 * <p>
 * Yields:
 * <p>
 * {
 * type: 'listItem',
 * spread: false,
 * children: [{
 * type: 'paragraph',
 * children: [{type: 'text', value: 'bar'}]
 * }]
 * }
 */
public class MdAstListItem extends MdAstParent<MdAstFlowContent> implements MdAstListContent {
    public MdAstListItem() {
        super("listItem");
    }

    /**
     * Represents that the item contains two or more children separated by a blank line (when true),
     * or not (when false).
     */
    public boolean spread;

    @Override
    protected Class<MdAstFlowContent> childClass() {
        return MdAstFlowContent.class;
    }
}
