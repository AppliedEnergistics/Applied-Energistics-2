package appeng.integration.modules.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;

import appeng.core.AppEng;

public final class CategoryIds {
    private CategoryIds() {
    }

    public static final CategoryIdentifier<AttunementDisplay> ATTUNEMENT = CategoryIdentifier
            .of(AppEng.makeId("attunement"));
    public static final CategoryIdentifier<CondenserOutputDisplay> CONDENSER = CategoryIdentifier
            .of(AppEng.makeId("condenser"));
    public static final CategoryIdentifier<InscriberRecipeDisplay> INSCRIBER = CategoryIdentifier
            .of(AppEng.makeId("ae2.inscriber"));
    public static final CategoryIdentifier<TransformRecipeWrapper> TRANSFORM = CategoryIdentifier
            .of(AppEng.makeId("item_transformation"));
    public static final CategoryIdentifier<EntropyRecipeDisplay> ENTROPY_MANIPULATOR = CategoryIdentifier
            .of(AppEng.makeId("ae2.entropy_manipulator"));
    public static final CategoryIdentifier<ChargerDisplay> CHARGER = CategoryIdentifier.of(AppEng.makeId("charger"));
}
