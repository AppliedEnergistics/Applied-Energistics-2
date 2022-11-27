package appeng.libs.mdast.model;

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
public class MdAstStrong extends MdAstParent<MdAstPhrasingContent> implements MdAstStaticPhrasingContent {
    public MdAstStrong() {
        super("strong");
    }

    @Override
    protected Class<MdAstPhrasingContent> childClass() {
        return MdAstPhrasingContent.class;
    }
}
