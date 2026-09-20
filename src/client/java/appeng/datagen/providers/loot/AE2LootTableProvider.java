package appeng.datagen.providers.loot;

import java.util.List;
import java.util.Set;

import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

// LootTableProvider is no longer a DataProvider. Loot tables became a reloadable
// datapack registry, so this class is now a SingleRegistryBootstrap<LootTable> that is registered
// through the RegistrySetBuilder in AE2DataGenerators. Consequently the old validate() override
// (which skipped validation against all registered loot tables) is gone: validation is now done
// centrally by VanillaRegistries.validateLootData over the patched registries.
public class AE2LootTableProvider extends LootTableProvider {
    private static final List<SubProviderEntry> SUB_PROVIDERS = List.of(
            new SubProviderEntry(BlockDropProvider::new, LootContextParamSets.BLOCK),
            new SubProviderEntry(RaidHeroGiftLootProvider::new, LootContextParamSets.GIFT));

    public AE2LootTableProvider() {
        super(Set.of(), SUB_PROVIDERS);
    }
}
