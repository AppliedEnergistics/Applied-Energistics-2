package appeng.datagen.providers.datamaps;

import static net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps.RAID_HERO_GIFTS;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.RaidHeroGift;

import appeng.init.InitVillager;

public class RaidHeroGiftsProvider extends DataMapProvider {
    public RaidHeroGiftsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    public void gather(HolderLookup.Provider provider) {
        this.builder(RAID_HERO_GIFTS)
                .add(InitVillager.ID, new RaidHeroGift(InitVillager.LOOT_TABLE_KEY), false);
    }
}
