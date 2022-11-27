package appeng.libs.mdast.model;

/**
 * ThematicBreak (Node) represents a thematic break, such as a scene change in a story, a transition to another topic, or a new document.
 * <p>
 * ThematicBreak can be used where flow content is expected. It has no content model.
 * <p>
 * For example, the following markdown:
 * <p>
 * ***
 * <p>
 * Yields:
 * <p>
 * {type: 'thematicBreak'}
 */
public class MdAstThematicBreak extends MdAstNode implements MdAstFlowContent {
    public MdAstThematicBreak() {
        super("thematicBreak");
    }

    @Override
    public void toText(StringBuilder buffer) {
    }
}
