package appeng.core.sync.packets;

import java.util.function.Consumer;

import io.netty.buffer.Unpooled;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;

/**
 * Sends updates from {@link appeng.blockentity.AEBaseBlockEntity} to the client.
 */
public class BlockEntityUpdatePacket extends BasePacket {
    private BlockPos pos;
    private BlockEntityType<?> type;
    private byte[] data;

    public BlockEntityUpdatePacket(BlockPos blockPos,
            BlockEntityType<?> blockEntityType,
            Consumer<FriendlyByteBuf> writer) {
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeInt(getPacketID());
        data.writeBlockPos(blockPos);
        data.writeVarInt(Registry.BLOCK_ENTITY_TYPE.getId(blockEntityType));
        writer.accept(data);
        configureWrite(data);
    }

    public BlockEntityUpdatePacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.type = Registry.BLOCK_ENTITY_TYPE.byId(buffer.readVarInt());
        this.data = new byte[buffer.readableBytes()];
        buffer.readBytes(this.data);
    }

    @Override
    public void clientPacketData(INetworkInfo network, Player player) {
        player.level.getBlockEntity(pos, type).ifPresent(blockEntity -> {
            if (blockEntity instanceof AEBaseBlockEntity baseBlockEntity) {
                baseBlockEntity.fromClientUpdate(data);
            }
        });
    }
}
