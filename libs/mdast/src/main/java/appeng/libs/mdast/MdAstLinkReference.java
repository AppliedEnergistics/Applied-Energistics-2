package appeng.libs.mdast;

import java.util.List;

/**
 * LinkReference (Parent) represents a hyperlink through association, or its original source if there is no association.
 * <p>
 * LinkReference can be used where phrasing content is expected. Its content model is static phrasing content.
 * <p>
 * LinkReferences should be associated with a Definition.
 * <p>
 * For example, the following markdown:
 * <p>
 * [alpha][Bravo]
 * <p>
 * Yields:
 * <p>
 * <pre>
 * {
 * type: 'linkReference',
 * identifier: 'bravo',
 * label: 'Bravo',
 * referenceType: 'full',
 * children: [{type: 'text', value: 'alpha'}]
 * }
 * </pre>
 */
public interface MdAstLinkReference extends MdAstParent, MdAstReference, MdAstPhrasingContent {
    default String type() {
        return "linkReference";
    }

    @Override
    List<MdAstStaticPhrasingContent> children();
}
