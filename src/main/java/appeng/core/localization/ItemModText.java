package appeng.core.localization;

import java.util.Locale;

/**
 * Texts for the integrations with item-mods like REI or JEI.
 */
public enum ItemModText implements LocalizationEnum {
    MISSING_ID("Cannot identify recipe"),
    MISSING_ITEMS("Missing items will be skipped"),
    INCOMPATIBLE_RECIPE("Incompatible recipe"),
    NO_OUTPUT("Recipe has no output"),
    RECIPE_TOO_LARGE("Recipe larger than 3x3"),
    REQUIRES_PROCESSING_MODE("Requires processing mode"),
    THROWING_IN_WATER_CATEGORY("Throwing In Water"),
    WITH_CRYSTAL_GROWTH_ACCELERATORS("With Crystal Growth Accelerators:"),
    P2P_API_ATTUNEMENT("Attune with any:"),
    P2P_TUNNEL_ATTUNEMENT("P2P Tunnel Attunement"),
    P2P_TAG_ATTUNEMENT("Attune with any shown item");

    private final String englishText;

    ItemModText(String englishText) {
        this.englishText = englishText;
    }

    @Override
    public String getEnglishText() {
        return englishText;
    }

    @Override
    public String getTranslationKey() {
        return "ae2.rei_jei_integration." + name().toLowerCase(Locale.ROOT);
    }
}
