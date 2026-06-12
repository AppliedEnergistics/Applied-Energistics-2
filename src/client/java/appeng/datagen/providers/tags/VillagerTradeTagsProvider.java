package appeng.datagen.providers.tags;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.world.item.trading.VillagerTrade;

import appeng.api.ids.AETags;
import appeng.core.AppEng;
import appeng.init.InitVillager;

public class VillagerTradeTagsProvider extends KeyTagProvider<VillagerTrade> {
    public VillagerTradeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.VILLAGER_TRADE, lookupProvider, AppEng.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(AETags.VILLAGER_TRADE_LEVEL_1)
                .add(
                        InitVillager.L1_BUY_CERTUS_QUARTZ_CRYSTAL,
                        InitVillager.L1_BUY_METEORITE_COMPASS);
        tag(AETags.VILLAGER_TRADE_LEVEL_2)
                .add(
                        InitVillager.L2_SELL_CERTUS_QUARTZ_CRYSTAL_CHARGED,
                        InitVillager.L2_SELL_SILICON,
                        InitVillager.L2_BUY_SKY_STONE_BLOCK);
        tag(AETags.VILLAGER_TRADE_LEVEL_3)
                .add(
                        InitVillager.L3_SELL_QUARTZ_GLASS,
                        InitVillager.L3_BUY_FLUIX_CRYSTAL);
        tag(AETags.VILLAGER_TRADE_LEVEL_4).add(
                InitVillager.L4_SELL_MATTER_BALL,
                InitVillager.L4_BUY_CALCULATION_PROCESSOR_PRESS,
                InitVillager.L4_BUY_ENGINEERING_PROCESSOR_PRESS,
                InitVillager.L4_BUY_LOGIC_PROCESSOR_PRESS,
                InitVillager.L4_BUY_SILICON_PRESS);
        tag(AETags.VILLAGER_TRADE_LEVEL_5).add(InitVillager.L5_BUY_SLIME_BALL);
    }
}
