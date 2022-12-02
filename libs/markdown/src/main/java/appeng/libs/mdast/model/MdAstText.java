package appeng.libs.mdast.model;

/**
 * Represents everything that is just text.
 * <p>
 * Text can be used where phrasing content is expected. Its content is represented by its value field.
 * <p>
 * For example, the following markdown:
 * <p>
 * Alpha bravo charlie.
 * <p>
 * Yields:
 * <p>
 * {type: 'text', value: 'Alpha bravo charlie.'}
 */
public class MdAstText extends MdAstLiteral implements MdAstStaticPhrasingContent {
    public MdAstText() {
        super("text");
    }
}
