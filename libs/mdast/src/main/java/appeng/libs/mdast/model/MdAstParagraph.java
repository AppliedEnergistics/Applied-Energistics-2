package appeng.libs.mdast.model;

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
public class MdAstParagraph extends MdAstParent<MdAstPhrasingContent> implements MdAstContent {
    public MdAstParagraph() {
        super("paragraph");
    }

    @Override
    protected Class<MdAstPhrasingContent> childClass() {
        return MdAstPhrasingContent.class;
    }
}
