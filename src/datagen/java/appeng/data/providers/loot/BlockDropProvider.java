package appeng.data.providers.loot;

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
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.loot.functions.ApplyBonus;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import appeng.core.AppEng;
import appeng.data.providers.IAE2DataProvider;

public class BlockDropProvider extends BlockLootTables implements IAE2DataProvider {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path outputFolder;

    private Map<Block, Function<Block, LootTable.Builder>> overrides = ImmutableMap.<Block, Function<Block, LootTable.Builder>>builder()
            .put(BLOCKS.matrixFrame().block(), $ -> LootTable.builder())
            .put(BLOCKS.quartzOre().block(),
                    b -> droppingWithSilkTouch(BLOCKS.quartzOre().block(), withExplosionDecay(BLOCKS.quartzOre().block(),
                            ItemLootEntry.builder(MATERIALS.certusQuartzCrystal().item())
                                    .acceptFunction(SetCount.builder(RandomValueRange.of(1.0F, 2.0F)))
                                    .acceptFunction(ApplyBonus.uniformBonusCount(Enchantments.FORTUNE)))))
            .put(BLOCKS.quartzOreCharged().block(), b -> droppingWithSilkTouch(BLOCKS.quartzOreCharged().block(),
                    withExplosionDecay(BLOCKS.quartzOreCharged().block(),
                            ItemLootEntry.builder(MATERIALS.certusQuartzCrystalCharged().item())
                                    .acceptFunction(SetCount.builder(RandomValueRange.of(1.0F, 2.0F)))
                                    .acceptFunction(ApplyBonus.uniformBonusCount(Enchantments.FORTUNE)))))
            .build();

    public BlockDropProvider(Path outputFolder) {
        this.outputFolder = outputFolder;
    }

    @Override
    public void act(DirectoryCache cache) throws IOException {
        for (Map.Entry<RegistryKey<Block>, Block> entry : Registry.BLOCK.getEntries()) {
            LootTable.Builder builder;
            ResourceLocation id = entry.getKey().getLocation();
            if (id.getNamespace().equals(AppEng.MOD_ID)) {
                builder = overrides.getOrDefault(entry.getValue(), this::defaultBuilder).apply(entry.getValue());

                IDataProvider.save(GSON, cache, toJson(builder), getPath(outputFolder, id));
            }
        }
    }

    private LootTable.Builder defaultBuilder(Block block) {
        StandaloneLootEntry.Builder<?> entry = ItemLootEntry.builder(block);
        LootPool.Builder pool = LootPool.builder().rolls(ConstantRange.of(1)).addEntry(entry)
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
