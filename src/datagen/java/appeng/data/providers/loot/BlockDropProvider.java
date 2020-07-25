package appeng.data.providers.loot;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

import appeng.data.providers.IAE2DataProvider;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.block.Block;
import net.minecraft.data.DataCache;
import net.minecraft.data.DataProvider;
import net.minecraft.data.server.BlockLootTableGenerator;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.loot.*;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.util.Identifier;

import appeng.core.AppEng;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public class BlockDropProvider extends BlockLootTableGenerator implements IAE2DataProvider {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path outputFolder;

    private Map<Block, Function<Block, LootTable.Builder>> overrides = ImmutableMap.<Block, Function<Block, LootTable.Builder>>builder()
            .put(BLOCKS.matrixFrame().block(), $ -> LootTable.builder())
            .put(BLOCKS.quartzOre().block(),
                    b -> dropsWithSilkTouch(BLOCKS.quartzOre().block(),
                            applyExplosionDecay(BLOCKS.quartzOre().block(),
                                    ItemEntry.builder(MATERIALS.certusQuartzCrystal().item())
                                            .apply(SetCountLootFunction.builder(UniformLootTableRange.between(1.0F, 2.0F)))
                                            .apply(ApplyBonusLootFunction.uniformBonusCount(Enchantments.FORTUNE)))))
            .put(BLOCKS.quartzOreCharged().block(),
                    b -> dropsWithSilkTouch(BLOCKS.quartzOreCharged().block(),
                            applyExplosionDecay(BLOCKS.quartzOreCharged().block(),
                                    ItemEntry.builder(MATERIALS.certusQuartzCrystalCharged().item())
                                            .apply(SetCountLootFunction.builder(UniformLootTableRange.between(1.0F, 2.0F)))
                                            .apply(ApplyBonusLootFunction.uniformBonusCount(Enchantments.FORTUNE)))))
            .build();

    public BlockDropProvider(Path outputFolder) {
        this.outputFolder = outputFolder;
    }

    @Override
    public void run(DataCache cache) throws IOException {
        for (Map.Entry<RegistryKey<Block>, Block> entry : Registry.BLOCK.getEntries()) {
            LootTable.Builder builder;
            Identifier id = entry.getKey().getValue();
            if (id.getNamespace().equals(AppEng.MOD_ID)) {
                builder = overrides.getOrDefault(entry.getValue(), this::defaultBuilder).apply(entry.getValue());

                DataProvider.writeToPath(GSON, cache, toJson(builder), getPath(outputFolder, id));
            }
        }
    }

    private LootTable.Builder defaultBuilder(Block block) {
        LeafEntry.Builder<?> entry = ItemEntry.builder(block);
        LootPool.Builder pool = LootPool.builder().rolls(ConstantLootTableRange.create(1)).with(entry)
                .conditionally(SurvivesExplosionLootCondition.builder());

        return LootTable.builder().pool(pool);
    }

    private Path getPath(Path root, Identifier id) {
        return root.resolve("data/" + id.getNamespace() + "/loot_tables/blocks/" + id.getPath() + ".json");
    }

    public JsonElement toJson(LootTable.Builder builder) {
        return LootManager.toJson(finishBuilding(builder));
    }

    @Nonnull
    public LootTable finishBuilding(LootTable.Builder builder) {
        return builder.type(LootContextTypes.BLOCK).build();
    }

    @Nonnull
    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Block Drops";
    }

}
