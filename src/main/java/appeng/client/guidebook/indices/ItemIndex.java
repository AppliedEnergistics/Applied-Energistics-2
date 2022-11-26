package appeng.client.guidebook.indices;

import appeng.client.guidebook.PageAnchor;
import appeng.client.guidebook.compiler.IdUtils;
import appeng.client.guidebook.compiler.ParsedGuidePage;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * An index of Minecraft items to the main guidebook page describing it.
 */
public class ItemIndex extends UniquePageIndex<ResourceLocation, PageAnchor> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemIndex.class);

    public static final ItemIndex INSTANCE = new ItemIndex();

    public ItemIndex() {
        super("Item Index", ItemIndex::getItemAnchors);
    }

    private static List<Pair<ResourceLocation, PageAnchor>> getItemAnchors(ParsedGuidePage page) {
        var itemIds = page.getFrontmatter().additionalProperties().get("item_ids");
        if (itemIds instanceof List<?> itemIdList) {
            var itemAnchors = new ArrayList<Pair<ResourceLocation, PageAnchor>>();

            for (var listEntry : itemIdList) {
                if (listEntry instanceof String itemIdStr) {
                    ResourceLocation itemId = null;
                    try {
                        itemId = IdUtils.resolve(itemIdStr);
                    } catch (ResourceLocationException e) {
                        LOGGER.warn("Page {} contains a malformed item_ids frontmatter entry: {}", page.getId(), listEntry);
                        continue;
                    }

                    if (Registry.ITEM.containsKey(itemId)) {
                        // add a link to the top of the page
                        itemAnchors.add(Pair.of(
                                itemId, new PageAnchor(page.getId(), null)
                        ));
                    } else {
                        LOGGER.warn("Page {} references an unknown item {} in its item_ids frontmatter",
                                page.getId(), itemId);
                    }
                } else {
                    LOGGER.warn("Page {} contains a malformed item_ids frontmatter entry: {}", page.getId(), listEntry);
                }
            }

            return itemAnchors;
        } else {
            LOGGER.warn("Page {} contains a malformed item_ids frontmatter", page.getId());
            return List.of();
        }
    }
}
