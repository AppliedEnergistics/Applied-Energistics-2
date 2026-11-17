package appeng.datagen.providers.loot;

import java.util.List;
import java.util.Set;

import net.minecraft.core.registries.SingleRegistryBootstrap;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public final class AE2LootTableProvider {
    private AE2LootTableProvider() {
    }

    public static SingleRegistryBootstrap<LootTable> create() {
        return new LootTableProvider(Set.of(), List.of(
                new SubProviderEntry(BlockDropProvider::new, LootContextParamSets.BLOCK),
                new SubProviderEntry(RaidHeroGiftLootProvider::new, LootContextParamSets.GIFT)));
    }
}
