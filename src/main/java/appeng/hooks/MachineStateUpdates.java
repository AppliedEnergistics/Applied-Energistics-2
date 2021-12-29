package appeng.hooks;

import appeng.core.sync.packets.SyncMachineStatePacket;
import appeng.parts.AEBasePart;
import com.google.common.primitives.Ints;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2IntArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2IntMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

public final class MachineStateUpdates {
    private static final Map<ServerLevel, MachineStateUpdates> PER_WORLD = new IdentityHashMap<>();

    private final ServerLevel level;

    private final Long2ReferenceMap<PerChunkState> perChunkState = new Long2ReferenceOpenHashMap<>();

    private int ticksBuffered;

    private MachineStateUpdates(ServerLevel level) {
        this.level = level;
    }

    public static void init() {
        ServerTickEvents.START_WORLD_TICK.register(level -> {
            // We only ever create this state lazily when the world ticks
            PER_WORLD.computeIfAbsent(level, MachineStateUpdates::new).beginTick();
        });
        ServerTickEvents.END_WORLD_TICK.register(level -> {
            var updates = ofLevel(level);
            if (updates != null) {
                updates.endTick();
            }
        });
        ServerWorldEvents.UNLOAD.register((server, level) -> PER_WORLD.remove(level));
    }

    @Nullable
    private static MachineStateUpdates ofLevel(ServerLevel level) {
        return PER_WORLD.get(level);
    }

    public static void addOperations(AEBasePart part, int amount) {
        if (!(part.getLevel() instanceof ServerLevel level)) {
            return;
        }

        var updates = ofLevel(level);
        if (updates == null) {
            return;
        }

        var pos = part.getHost().getLocation().getPos();
        var side = part.getSide();

        var chunkPos = ChunkPos.asLong(pos);
        var perChunkState = updates.perChunkState.computeIfAbsent(chunkPos, key -> {
            return new PerChunkState(level, new ChunkPos(chunkPos));
        });
        perChunkState.addMachineOps(pos, side, amount);
    }

    private void beginTick() {
    }

    private void endTick() {
        if (++ticksBuffered >= 5) {
            for (var chunkState : perChunkState.values()) {
                var chunkPos = chunkState.pos;
                var watchers = level.getChunkSource().chunkMap.getPlayers(chunkPos, false);

                if (!watchers.isEmpty()) {
                    // Build update packet
                    var p = chunkState.buildUpdatePacket(ticksBuffered);

                    // Send update packet
                    for (var watcher : watchers) {
                        watcher.connection.send(p);
                    }
                }
            }
            perChunkState.clear();
            ticksBuffered = 0;
        }
    }

    private static class PerChunkState {
        private final LevelHeightAccessor level;
        private final ChunkPos pos;
        private final SectionState[] sections;

        public PerChunkState(LevelHeightAccessor level, ChunkPos pos) {
            this.level = level;
            this.pos = pos;
            this.sections = new SectionState[level.getSectionsCount()];
        }

        public Packet<?> buildUpdatePacket(int ticksBuffered) {
            var packet = new SyncMachineStatePacket(ticksBuffered, pos, sections);

            return packet.toPacket(PacketFlow.CLIENTBOUND);
        }

        public int getMachineOps(BlockPos pos, @Nullable Direction side) {
            var machineOps = getSectionPosMap(pos);
            var sectionRelPos = getSectionRelPos(pos, side);

            return machineOps.getOrDefault(sectionRelPos, 0);
        }

        public int addMachineOps(BlockPos pos, @Nullable Direction side, int ops) {
            var machineOps = getSectionPosMap(pos);
            var sectionRelPos = getSectionRelPos(pos, side);

            return machineOps.merge(sectionRelPos, ops, (a, b) -> Ints.saturatedCast((long) a + b));
        }

        private Short2IntMap getSectionPosMap(BlockPos pos) {
            var sectionIndex = level.getSectionIndex(pos.getY());
            if (sections[sectionIndex] == null) {
                sections[sectionIndex] = new SectionState(new Short2IntArrayMap());
            }
            return sections[sectionIndex].machineOps;
        }

        private short getSectionRelPos(BlockPos pos, @Nullable Direction side) {
            var sectionRelPos = SectionPos.sectionRelativePos(pos);

            // Use the upper 3 bit of section pos to store the side
            var sideIdx = side == null ? 0 : (side.ordinal() + 1);
            sectionRelPos |= sideIdx << 13;
            return sectionRelPos;
        }

    }

    public record SectionState(Short2IntMap machineOps) {
    }
}
