package appeng.init;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import appeng.api.ids.AETags;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

public class InitVillager {
    private InitVillager() {
    }

    public static final Identifier ID = AppEng.makeId("fluix_researcher");

    public static final String NAME = "entity.minecraft.villager.ae2.fluix_researcher";

    public static final ResourceKey<TradeSet> TRADES_LEVEL_1 = ResourceKey.create(Registries.TRADE_SET,
            AppEng.makeId("fluix_researcher/level_1"));
    public static final ResourceKey<TradeSet> TRADES_LEVEL_2 = ResourceKey.create(Registries.TRADE_SET,
            AppEng.makeId("fluix_researcher/level_2"));
    public static final ResourceKey<TradeSet> TRADES_LEVEL_3 = ResourceKey.create(Registries.TRADE_SET,
            AppEng.makeId("fluix_researcher/level_3"));
    public static final ResourceKey<TradeSet> TRADES_LEVEL_4 = ResourceKey.create(Registries.TRADE_SET,
            AppEng.makeId("fluix_researcher/level_4"));
    public static final ResourceKey<TradeSet> TRADES_LEVEL_5 = ResourceKey.create(Registries.TRADE_SET,
            AppEng.makeId("fluix_researcher/level_5"));

    public static final ResourceKey<VillagerTrade> L1_BUY_CERTUS_QUARTZ_CRYSTAL = tradeKey(
            "fluix_researcher/1/buy_certus_quartz_crystal");
    public static final ResourceKey<VillagerTrade> L1_BUY_METEORITE_COMPASS = tradeKey(
            "fluix_researcher/1/buy_meteorite_compass");
    public static final ResourceKey<VillagerTrade> L2_SELL_CERTUS_QUARTZ_CRYSTAL_CHARGED = tradeKey(
            "fluix_researcher/2/sell_certus_quartz_crystal_charged");
    public static final ResourceKey<VillagerTrade> L2_SELL_SILICON = tradeKey("fluix_researcher/2/sell_silicon");
    public static final ResourceKey<VillagerTrade> L2_BUY_SKY_STONE_BLOCK = tradeKey(
            "fluix_researcher/2/buy_sky_stone_block");
    public static final ResourceKey<VillagerTrade> L3_SELL_QUARTZ_GLASS = tradeKey(
            "fluix_researcher/3/sell_quartz_glass");
    public static final ResourceKey<VillagerTrade> L3_BUY_FLUIX_CRYSTAL = tradeKey(
            "fluix_researcher/3/buy_fluix_crystal");
    public static final ResourceKey<VillagerTrade> L4_SELL_MATTER_BALL = tradeKey(
            "fluix_researcher/4/sell_matter_ball");
    public static final ResourceKey<VillagerTrade> L4_BUY_CALCULATION_PROCESSOR_PRESS = tradeKey(
            "fluix_researcher/4/buy_calculation_processor_press");
    public static final ResourceKey<VillagerTrade> L4_BUY_ENGINEERING_PROCESSOR_PRESS = tradeKey(
            "fluix_researcher/4/buy_engineering_processor_press");
    public static final ResourceKey<VillagerTrade> L4_BUY_LOGIC_PROCESSOR_PRESS = tradeKey(
            "fluix_researcher/4/buy_logic_processor_press");
    public static final ResourceKey<VillagerTrade> L4_BUY_SILICON_PRESS = tradeKey(
            "fluix_researcher/4/buy_silicon_press");
    public static final ResourceKey<VillagerTrade> L5_BUY_SLIME_BALL = tradeKey("fluix_researcher/5/buy_slime_ball");

    public static PoiType POI_TYPE = new PoiType(
            Set.copyOf(AEBlocks.CHARGER.block().getStateDefinition().getPossibleStates()), 1, 1);
    public static final ResourceKey<PoiType> POI_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, ID);

    public static final VillagerProfession PROFESSION = new VillagerProfession(
            Component.translatable(NAME),
            e -> e.is(POI_KEY),
            e -> e.is(POI_KEY),
            ImmutableSet.of(),
            ImmutableSet.of(),
            SoundEvents.VILLAGER_WORK_LIBRARIAN,
            Int2ObjectMap.ofEntries(
                    Int2ObjectMap.entry(1, TRADES_LEVEL_1),
                    Int2ObjectMap.entry(2, TRADES_LEVEL_2),
                    Int2ObjectMap.entry(3, TRADES_LEVEL_3),
                    Int2ObjectMap.entry(4, TRADES_LEVEL_4),
                    Int2ObjectMap.entry(5, TRADES_LEVEL_5)));

    public static final ResourceKey<LootTable> LOOT_TABLE_KEY = ResourceKey.create(Registries.LOOT_TABLE,
            AppEng.makeId("gameplay/hero_of_the_village/fluix_researcher_gifts"));

    public static void initProfession(Registry<VillagerProfession> registry) {
        Registry.register(registry, ID, PROFESSION);
    }

    public static void initPointOfInterestType(Registry<PoiType> registry) {
        Registry.register(registry, ID, POI_TYPE);
    }

    public static Holder<TradeSet> bootstrapTradeSets(BootstrapContext<TradeSet> context) {
        register(context, TRADES_LEVEL_1, AETags.VILLAGER_TRADE_LEVEL_1);
        register(context, TRADES_LEVEL_2, AETags.VILLAGER_TRADE_LEVEL_2);
        register(context, TRADES_LEVEL_3, AETags.VILLAGER_TRADE_LEVEL_3);
        register(context, TRADES_LEVEL_4, AETags.VILLAGER_TRADE_LEVEL_4);
        return register(context, TRADES_LEVEL_5, AETags.VILLAGER_TRADE_LEVEL_5);
    }

    public static Holder<VillagerTrade> bootstrapTrades(BootstrapContext<VillagerTrade> context) {

        buyItems(context, L1_BUY_CERTUS_QUARTZ_CRYSTAL, AEItems.CERTUS_QUARTZ_CRYSTAL, 3, 4, 10);
        buyItems(context, L1_BUY_METEORITE_COMPASS, AEItems.METEORITE_COMPASS, 2, 1, 5);
        sellItems(context, L2_SELL_CERTUS_QUARTZ_CRYSTAL_CHARGED, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, 3, 10, 15);
        sellItems(context, L2_SELL_SILICON, AEItems.SILICON, 5, 8, 13);
        buyItems(context, L2_BUY_SKY_STONE_BLOCK, AEBlocks.SKY_STONE_BLOCK, 5, 8, 20);
        sellItems(context, L3_SELL_QUARTZ_GLASS, AEBlocks.QUARTZ_GLASS, 2, 10, 10);
        buyItems(context, L3_BUY_FLUIX_CRYSTAL, AEItems.FLUIX_CRYSTAL, 5, 4, 14);
        sellItems(context, L4_SELL_MATTER_BALL, AEItems.MATTER_BALL, 5, 8, 12);
        buyItems(context, L4_BUY_CALCULATION_PROCESSOR_PRESS, AEItems.CALCULATION_PROCESSOR_PRESS, 10, 1, 20);
        buyItems(context, L4_BUY_ENGINEERING_PROCESSOR_PRESS, AEItems.ENGINEERING_PROCESSOR_PRESS, 10, 1, 20);
        buyItems(context, L4_BUY_LOGIC_PROCESSOR_PRESS, AEItems.LOGIC_PROCESSOR_PRESS, 10, 1, 20);
        buyItems(context, L4_BUY_SILICON_PRESS, AEItems.SILICON_PRESS, 10, 1, 20);
        return buyItems(context, L5_BUY_SLIME_BALL, Items.SLIME_BALL, 8, 5, 12);

    }

    public static Holder.Reference<TradeSet> register(BootstrapContext<TradeSet> context,
            ResourceKey<TradeSet> resourceKey, TagKey<VillagerTrade> tradeTag) {
        return register(context, resourceKey, tradeTag, ConstantValue.exactly(2.0F));
    }

    public static Holder.Reference<TradeSet> register(BootstrapContext<TradeSet> context,
            ResourceKey<TradeSet> resourceKey, TagKey<VillagerTrade> tradeTag, NumberProvider numberProvider) {
        return context.register(resourceKey,
                new TradeSet(context.lookup(Registries.VILLAGER_TRADE).getOrThrow(tradeTag), numberProvider, false,
                        Optional.of(resourceKey.identifier().withPrefix("trade_set/"))));
    }

    private static Holder<VillagerTrade> sellItems(BootstrapContext<VillagerTrade> context,
            ResourceKey<VillagerTrade> key,
            ItemLike soldItem, int numberOfItems, int maxUses, int xp) {
        return context.register(
                key,
                new VillagerTrade(
                        new TradeCost(soldItem, numberOfItems),
                        new ItemStackTemplate(Items.EMERALD),
                        maxUses,
                        xp,
                        0.05F,
                        Optional.empty(),
                        List.of()));
    }

    private static Holder<VillagerTrade> buyItems(BootstrapContext<VillagerTrade> context,
            ResourceKey<VillagerTrade> key,
            ItemLike boughtItem, int emeraldCost, int numberOfItems, int xp) {
        return context.register(
                key,
                new VillagerTrade(
                        new TradeCost(Items.EMERALD, emeraldCost),
                        new ItemStackTemplate(boughtItem.asItem()),
                        numberOfItems,
                        xp,
                        0.05F,
                        Optional.empty(),
                        List.of()));
    }

    private static ResourceKey<VillagerTrade> tradeKey(String id) {
        return ResourceKey.create(Registries.VILLAGER_TRADE, AppEng.makeId(id));
    }
}
