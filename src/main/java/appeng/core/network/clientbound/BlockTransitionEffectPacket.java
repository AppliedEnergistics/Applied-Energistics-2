
package appeng.core.network.clientbound;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.GameData;

import appeng.core.AELog;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;

/**
 * Plays the block breaking or fluid pickup sound and a transition particle effect into the supplied direction. Used
 * primarily by annihilation planes.
 */
public record BlockTransitionEffectPacket(BlockPos pos,
        BlockState blockState,
        Direction direction,
        SoundMode soundMode) implements ClientboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockTransitionEffectPacket> STREAM_CODEC = StreamCodec
            .ofMember(
                    BlockTransitionEffectPacket::write,
                    BlockTransitionEffectPacket::decode);

    public static final Type<BlockTransitionEffectPacket> TYPE = CustomAppEngPayload
            .createType("block_transition_effect");

    @Override
    public Type<BlockTransitionEffectPacket> type() {
        return TYPE;
    }

    public enum SoundMode {
        BLOCK, FLUID, NONE
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeBlockPos(pos);
        int blockStateId = GameData.getBlockStateIDMap().getId(blockState);
        if (blockStateId == -1) {
            AELog.warn("Failed to find numeric id for block state %s", blockState);
        }
        data.writeInt(blockStateId);
        data.writeEnum(direction);
        data.writeEnum(soundMode);
    }

    public static BlockTransitionEffectPacket decode(RegistryFriendlyByteBuf data) {

        var pos = data.readBlockPos();
        int blockStateId = data.readInt();
        BlockState blockState = GameData.getBlockStateIDMap().byId(blockStateId);
        if (blockState == null) {
            AELog.warn("Received invalid blockstate id %d from server", blockStateId);
            blockState = Blocks.AIR.defaultBlockState();
        }
        var direction = data.readEnum(Direction.class);
        var soundMode = data.readEnum(SoundMode.class);
        return new BlockTransitionEffectPacket(pos, blockState, direction, soundMode);
    }

}
