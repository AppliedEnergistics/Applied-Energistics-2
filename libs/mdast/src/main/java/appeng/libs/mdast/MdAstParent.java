package appeng.libs.mdast;

import appeng.libs.unist.UnistParent;

import java.util.List;

/**
 * Parent (UnistParent) represents an abstract public interface in mdast containing other nodes (said to be children).
 * <p>
 * Its content is limited to only other mdast content.
 */
public interface MdAstParent extends UnistParent {
    List<? extends MdAstAnyContent> children();
}
