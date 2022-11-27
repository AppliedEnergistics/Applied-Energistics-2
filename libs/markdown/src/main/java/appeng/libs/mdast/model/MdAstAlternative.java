package appeng.libs.mdast.model;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a node with a fallback.
 * <p>
 * An alt field should be present. It represents equivalent content for environments that cannot represent the node
 * as intended.
 */
public interface MdAstAlternative {
    @Nullable
    String alt();
}
