package appeng.libs.mdast;

/**
 * Root (Parent) represents a document.
 * <p>
 * Root can be used as the root of a tree, never as a child. Its content model is not limited to flow content,
 * but instead can contain any mdast content with the restriction that all content must be of the same category.
 */
public interface MdAstRoot extends MdAstParent {
    default String type() {
        return "root";
    }
}
