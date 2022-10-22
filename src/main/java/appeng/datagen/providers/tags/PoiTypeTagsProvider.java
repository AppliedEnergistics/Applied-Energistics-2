package appeng.datagen.providers.tags;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.PoiTypeTags;

import appeng.datagen.providers.IAE2DataProvider;
import appeng.init.InitVillager;

public class PoiTypeTagsProvider extends net.minecraft.data.tags.PoiTypeTagsProvider implements IAE2DataProvider {
    public PoiTypeTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
        super(packOutput, registries);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(InitVillager.POI_KEY);
    }
}
