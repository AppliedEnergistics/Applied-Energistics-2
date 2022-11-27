package appeng.client.guidebook.compiler;

import java.net.URI;

import net.minecraft.resources.ResourceLocation;

/**
 * Helper to resolve shorthand and relative IDs found in markdown pages.
 */
public final class IdUtils {

    private IdUtils() {
    }

    public static ResourceLocation resolveId(String idText, String defaultNamespace) {
        if (!idText.contains(":")) {
            return new ResourceLocation(defaultNamespace, idText);
        }
        return new ResourceLocation(idText);
    }

    /**
     * Supports relative resource locations such as: ./somepath, which would resolve relative to a given anchor
     * location. Relative locations must not be namespaced since we would otherwise run into the problem if namespaced
     * locations potentially having a different namespace than the anchor.
     */
    public static ResourceLocation resolveLink(String idText, ResourceLocation anchor) {
        if (!idText.contains(":")) {
            URI uri = URI.create(anchor.getPath());
            uri = uri.resolve(idText);

            var relativeId = uri.toString();

            return new ResourceLocation(anchor.getNamespace(), relativeId);
        }

        // if it contains a ":" it's assumed to be absolute
        return new ResourceLocation(idText);
    }

}
