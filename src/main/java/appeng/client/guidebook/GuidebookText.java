package appeng.client.guidebook;

import appeng.core.localization.LocalizationEnum;

public enum GuidebookText implements LocalizationEnum {
    GuidebookHistoryGoBack("Go back one page"),
    GuidebookHistoryGoForward("Go forward one page");

    private final String englishText;

    GuidebookText(String englishText) {
        this.englishText = englishText;
    }

    @Override
    public String getTranslationKey() {
        return "ae2.guidebook." + name();
    }

    @Override
    public String getEnglishText() {
        return englishText;
    }
}
