package appeng.init.worldgen;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.spatial.SpatialStorageBiome;
import appeng.spatial.SpatialStorageDimensionIds;

public final class InitBiomes {

    private InitBiomes() {
    }

    public static void init(IForgeRegistry<Biome> registry) {
        Biome biome = SpatialStorageBiome.INSTANCE;
        biome.setRegistryName(SpatialStorageDimensionIds.BIOME_KEY.getLocation());
        registry.register(biome);
    }

}
