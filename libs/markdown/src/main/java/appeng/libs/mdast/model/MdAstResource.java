package appeng.libs.mdast.model;

import org.jetbrains.annotations.Nullable;

/**
 * A reference to a resource.
 */
public interface MdAstResource {
    /**
     * The URL of the referenced resource.
     */
    String url();

    /**
     * Advisory information for the resource, such as would be appropriate for a tooltip.
     */
    @Nullable
    String title();
}
