package appeng.libs.unist;

import java.util.List;

/**
 * A node that contains children.
 */
public interface UnistParent extends UnistNode {
    /**
     * @return The children of this node.
     */
    List<? extends UnistNode> children();
}
