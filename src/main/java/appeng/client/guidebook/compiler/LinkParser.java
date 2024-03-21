package appeng.client.guidebook.compiler;

import java.net.URI;

import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

import appeng.client.guidebook.PageAnchor;

public final class LinkParser {
    private LinkParser() {
    }

    /**
     * Parses a textual reference found in a link.
     */
    public static void parseLink(PageCompiler compiler, String href, Visitor visitor) {
        // Internal vs. external links
        URI uri;
        try {
            uri = URI.create(href);
        } catch (Exception ignored) {
            visitor.handleError("Invalid URL");
            return;
        }

        ResourceLocation pageId;

        // External link
        if (uri.isAbsolute()) {
            if (uri.getScheme().equals("http") || uri.getScheme().equalsIgnoreCase("https")) {
                visitor.handleExternal(uri);
                return;
            } else {
                // Fully namespaced, absolute page id
                try {
                    pageId = new ResourceLocation(href);
                } catch (ResourceLocationException ignored) {
                    visitor.handleError("Invalid resource location");
                    return;
                }
            }
        } else {
            // Determine the page id, account for relative paths
            try {
                pageId = IdUtils.resolveLink(uri.getPath(), compiler.getPageId());
            } catch (ResourceLocationException ignored) {
                visitor.handleError("Invalid link");
                return;
            }
        }

        if (!compiler.getPageCollection().pageExists(pageId)) {
            visitor.handleError("Page does not exist");
            return;
        }

        visitor.handlePage(new PageAnchor(pageId, uri.getFragment()));
    }

    public interface Visitor {
        default void handlePage(PageAnchor page) {
        }

        default void handleExternal(URI uri) {
        }

        default void handleError(String error) {
        }
    }

}
