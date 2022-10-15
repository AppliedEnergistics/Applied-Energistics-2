package appeng.libs.mdast;

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
public interface MdAstBreak extends UnistNode, MdAstStaticPhrasingContent {
    @Override
    default String type() {
        return "break";
    }
}
