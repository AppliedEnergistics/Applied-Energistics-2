package appeng.init;

import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

public class InitVillager {
    private InitVillager() {
    }

    public static final ResourceLocation ID = AppEng.makeId("fluix_researcher");

    public static PoiType POI_TYPE = new PoiType(ImmutableSet.of(AEBlocks.CHARGER.block().defaultBlockState()), 1, 1);
    public static final ResourceKey<PoiType> POI_KEY = ResourceKey.create(Registry.POINT_OF_INTEREST_TYPE_REGISTRY, ID);

    public static final VillagerProfession PROFESSION = new VillagerProfession(ID.toString(), e -> e.is(POI_KEY),
            e -> e.is(POI_KEY), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_LIBRARIAN);

    public static void init() {
        ForgeRegistries.POI_TYPES.register(ID, POI_TYPE);
        ForgeRegistries.VILLAGER_PROFESSIONS.register(ID, PROFESSION);

        sellItems(1, AEItems.CERTUS_QUARTZ_CRYSTAL, 5, 10, 12);
        buyItems(1, AEItems.CERTUS_QUARTZ_CRYSTAL, 3, 4, 10);
        buyItems(1, AEItems.METEORITE_COMPASS, 2, 1, 5);

        sellItems(2, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, 3, 10, 15);
        sellItems(2, AEItems.SILICON, 5, 8, 13);
        buyItems(2, AEBlocks.SKY_STONE_BLOCK, 5, 8, 20);

        sellItems(3, AEItems.FLUIX_CRYSTAL, 3, 6, 20);
        sellItems(3, AEBlocks.QUARTZ_GLASS, 2, 10, 10);
        buyItems(3, AEItems.FLUIX_CRYSTAL, 5, 4, 14);

        sellItems(4, AEItems.MATTER_BALL, 5, 8, 12);
        buyItems(4, AEItems.CALCULATION_PROCESSOR_PRESS, 10, 1, 20);
        buyItems(4, AEItems.ENGINEERING_PROCESSOR_PRESS, 10, 1, 20);
        buyItems(4, AEItems.LOGIC_PROCESSOR_PRESS, 10, 1, 20);
        buyItems(4, AEItems.SILICON_PRESS, 10, 1, 20);

        buyItems(5, Items.SLIME_BALL, 8, 5, 12);
    }

    private static void sellItems(int minLevel, ItemLike soldItem, int numberOfItems, int maxUses, int xp) {
        addOffers(
                minLevel,
                new VillagerTrades.EmeraldForItems(soldItem, numberOfItems, maxUses, xp));
    }

    private static void buyItems(int minLevel, ItemLike boughtItem, int emeraldCost, int numberOfItems, int xp) {
        addOffers(
                minLevel,
                new VillagerTrades.ItemsForEmeralds(boughtItem.asItem(), emeraldCost, numberOfItems, xp));
    }

    private static void addOffers(int minLevel, VillagerTrades.ItemListing... newOffers) {
        var offersByLevel = VillagerTrades.TRADES.computeIfAbsent(PROFESSION, key -> new Int2ObjectOpenHashMap<>());
        var entries = offersByLevel.computeIfAbsent(minLevel, key -> new VillagerTrades.ItemListing[0]);
        entries = ArrayUtils.addAll(entries, newOffers);
        offersByLevel.put(minLevel, entries);
    }
}
