package appeng.datagen.providers.loot;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class AE2LootTableProvider extends LootTableProvider {
    private static final List<SubProviderEntry> SUB_PROVIDERS = List.of(
            new SubProviderEntry(BlockDropProvider::new, LootContextParamSets.BLOCK));

    public AE2LootTableProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> provider) {
        super(packOutput, Set.of(), SUB_PROVIDERS, provider);
    }

    @Override
    protected void validate(WritableRegistry<LootTable> writableregistry, ValidationContext validationcontext,
            ProblemReporter.Collector collector) {
        // Do not validate against all registered loot tables
    }
}
