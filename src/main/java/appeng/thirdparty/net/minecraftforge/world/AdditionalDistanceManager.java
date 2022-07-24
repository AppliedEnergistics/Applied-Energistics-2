package appeng.thirdparty.net.minecraftforge.world;

import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

public interface AdditionalDistanceManager {
    <T> void addRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object, boolean forceTicks);

    <T> void removeRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object, boolean forceTicks);

    boolean shouldForceTicks(long chunkPos);
}
