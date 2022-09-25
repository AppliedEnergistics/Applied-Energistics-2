package appeng.util;

import com.google.common.base.Strings;

import appeng.integration.abstraction.JEIFacade;
import appeng.integration.abstraction.REIFacade;

public final class ExternalSearch {
    private ExternalSearch() {
    }

    public static boolean isExternalSearchAvailable() {
        return JEIFacade.instance().isEnabled() || REIFacade.instance().isEnabled();
    }

    public static String getExternalSearchText() {
        if (JEIFacade.instance().isEnabled()) {
            return Strings.nullToEmpty(JEIFacade.instance().getSearchText());
        } else if (REIFacade.instance().isEnabled()) {
            return Strings.nullToEmpty(REIFacade.instance().getSearchText());
        } else {
            return "";
        }
    }

    public static void setExternalSearchText(String text) {
        JEIFacade.instance().setSearchText(Strings.nullToEmpty(text));
        REIFacade.instance().setSearchText(Strings.nullToEmpty(text));
    }

    public static void clearExternalSearchText() {
        JEIFacade.instance().setSearchText("");
        REIFacade.instance().setSearchText("");
    }

    public static boolean isExternalSearchFocused() {
        return JEIFacade.instance().hasSearchFocus()
                || REIFacade.instance().hasSearchFocus();
    }
}
