package appeng.integration.abstraction;

import com.google.common.base.Strings;

/**
 * Abstraction for accessing functionality of mods like JEI/REI/EMI.
 */
public class ItemListMod {

    private static ItemListModAdapter adapter = ItemListModAdapter.none();

    private ItemListMod() {
    }

    /**
     * @return True when an item-list mod like JEI/REI/EMI is active.
     */
    public static boolean isEnabled() {
        return adapter.isEnabled();
    }

    /**
     * @return The name of the item-list mod that is present.
     */
    public static String getShortName() {
        return adapter.getShortName();
    }

    public static String getSearchText() {
        return Strings.nullToEmpty(adapter.getSearchText());
    }

    public static void setSearchText(String text) {
        adapter.setSearchText(Strings.nullToEmpty(text));
    }

    public static boolean hasSearchFocus() {
        return adapter.hasSearchFocus();
    }

    public static void setAdapter(ItemListModAdapter adapter) {
        ItemListMod.adapter = adapter;
    }
}
