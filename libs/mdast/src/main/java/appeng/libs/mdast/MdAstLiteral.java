package appeng.libs.mdast;

import appeng.libs.unist.UnistLiteral;

/**
 * Literal (UnistLiteral) represents an abstract public interface in mdast containing a value.
 * <p>
 * Its value field is a string.
 */
public interface MdAstLiteral extends UnistLiteral {
    String value();
}
