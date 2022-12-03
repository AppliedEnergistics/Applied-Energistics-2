package appeng.client.guidebook.indices;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appeng.client.guidebook.PageAnchor;
import appeng.client.guidebook.compiler.ParsedGuidePage;

/**
 * Pages can declare to be part of multiple categories using the categories frontmatter.
 * <p/>
 * This index is installed by default on all {@linkplain appeng.client.guidebook.Guide guides}.
 */
public class CategoryIndex extends MultiValuedIndex<String, PageAnchor> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryIndex.class);

    public CategoryIndex() {
        super("Categories", CategoryIndex::getCategories);
    }

    private static List<Pair<String, PageAnchor>> getCategories(ParsedGuidePage page) {
        var categoriesNode = page.getFrontmatter().additionalProperties().get("categories");
        if (categoriesNode == null) {
            return List.of();
        }

        if (!(categoriesNode instanceof List<?>categoryList)) {
            LOGGER.warn("Page {} contains malformed categories frontmatter", page.getId());
            return List.of();
        }

        // The anchor to the current page
        var anchor = new PageAnchor(page.getId(), null);

        var categories = new ArrayList<Pair<String, PageAnchor>>();

        for (var listEntry : categoryList) {
            if (listEntry instanceof String categoryString) {
                categories.add(Pair.of(categoryString, anchor));
            } else {
                LOGGER.warn("Page {} contains a malformed categories frontmatter entry: {}", page.getId(), listEntry);
            }
        }

        return categories;
    }
}
