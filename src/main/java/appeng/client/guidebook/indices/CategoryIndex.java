package appeng.client.guidebook.indices;

import appeng.client.guidebook.PageAnchor;
import appeng.client.guidebook.compiler.ParsedGuidePage;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Pages can declare to be part of multiple categories using the categories frontmatter.
 */
public class CategoryIndex extends MultiValuedIndex<String, PageAnchor> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryIndex.class);

    public static final CategoryIndex INSTANCE = new CategoryIndex();

    public CategoryIndex() {
        super("Categories", CategoryIndex::getCategories);
    }

    private static List<Pair<String, PageAnchor>> getCategories(ParsedGuidePage page) {
        var categoriesNode = page.getFrontmatter().additionalProperties().get("categories");
        if (categoriesNode == null) {
            return List.of();
        }

        if (!(categoriesNode instanceof List<?> categoryList)) {
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
