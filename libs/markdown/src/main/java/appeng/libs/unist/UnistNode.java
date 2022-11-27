package appeng.libs.unist;

import org.jetbrains.annotations.Nullable;

/**
 * A syntactic unit in unist syntax tree.
 */
public interface UnistNode {
    /**
     * Technical identifier of the type of this node.
     */
    String type();

    /**
     * Arbitrary data associated with this node.
     */
    @Nullable
    Object data();

    /**
     * The location in the source document.
     * Null if this node is generated.
     */
    @Nullable
    UnistPosition position();
}

