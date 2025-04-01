package appeng.core.network.clientbound;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import appeng.core.AppEng;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;

/**
 * Contains data produced by {@link appeng.server.subcommands.GridsCommand}
 */
public record ExportedGridContent(int serialNumber,
        ContentType contentType,
        byte[] compressedData) implements ClientboundPacket {

    public static final Type<ExportedGridContent> TYPE = CustomAppEngPayload.createType("exported_grid_content");

    @Override
    public Type<ExportedGridContent> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ExportedGridContent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ExportedGridContent::serialNumber,
            ContentType.STREAM_CODEC, ExportedGridContent::contentType,
            ByteBufCodecs.BYTE_ARRAY, ExportedGridContent::compressedData,
            ExportedGridContent::new);

    public enum ContentType {
        FIRST_CHUNK,
        CHUNK,
        LAST_CHUNK;

        public static final StreamCodec<FriendlyByteBuf, ContentType> STREAM_CODEC = NeoForgeStreamCodecs
                .enumCodec(ContentType.class);
    }
}
