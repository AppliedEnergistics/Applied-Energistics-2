package appeng.client.guidebook;

import appeng.core.localization.LocalizationEnum;

public enum GuidebookText implements LocalizationEnum {
    HistoryGoBack("Go back one page"),
    HistoryGoForward("Go forward one page"),
    Close("Close"),
    HoldToShow("Hold [%s] to open guide"),
    HideAnnotations("Hide Annotations"),
    ShowAnnotations("Show Annotations"),
    ZoomIn("Zoom In"),
    ZoomOut("Zoom Out"),
    ResetView("Reset View"),
    Search("Search");

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
