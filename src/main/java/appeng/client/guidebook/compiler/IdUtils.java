package appeng.client.guidebook.compiler;

import appeng.core.AppEng;
import net.minecraft.resources.ResourceLocation;

import java.net.URI;

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
            if (idText.startsWith(RELATIVE_PREFIX) || idText.contains("../")) {
                URI uri = URI.create(anchor.getPath());
                uri = uri.resolve(idText);

                var relativeId = uri.toString();

                return new ResourceLocation(anchor.getNamespace(), relativeId);
            } else {
                return new ResourceLocation(anchor.getNamespace(), idText);
            }
        }

        // if it contains a ":" it's assumed to be absolute
        return new ResourceLocation(idText);
    }

}
