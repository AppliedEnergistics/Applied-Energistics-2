package appeng.libs.mdast;

import appeng.libs.unist.UnistNode;

/**
 * Image (Node) represents an image.
 * <p>
 * Image can be used where phrasing content is expected. It has no content model, but is described by its alt field.
 * <p>
 * Image includes the mixins Resource and Alternative.
 * <p>
 * For example, the following markdown:
 * <p>
 * ![alpha](https://example.com/favicon.ico "bravo")
 * <p>
 * Yields:
 * <p>
 * {
 * type: 'image',
 * url: 'https://example.com/favicon.ico',
 * title: 'bravo',
 * alt: 'alpha'
 * }
 */
public interface MdAstImage extends UnistNode, MdAstResource, MdAstAlternative, MdAstStaticPhrasingContent {
    default String type() {
        return "image";
    }
}
