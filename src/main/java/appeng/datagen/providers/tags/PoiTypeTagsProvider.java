package appeng.datagen.providers.tags;

import net.minecraft.data.DataGenerator;
import net.minecraft.tags.PoiTypeTags;

import appeng.datagen.providers.IAE2DataProvider;
import appeng.init.InitVillager;

public class PoiTypeTagsProvider extends net.minecraft.data.tags.PoiTypeTagsProvider implements IAE2DataProvider {
    public PoiTypeTagsProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void addTags() {
        tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(InitVillager.POI_TYPE);
    }
}
