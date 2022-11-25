package appeng.client.guidebook.compiler;

import appeng.core.AppEng;
import net.minecraft.resources.ResourceLocation;

/**
 * Helper to resolve shorthand and relative IDs found in markdown pages.
 */
public final class IdUtils {

    private static final String RELATIVE_PREFIX = "./";

    private IdUtils() {
    }

    public static ResourceLocation resolve(String idText) {
        if (!idText.contains(":")) {
            return new ResourceLocation(AppEng.MOD_ID, idText);
        }
        return new ResourceLocation(idText);
    }

    /**
     * Supports relative resource locations such as:
     * ./somepath, which would resolve relative to a given anchor location.
     * Relative locations must not be namespaced since we would otherwise run into the
     * problem if namespaced locations potentially having a different namespace than the anchor.
     */
    public static ResourceLocation resolve(String idText, ResourceLocation anchor) {
        if (!idText.contains(":")) {
            if (idText.startsWith(RELATIVE_PREFIX)) {
                var relativeId = idText.substring(RELATIVE_PREFIX.length());
                var anchorPath = anchor.getPath();
                // If the anchor has no separator, assume relative to the root
                var lastSlashInAnchor = anchorPath.lastIndexOf('/');
                if (lastSlashInAnchor == -1) {
                    return new ResourceLocation(anchor.getNamespace(), relativeId);
                } else {
                    // Otherwise strip everything past the last /
                    return new ResourceLocation(
                            anchor.getNamespace(),
                            anchor.getPath().substring(0, lastSlashInAnchor + 1) + relativeId
                    );
                }
            } else {
                return new ResourceLocation(anchor.getNamespace(), idText);
            }
        }

        // if it contains a ":" it's assumed to be absolute
        return new ResourceLocation(idText);
    }

}
