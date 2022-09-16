package appeng.core.localization;

import java.util.Locale;

/**
 * Texts for the integrations with item-mods like REI or JEI.
 */
public enum ItemModText implements LocalizationEnum {
    // Recipe transfer handling
    MISSING_ID("Cannot identify recipe"),
    MISSING_ITEMS("Missing items will be skipped"),
    INCOMPATIBLE_RECIPE("Incompatible recipe"),
    NO_OUTPUT("Recipe has no output"),
    RECIPE_TOO_LARGE("Recipe larger than 3x3"),
    REQUIRES_PROCESSING_MODE("Requires processing mode"),
    NO_ITEMS("Found no compatible items"),
    WILL_CRAFT("Will craft unavailable items"),
    CTRL_CLICK_TO_CRAFT("CTRL + click to craft unavailable items"),
    HAS_ENCODED_INGREDIENTS("Highlighted elements are already craftable"),
    MOVE_ITEMS("Move items"),
    ENCODE_PATTERN("Encode pattern"),
    // Display of AE2-specific REI/JEI entries
    THROWING_IN_WATER_CATEGORY("Throwing In Water"),
    P2P_API_ATTUNEMENT("Attune with any:"),
    P2P_TUNNEL_ATTUNEMENT("P2P Tunnel Attunement"),
    P2P_TAG_ATTUNEMENT("Attune with any shown item"),
    CRANK_DESCRIPTION("Attach the crank to a charger to power it manually."),
    CHARGER_REQUIRED_POWER("%d turns or %d AE"),
    CERTUS_QUARTZ_GROWTH("Certus Quartz Growth"),

    QUARTZ_BUDS_GROW_ON_BUDDING_QUARTZ("Quartz buds grow on budding quartz."),
    BUDS_DROP_DUST_WHEN_NOT_FULLY_GROWN("A quartz bud drops dust when not fully grown."),
    FULLY_GROWN_BUDS_DROP_CRYSTALS("A fully grown quartz bud drops crystals."),
    FORTUNE_APPLIES("Fortune enchantment applies"),
    IMPERFECT_BUDDING_QUARTZ_DECAYS("Imperfect budding quartz has a chance to decay when buds grow."),
    DECAY_CHANCE("%d%% chance"),
    BUDDING_QUARTZ_DECAYS_WHEN_BROKEN("Budding quartz decays into quartz when broken."),
    SILK_TOUCH_CAUSES_LESS_DECAY("Silk touch causes less decay"),
    SPATIAL_IO_CAUSES_NONE("Spatial I/O causes none"),
    BUDDING_QUARTZ_CREATION_AND_WORLDGEN(
            "Budding quartz can be found in meteorites, or be regenerated using charged quartz in water."),
    FLAWLESS_BUDDING_QUARTZ_DESCRIPTION(
            "Flawless budding quartz never decays when growing buds. It may only be found in meteorites."),
    CRYSTAL_GROWTH_ACCELERATORS_EFFECT(
            "Powered crystal growth accelerators speed up the growth of adjacent budding quartz."),

    ENTROPY_MANIPULATOR_HEAT("Heat (%d AE)"),
    ENTROPY_MANIPULATOR_COOL("Cool (%d AE)"),
    RIGHT_CLICK("Right-Click"),
    SHIFT_RIGHT_CLICK("Shift+Right-Click"),
    CONSUMED("Consumed"),
    FLOWING_FLUID_NAME("%s (flowing)"),
    ;

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
