package appeng.datagen.providers.tags;

import net.minecraft.data.DataGenerator;
import net.minecraft.tags.PoiTypeTags;
import net.minecraftforge.common.data.ExistingFileHelper;

import appeng.core.AppEng;
import appeng.datagen.providers.IAE2DataProvider;
import appeng.init.InitVillager;

public class PoiTypeTagsProvider extends net.minecraft.data.tags.PoiTypeTagsProvider implements IAE2DataProvider {
    public PoiTypeTagsProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, AppEng.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(InitVillager.POI_TYPE);
    }
}
