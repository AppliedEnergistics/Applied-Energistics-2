package appeng.client.guidebook;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Points to a guidebook page with an optional anchor within that page.
 *
 * @param pageId
 * @param anchor ID of an anchor in the page.
 */
public record PageAnchor(ResourceLocation pageId, @Nullable String anchor) {
}
