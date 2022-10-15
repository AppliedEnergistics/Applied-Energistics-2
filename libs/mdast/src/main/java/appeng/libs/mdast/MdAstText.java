package appeng.libs.mdast;

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
public interface MdAstText extends MdAstLiteral, MdAstStaticPhrasingContent {
    default String type() {
        return "text";
    }
}
