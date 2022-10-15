package appeng.libs.mdast;

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
public interface MdAstListItem extends MdAstParent, MdAstListContent {
    @Override
    default String type() {
        return "listItem";
    }

    /**
     * Represents that the item contains two or more children separated by a blank line (when true),
     * or not (when false).
     */
    default boolean spread() {
        return false;
    }

    @Override
    List<MdAstFlowContent> children();
}
