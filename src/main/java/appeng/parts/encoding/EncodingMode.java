package appeng.parts.encoding;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public enum EncodingMode {
    CRAFTING,
    PROCESSING,
    SMITHING_TABLE,
    STONECUTTING,
    ;

    public static final StreamCodec<FriendlyByteBuf, EncodingMode> STREAM_CODEC = NeoForgeStreamCodecs
            .enumCodec(EncodingMode.class);
}
