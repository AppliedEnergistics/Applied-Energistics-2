package appeng.mixins.chunkloading;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.TickingTracker;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.ChunkPos;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import appeng.thirdparty.net.minecraftforge.world.AdditionalDistanceManager;

@Mixin(DistanceManager.class)
public abstract class DistanceManagerMixin implements AdditionalDistanceManager {
    private final Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> forcedTickets = new Long2ObjectOpenHashMap<>();

    @Final
    @Shadow
    private TickingTracker tickingTicketsTracker;

    @Shadow
    abstract void addTicket(long l, Ticket<?> ticket);

    @Shadow
    abstract void removeTicket(long l, Ticket<?> ticket);

    @Override
    public <T> void addRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object, boolean forceTicks) {
        Ticket<T> ticket = new Ticket<>(ticketType, 33 - i, object);
        long l = chunkPos.toLong();
        this.addTicket(l, ticket);
        this.tickingTicketsTracker.addTicket(l, ticket);

        if (forceTicks) {
            SortedArraySet<Ticket<?>> tickets = forcedTickets.computeIfAbsent(l, e -> SortedArraySet.create(4));
            tickets.addOrGet(ticket);
        }
    }

    @Override
    public <T> void removeRegionTicket(TicketType<T> ticketType, ChunkPos chunkPos, int i, T object,
            boolean forceTicks) {
        Ticket<T> ticket = new Ticket<>(ticketType, 33 - i, object);
        long l = chunkPos.toLong();
        this.removeTicket(l, ticket);
        this.tickingTicketsTracker.removeTicket(l, ticket);

        if (forceTicks) {
            SortedArraySet<Ticket<?>> tickets = forcedTickets.get(l);
            if (tickets != null) {
                tickets.remove(ticket);
            }
        }
    }

    public boolean shouldForceTicks(long chunkPos) {
        SortedArraySet<Ticket<?>> tickets = forcedTickets.get(chunkPos);
        return tickets != null && !tickets.isEmpty();
    }
}
