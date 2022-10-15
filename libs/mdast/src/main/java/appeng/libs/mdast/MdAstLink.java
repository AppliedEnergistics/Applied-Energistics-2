package appeng.libs.mdast;

import java.util.List;

/**
 * Link includes Resource
 * <p>
 * Link (Parent) represents a hyperlink.
 * <p>
 * Link can be used where phrasing content is expected. Its content model is static phrasing content.
 * <p>
 * Link includes the mixin Resource.
 * <p>
 * For example, the following markdown:
 * <p>
 * [alpha](https://example.com "bravo")
 * <p>
 * Yields:
 * <p>
 * {
 * type: 'link',
 * url: 'https://example.com',
 * title: 'bravo',
 * children: [{type: 'text', value: 'alpha'}]
 * }
 */
public interface MdAstLink extends MdAstParent, MdAstPhrasingContent {
    @Override
    default String type() {
        return "link";
    }

    @Override
    List<MdAstStaticPhrasingContent> children();
}
