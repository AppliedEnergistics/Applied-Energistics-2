package appeng.hooks;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public interface DynamicDimensions {

    ServerWorld addWorld(RegistryKey<World> worldId, RegistryKey<DimensionType> dimensionTypeId,
            ChunkGenerator chunkGenerator);

}
