package appeng.datagen.providers.models;

import static appeng.core.AppEng.makeId;

import java.util.Locale;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.data.PackOutput;


import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ColoredItemDefinition;

public class CableModelProvider extends AE2BlockStateProvider {
    public CableModelProvider(PackOutput packOutput) {
        super(packOutput, AppEng.MOD_ID);
    }

    @Override
    protected void register(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {

        buildCableItems(AEParts.GLASS_CABLE, "item/glass_cable_base", "part/cable/glass/");
        buildCableItems(AEParts.COVERED_CABLE, "item/covered_cable_base", "part/cable/covered/");
        buildCableItems(AEParts.COVERED_DENSE_CABLE, "item/covered_dense_cable_base", "part/cable/dense_covered/");
        buildCableItems(AEParts.SMART_CABLE, "item/smart_cable_base", "part/cable/smart/");
        buildCableItems(AEParts.SMART_DENSE_CABLE, "item/smart_dense_cable_base", "part/cable/dense_smart/");

    }

    private void buildCableItems(ColoredItemDefinition cable, String baseModel, String textureBase) {
        for (AEColor color : AEColor.values()) {

            // TODO itemModels.withExistingParent(
            // TODO         cable.id(color).getPath(),
            // TODO         makeId(baseModel)).texture("base", makeId(textureBase + color.name().toLowerCase(Locale.ROOT)));
        }
    }
}
