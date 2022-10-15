package appeng.libs.mdast;

import java.util.List;

/**
 * Strong (Parent) represents strong importance, seriousness, or urgency for its contents.
 * <p>
 * Strong can be used where phrasing content is expected. Its content model is transparent content.
 * <p>
 * For example, the following markdown:
 * <p>
 * **alpha** __bravo__
 * <p>
 * Yields:
 * <p>
 * <pre>
 * {
 * type: 'paragraph',
 * children: [
 * {
 * type: 'strong',
 * children: [{type: 'text', value: 'alpha'}]
 * },
 * {type: 'text', value: ' '},
 * {
 * type: 'strong',
 * children: [{type: 'text', value: 'bravo'}]
 * }
 * ]
 * }
 * </pre>
 */
public interface MdAstStrong extends MdAstParent, MdAstStaticPhrasingContent {
    @Override
    default String type() {
        return "strong";
    }

    @Override
    List<MdAstPhrasingContent> children();
}
