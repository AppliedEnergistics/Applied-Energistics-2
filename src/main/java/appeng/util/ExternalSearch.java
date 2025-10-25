package appeng.util;

import appeng.integration.abstraction.ItemListMod;

@Deprecated(forRemoval = true)
public final class ExternalSearch {
    private ExternalSearch() {
    }

    public static boolean isExternalSearchAvailable() {
        return ItemListMod.isEnabled();
    }

    public static String getExternalSearchText() {
        return ItemListMod.getSearchText();
    }

    public static void setExternalSearchText(String text) {
        ItemListMod.setSearchText(text);
    }

    public static void clearExternalSearchText() {
        setExternalSearchText("");
    }

    public static boolean isExternalSearchFocused() {
        return ItemListMod.hasSearchFocus();
    }
}
