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
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import appeng.core.network.ClientboundPacket;

/**
 * Contains data produced by {@link appeng.server.subcommands.GridsCommand}
 */
public record ExportedGridContent(int serialNumber,
        Type type,
        byte[] compressedData) implements ClientboundPacket {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    private static final Logger LOG = LoggerFactory.getLogger(ExportedGridContent.class);

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(serialNumber);
        buffer.writeEnum(type);
        buffer.writeByteArray(compressedData);
    }

    public static ExportedGridContent decode(FriendlyByteBuf buffer) {
        var serialNumber = buffer.readInt();
        var type = buffer.readEnum(Type.class);
        var data = buffer.readByteArray();
        return new ExportedGridContent(serialNumber, type, data);
    }

    @Override
    public void handleOnClient(Player player) {
        var saveDir = Minecraft.getInstance().gameDirectory.toPath();
        String filename;
        var spServer = Minecraft.getInstance().getSingleplayerServer();
        var connection = Minecraft.getInstance().getConnection();
        if (spServer != null) {
            saveDir = spServer.getServerDirectory().toPath();
            filename = "ae2_grid_";
        } else if (connection != null) {
            filename = "ae2_grid_from_server_";
        } else {
            LOG.error("Ignoring grid export without a connection to a server.");
            return;
        }

        saveDir = saveDir.toAbsolutePath().normalize();

        filename += serialNumber + "_" + TIMESTAMP_FORMATTER.format(LocalDateTime.now()) + ".zip";

        OpenOption[] openOptions = new OpenOption[0];
        if (type != Type.FIRST_CHUNK) {
            openOptions = new OpenOption[] { StandardOpenOption.APPEND };
        }

        var tempPath = saveDir.resolve(filename + ".tmp");
        var finalPath = saveDir.resolve(filename);
        try (var out = Files.newOutputStream(tempPath, openOptions)) {
            out.write(compressedData);
        } catch (IOException e) {
            player.sendSystemMessage(
                    Component.literal("Failed to write exported grid data to " + tempPath)
                            .withStyle(ChatFormatting.RED));
            LOG.error("Failed to write exported grid data to {}", tempPath, e);
            return;
        }

        if (type == Type.LAST_CHUNK) {
            try {
                Files.move(tempPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOG.error("Failed to move grid export {} into place", finalPath, e);
            }

            player.sendSystemMessage(Component.literal("Saved grid data for grid #" + serialNumber + " from server to ")
                    .append(Component.literal(finalPath.toString()).withStyle(style -> {
                        return style.withUnderlined(true)
                                .withClickEvent(new ClickEvent(
                                        ClickEvent.Action.OPEN_FILE,
                                        finalPath.getParent().toString()));
                    })));
        }
    }

    public enum Type {
        FIRST_CHUNK,
        CHUNK,
        LAST_CHUNK
    }
}
