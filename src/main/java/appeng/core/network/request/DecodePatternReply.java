package appeng.core.network.request;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import appeng.api.crafting.PatternDetailsTooltip;
import appeng.core.AppEng;
import appeng.core.network.ClientboundPacket;
import appeng.util.AECodecs;

public record DecodePatternReply(UUID requestId,
        @Nullable PatternDetailsTooltip tooltip,
        @Nullable Component error) implements ClientboundPacket {

    public static Type<DecodePatternReply> TYPE = new Type<>(AppEng.makeId("decode_pattern_reply"));

    public static StreamCodec<RegistryFriendlyByteBuf, DecodePatternReply> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, DecodePatternReply::requestId,
            PatternDetailsTooltip.STREAM_CODEC.apply(AECodecs::nullable), DecodePatternReply::tooltip,
            ComponentSerialization.OPTIONAL_STREAM_CODEC.map(o -> o.orElse(null), Optional::ofNullable),
            DecodePatternReply::error,
            DecodePatternReply::new);

    @Override
    public Type<DecodePatternReply> type() {
        return TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        RequestManager.getInstance().handleDecodePatternReply(this);
    }
}
