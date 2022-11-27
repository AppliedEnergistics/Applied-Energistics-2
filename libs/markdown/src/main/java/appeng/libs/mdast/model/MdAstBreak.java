package appeng.libs.mdast.model;

import appeng.libs.unist.UnistNode;

/**
 * Break (Node) represents a line break, such as in poems or addresses.
 * <p>
 * Break can be used where phrasing content is expected. It has no content model.
 * <p>
 * For example, the following markdown:
 * <p>
 * foo··
 * bar
 * <p>
 * Yields:
 * <p>
 * {
 * type: 'paragraph',
 * children: [
 * {type: 'text', value: 'foo'},
 * {type: 'break'},
 * {type: 'text', value: 'bar'}
 * ]
 * }
 */
public class MdAstBreak extends MdAstNode implements MdAstStaticPhrasingContent {
    public MdAstBreak() {
        super("break");
    }

    @Override
    public void toText(StringBuilder buffer) {
    }
}
