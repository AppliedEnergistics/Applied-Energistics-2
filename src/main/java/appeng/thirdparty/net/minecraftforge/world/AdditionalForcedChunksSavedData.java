package appeng.thirdparty.net.minecraftforge.world;

public interface AdditionalForcedChunksSavedData {
    TicketTracker<net.minecraft.core.BlockPos> getBlockForcedChunks();

    TicketTracker<java.util.UUID> getEntityForcedChunks();
}
