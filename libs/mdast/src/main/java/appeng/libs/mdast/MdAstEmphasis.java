package appeng.libs.mdast;

import java.util.List;

/**
 * Emphasis (Parent) represents stress emphasis of its contents.
 * Emphasis can be used where phrasing content is expected. Its content model is transparent content.
 * For example, the following markdown:
 * *alpha* _bravo_
 * Yields:
 * <pre>
 * {
 * type: 'paragraph',
 * children: [
 * {
 * type: 'emphasis',
 * children: [{type: 'text', value: 'alpha'}]
 * },
 * {type: 'text', value: ' '},
 * {
 * type: 'emphasis',
 * children: [{type: 'text', value: 'bravo'}]
 * }
 * ]
 * }
 * </pre>
 */
public interface MdAstEmphasis extends MdAstParent, MdAstStaticPhrasingContent {
    @Override
    default String type() {
        return "emhpasis";
    }

    @Override
    List<MdAstPhrasingContent> children();
}
