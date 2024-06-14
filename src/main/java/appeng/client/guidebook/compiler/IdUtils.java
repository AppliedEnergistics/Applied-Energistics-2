package appeng.client.guidebook.compiler;

import java.net.URI;

import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

/**
 * Helper to resolve shorthand and relative IDs found in markdown pages.
 */
public final class IdUtils {

    private IdUtils() {
    }

    public static ResourceLocation resolveId(String idText, String defaultNamespace) {
        if (!idText.contains(":")) {
            return ResourceLocation.fromNamespaceAndPath(defaultNamespace, idText);
        }
        return ResourceLocation.parse(idText);
    }

    /**
     * Supports relative resource locations such as: ./somepath, which would resolve relative to a given anchor
     * location. Relative locations must not be namespaced since we would otherwise run into the problem if namespaced
     * locations potentially having a different namespace than the anchor.
     */
    public static ResourceLocation resolveLink(String idText, ResourceLocation anchor)
            throws ResourceLocationException {
        if (idText.startsWith("/")) {
            // Absolute path, but relative to namespace
            return ResourceLocation.fromNamespaceAndPath(anchor.getNamespace(), idText.substring(1));
        } else if (!idText.contains(":")) {
            URI uri = URI.create(anchor.getPath());
            uri = uri.resolve(idText);

            var relativeId = uri.toString();

            return ResourceLocation.fromNamespaceAndPath(anchor.getNamespace(), relativeId);
        }

        // if it contains a ":" it's assumed to be absolute
        return ResourceLocation.parse(idText);
    }

}
