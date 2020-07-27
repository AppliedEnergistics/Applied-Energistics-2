package appeng.mixins;

import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.server.ChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkManager.class)
public interface ThreadedAnvilChunkStorageAccessor {

    @Accessor("field_219266_t")
    IChunkStatusListener getWorldGenerationProgressListener();

}
