package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.server.ChunkManager;

@Mixin(ChunkManager.class)
public interface ThreadedAnvilChunkStorageAccessor {

    @Accessor("field_219266_t")
    IChunkStatusListener getWorldGenerationProgressListener();

}
