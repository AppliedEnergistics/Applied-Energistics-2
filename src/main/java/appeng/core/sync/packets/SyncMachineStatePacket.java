package appeng.core.sync.packets;

import io.netty.buffer.Unpooled;

import it.unimi.dsi.fastutil.shorts.Short2IntArrayMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.hooks.MachineStateUpdates;

public class SyncMachineStatePacket extends BasePacket {
    private ChunkPos pos;
    private int ticksBuffered;
    private MachineStateUpdates.SectionState[] sections;

    public SyncMachineStatePacket(FriendlyByteBuf data) {
        this.pos = data.readChunkPos();
        this.ticksBuffered = data.readByte();
        this.sections = new MachineStateUpdates.SectionState[data.readByte()];

        for (var i = data.readByte(); i != -1; i = data.readByte()) {
            var machineCount = data.readVarInt();
            var machines = new Short2IntArrayMap(machineCount);
            for (int j = 0; j < machineCount; j++) {
                var key = data.readShort();
                machines.put(key, data.readInt());
            }

            sections[i] = new MachineStateUpdates.SectionState(machines);
        }
    }

    public SyncMachineStatePacket(int ticksBuffered, ChunkPos pos, MachineStateUpdates.SectionState[] sections) {
        var data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        data.writeChunkPos(pos);
        data.writeByte(ticksBuffered);
        data.writeByte((byte) sections.length);

        for (int i = 0; i < sections.length; i++) {
            if (sections[i] == null) {
                continue;
            }

            data.writeByte(i);
            data.writeVarInt(sections[i].machineOps().size());
            for (var entry : sections[i].machineOps().short2IntEntrySet()) {
                data.writeShort(entry.getShortKey());
                data.writeInt(entry.getIntValue());
            }
        }

        data.writeInt(-1); // Terminator

        configureWrite(data);
    }

    @Override
    public void clientPacketData(INetworkInfo network, Player player) {

    }
}
