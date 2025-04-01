package appeng.client.api.model.parts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import appeng.client.model.PartModels;

/**
 * Describes the rendering of an AE2 part.
 */
public record ClientPart(PartModel.Unbaked model, Properties properties) {
    /**
     * Additional render properties for the cable part model.
     *
     * @param requireCableConnection A solid part indicates that the rendering requires a cable connection, which will
     *                               also result in creating an intersection for the cable.
     */
    public record Properties(boolean requireCableConnection) {
        public static final Properties DEFAULT = new Properties(true);
        public static final MapCodec<Properties> MAP_CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder
                        .group(Codec.BOOL.optionalFieldOf("require_cable_connection", true)
                                .forGetter(Properties::requireCableConnection))
                        .apply(builder, Properties::new));
    }

    public static final Codec<ClientPart> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(
                    PartModels.CODEC.fieldOf("model").forGetter(ClientPart::model),
                    Properties.MAP_CODEC.forGetter(ClientPart::properties))
                    .apply(builder, ClientPart::new));
}
