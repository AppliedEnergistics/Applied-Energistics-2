package appeng.datagen.providers.models;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.datagen.providers.IAE2DataProvider;

public class ItemModelProvider extends net.minecraftforge.client.model.generators.ItemModelProvider
        implements IAE2DataProvider {
    public ItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, AppEng.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        flatSingleLayer(AEItems.ITEM_CELL_CREATIVE, "item/creative_item_cell");
        flatSingleLayer(AEItems.FLUID_CELL_CREATIVE, "item/creative_fluid_cell");
    }

    private void flatSingleLayer(ItemDefinition<?> item, String texture) {
        String id = item.id().getPath();
        singleTexture(
                id,
                mcLoc("item/generated"),
                "layer0",
                AppEng.makeId(texture));
    }
}
