package appeng.init;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

public class InitVillager {
    private InitVillager() {
    }

    public static final ResourceLocation ID = AppEng.makeId("fluix_researcher");

    public static PoiType POI_TYPE = new PoiType(
            Set.copyOf(AEBlocks.CHARGER.block().getStateDefinition().getPossibleStates()), 1, 1);
    public static final ResourceKey<PoiType> POI_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, ID);

    public static final VillagerProfession PROFESSION = new VillagerProfession(ID.toString(), e -> e.is(POI_KEY),
            e -> e.is(POI_KEY), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_LIBRARIAN);

    public static final ResourceKey<LootTable> LOOT_TABLE_KEY = ResourceKey.create(Registries.LOOT_TABLE,
            AppEng.makeId("gameplay/hero_of_the_village/fluix_researcher_gifts"));

    public static void initProfession(Registry<VillagerProfession> registry) {
        Registry.register(registry, ID, PROFESSION);
    }

    public static void initPointOfInterestType(Registry<PoiType> registry) {
        Registry.register(registry, ID, POI_TYPE);
    }

    public static void initTrades(VillagerTradesEvent event) {
        if (!event.getType().name().equals(PROFESSION.name())) {
            return;
        }

        var trades = event.getTrades();
        buyItems(trades, 1, AEItems.CERTUS_QUARTZ_CRYSTAL, 3, 4, 10);
        buyItems(trades, 1, AEItems.METEORITE_COMPASS, 2, 1, 5);

        sellItems(trades, 2, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, 3, 10, 15);
        sellItems(trades, 2, AEItems.SILICON, 5, 8, 13);
        buyItems(trades, 2, AEBlocks.SKY_STONE_BLOCK, 5, 8, 20);

        sellItems(trades, 3, AEBlocks.QUARTZ_GLASS, 2, 10, 10);
        buyItems(trades, 3, AEItems.FLUIX_CRYSTAL, 5, 4, 14);

        sellItems(trades, 4, AEItems.MATTER_BALL, 5, 8, 12);
        buyItems(trades, 4, AEItems.CALCULATION_PROCESSOR_PRESS, 10, 1, 20);
        buyItems(trades, 4, AEItems.ENGINEERING_PROCESSOR_PRESS, 10, 1, 20);
        buyItems(trades, 4, AEItems.LOGIC_PROCESSOR_PRESS, 10, 1, 20);
        buyItems(trades, 4, AEItems.SILICON_PRESS, 10, 1, 20);

        buyItems(trades, 5, Items.SLIME_BALL, 8, 5, 12);
    }

    private static void sellItems(Int2ObjectMap<List<VillagerTrades.ItemListing>> trades, int minLevel,
            ItemLike soldItem, int numberOfItems, int maxUses, int xp) {
        addOffers(
                trades, minLevel,
                new VillagerTrades.EmeraldForItems(soldItem, numberOfItems, maxUses, xp));
    }

    private static void buyItems(Int2ObjectMap<List<VillagerTrades.ItemListing>> trades, int minLevel,
            ItemLike boughtItem, int emeraldCost, int numberOfItems, int xp) {
        addOffers(
                trades, minLevel,
                new VillagerTrades.ItemsForEmeralds(boughtItem.asItem(), emeraldCost, numberOfItems, xp));
    }

    private static void addOffers(Int2ObjectMap<List<VillagerTrades.ItemListing>> offersByLevel, int minLevel,
            VillagerTrades.ItemListing... newOffers) {
        var entries = offersByLevel.computeIfAbsent(minLevel, key -> new ArrayList<>());
        Collections.addAll(entries, newOffers);
        offersByLevel.put(minLevel, entries);
    }
}
