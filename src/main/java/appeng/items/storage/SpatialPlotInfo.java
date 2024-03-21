package appeng.items.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import appeng.spatial.SpatialStoragePlot;

/**
 * @param id   The {@linkplain SpatialStoragePlot#getId() plot id}.
 * @param size This is only stored in the itemstack to display in the tooltip on the client-side.
 */
public record SpatialPlotInfo(int id, BlockPos size) {
    public static final Codec<SpatialPlotInfo> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("id").forGetter(SpatialPlotInfo::id),
            BlockPos.CODEC.fieldOf("size").forGetter(SpatialPlotInfo::size)).apply(builder, SpatialPlotInfo::new));

    public static final StreamCodec<FriendlyByteBuf, SpatialPlotInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SpatialPlotInfo::id,
            BlockPos.STREAM_CODEC,
            SpatialPlotInfo::size,
            SpatialPlotInfo::new);
}
