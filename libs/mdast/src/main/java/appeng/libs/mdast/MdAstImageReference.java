package appeng.libs.mdast;

import appeng.libs.unist.UnistNode;

/**
 * ImageReference (Node) represents an image through association, or its original source if there is no association.
 * <p>
 * ImageReference can be used where phrasing content is expected. It has no content model, but is described by its alt field.
 * <p>
 * ImageReference should be associated with a Definition.
 * <p>
 * For example, the following markdown:
 * <p>
 * ![alpha][bravo]
 * <p>
 * Yields:
 * <p>
 * <pre>
 * {
 * type: 'imageReference',
 * identifier: 'bravo',
 * label: 'bravo',
 * referenceType: 'full',
 * alt: 'alpha'
 * }
 * </pre>
 */
public interface MdAstImageReference extends UnistNode, MdAstReference, MdAstAlternative, MdAstStaticPhrasingContent {
    default String type() {
        return "imageReference";
    }
}
