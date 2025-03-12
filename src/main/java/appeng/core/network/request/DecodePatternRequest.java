package appeng.core.network.request;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import appeng.core.AppEng;
import appeng.core.network.ServerboundPacket;

public record DecodePatternRequest(UUID requestId, long deadlineInMs,
        ItemStack patternItem) implements ServerboundPacket {

    public static Type<DecodePatternRequest> TYPE = new Type<>(AppEng.makeId("decode_pattern_request"));

    public static StreamCodec<RegistryFriendlyByteBuf, DecodePatternRequest> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, DecodePatternRequest::requestId,
            ByteBufCodecs.LONG, DecodePatternRequest::deadlineInMs,
            ItemStack.STREAM_CODEC, DecodePatternRequest::patternItem,
            DecodePatternRequest::new);

    @Override
    public Type<DecodePatternRequest> type() {
        return TYPE;
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        RequestManager.getInstance().handleDecodePatternRequest(player, this);
    }
}
