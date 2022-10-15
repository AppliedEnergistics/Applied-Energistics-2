package appeng.libs.mdast;

import appeng.libs.unist.UnistNode;

/**
 * Represents a resource.
 * <p>
 * Definition can be used where content is expected. It has no content model.
 * <p>
 * Definition should be associated with LinkReferences and ImageReferences.
 * <p>
 * For example, the following markdown:
 * <p>
 * [Alpha]: https://example.com
 * <p>
 * Yields:
 * <p>
 * {
 * type: 'definition',
 * identifier: 'alpha',
 * label: 'Alpha',
 * url: 'https://example.com',
 * title: null
 * }
 */
public interface MdAstDefinition extends UnistNode, MdAstAssociation, MdAstResource, MdAstContent {
    default String type() {
        return "definition";
    }
}
