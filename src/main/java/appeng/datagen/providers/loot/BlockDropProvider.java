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
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.loot.functions.ApplyBonus;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.core.AppEng;
import appeng.datagen.providers.IAE2DataProvider;

public class BlockDropProvider extends BlockLootTables implements IAE2DataProvider {
    private Map<Block, Function<Block, LootTable.Builder>> overrides = ImmutableMap.<Block, Function<Block, LootTable.Builder>>builder()
            .put(BLOCKS.matrixFrame().block(), $ -> LootTable.lootTable())
            .put(BLOCKS.quartzOre().block(),
                    b -> createSilkTouchDispatchTable(BLOCKS.quartzOre().block(),
                            applyExplosionDecay(BLOCKS.quartzOre().block(),
                                    ItemLootEntry.lootTableItem(MATERIALS.certusQuartzCrystal().item())
                                            .apply(SetCount.setCount(RandomValueRange.between(1.0F, 2.0F)))
                                            .apply(ApplyBonus.addUniformBonusCount(Enchantments.BLOCK_FORTUNE)))))
            .put(BLOCKS.quartzOreCharged().block(),
                    b -> createSilkTouchDispatchTable(BLOCKS.quartzOreCharged().block(),
                            applyExplosionDecay(BLOCKS.quartzOreCharged().block(),
                                    ItemLootEntry.lootTableItem(MATERIALS.certusQuartzCrystalCharged().item())
                                            .apply(SetCount.setCount(RandomValueRange.between(1.0F, 2.0F)))
                                            .apply(ApplyBonus.addUniformBonusCount(Enchantments.BLOCK_FORTUNE)))))
            .build();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path outputFolder;

    public BlockDropProvider(GatherDataEvent dataEvent) {
        outputFolder = dataEvent.getGenerator().getOutputFolder();
    }

    @Override
    public void run(@Nonnull DirectoryCache cache) throws IOException {
        for (Map.Entry<RegistryKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
            LootTable.Builder builder;
            if (entry.getKey().location().getNamespace().equals(AppEng.MOD_ID)) {
                builder = overrides.getOrDefault(entry.getValue(), this::defaultBuilder).apply(entry.getValue());

                IDataProvider.save(GSON, cache, toJson(builder), getPath(outputFolder, entry.getKey().location()));
            }
        }
    }

    private LootTable.Builder defaultBuilder(Block block) {
        LootEntry.Builder<?> entry = ItemLootEntry.lootTableItem(block);
        LootPool.Builder pool = LootPool.lootPool().name("main").setRolls(ConstantRange.exactly(1)).add(entry)
                .when(SurvivesExplosion.survivesExplosion());

        return LootTable.lootTable().withPool(pool);
    }

    private Path getPath(Path root, ResourceLocation id) {
        return root.resolve("data/" + id.getNamespace() + "/loot_tables/blocks/" + id.getPath() + ".json");
    }

    public JsonElement toJson(LootTable.Builder builder) {
        return LootTableManager.serialize(finishBuilding(builder));
    }

    @Nonnull
    public LootTable finishBuilding(LootTable.Builder builder) {
        return builder.setParamSet(LootParameterSets.BLOCK).build();
    }

    @Nonnull
    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Block Drops";
    }

}
