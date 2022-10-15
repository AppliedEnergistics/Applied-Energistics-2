package appeng.libs.mdast;

import java.util.List;

/**
 * Paragraph (Parent) represents a unit of discourse dealing with a particular point or idea.
 * <p>
 * Paragraph can be used where content is expected. Its content model is phrasing content.
 * <p>
 * For example, the following markdown:
 * <p>
 * Alpha bravo charlie.
 * <p>
 * Yields:
 * <p>
 * {
 * type: 'paragraph',
 * children: [{type: 'text', value: 'Alpha bravo charlie.'}]
 * }
 */
public interface MdAstParagraph extends MdAstParent, MdAstContent {
    default String type() {
        return "paragraph";
    }

    List<MdAstPhrasingContent> children();
}
