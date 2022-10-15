package appeng.libs.mdast;

/**
 * Represents a fragment of raw HTML.
 * <p>
 * HTML can be used where flow or phrasing content is expected. Its content is represented by its value field.
 * <p>
 * HTML nodes do not have the restriction of being valid or complete HTML ([HTML]) constructs.
 * <p>
 * For example, the following markdown:
 * <p>
 * &lt;div>
 * <p>
 * Yields:
 * <p>
 * {type: 'html', value: '&lt;div>'}
 */
public interface MdAstHTML extends MdAstLiteral, MdAstFlowContent, MdAstStaticPhrasingContent {
    @Override
    default String type() {
        return "html";
    }
}
