package appeng.hooks;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerWorld;

public interface DynamicDimensions {

    ServerWorld addWorld(RegistryKey<World> worldId, RegistryKey<DimensionType> dimensionTypeId,
                         ChunkGenerator chunkGenerator);

}
