package appeng.datagen.providers.loot;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.block.Block;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.ConstantRange;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.SurvivesExplosion;
import net.minecraft.world.storage.loot.functions.ApplyBonus;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.core.AppEng;
import appeng.datagen.providers.IAE2DataProvider;

public class BlockDropProvider extends BlockLootTables implements IAE2DataProvider {
    private Map<Block, Function<Block, LootTable.Builder>> overrides = ImmutableMap.<Block, Function<Block, LootTable.Builder>>builder()
            .put(BLOCKS.matrixFrame().block(), $ -> LootTable.builder())
            .put(BLOCKS.quartzOre().block(),
                    b -> droppingWithSilkTouch(BLOCKS.quartzOre().block(),
                            withExplosionDecay(BLOCKS.quartzOre().block(),
                                    ItemLootEntry.builder(MATERIALS.certusQuartzCrystal().item())
                                            .acceptFunction(SetCount.builder(RandomValueRange.of(1.0F, 2.0F)))
                                            .acceptFunction(ApplyBonus.uniformBonusCount(Enchantments.FORTUNE)))))
            .put(BLOCKS.quartzOreCharged().block(),
                    b -> droppingWithSilkTouch(BLOCKS.quartzOreCharged().block(),
                            withExplosionDecay(BLOCKS.quartzOreCharged().block(),
                                    ItemLootEntry.builder(MATERIALS.certusQuartzCrystalCharged().item())
                                            .acceptFunction(SetCount.builder(RandomValueRange.of(1.0F, 2.0F)))
                                            .acceptFunction(ApplyBonus.uniformBonusCount(Enchantments.FORTUNE)))))
            .build();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path outputFolder;

    public BlockDropProvider(GatherDataEvent dataEvent) {
        outputFolder = dataEvent.getGenerator().getOutputFolder();
    }

    @Override
    public void act(@Nonnull DirectoryCache cache) throws IOException {
        for (Map.Entry<ResourceLocation, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
            LootTable.Builder builder;
            if (entry.getKey().getNamespace().equals(AppEng.MOD_ID)) {
                builder = overrides.getOrDefault(entry.getValue(), this::defaultBuilder).apply(entry.getValue());

                IDataProvider.save(GSON, cache, toJson(builder), getPath(outputFolder, entry.getKey()));
            }
        }
    }

    private LootTable.Builder defaultBuilder(Block block) {
        LootEntry.Builder<?> entry = ItemLootEntry.builder(block);
        LootPool.Builder pool = LootPool.builder().name("main").rolls(ConstantRange.of(1)).addEntry(entry)
                .acceptCondition(SurvivesExplosion.builder());

        return LootTable.builder().addLootPool(pool);
    }

    private Path getPath(Path root, ResourceLocation id) {
        return root.resolve("data/" + id.getNamespace() + "/loot_tables/blocks/" + id.getPath() + ".json");
    }

    public JsonElement toJson(LootTable.Builder builder) {
        return LootTableManager.toJson(finishBuilding(builder));
    }

    @Nonnull
    public LootTable finishBuilding(LootTable.Builder builder) {
        return builder.setParameterSet(LootParameterSets.BLOCK).build();
    }

    @Nonnull
    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Block Drops";
    }

}
