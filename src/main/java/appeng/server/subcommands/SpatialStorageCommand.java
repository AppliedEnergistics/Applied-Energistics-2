package appeng.server.subcommands;

import static net.minecraft.server.command.CommandManager.literal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandException;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.worlddata.WorldData;
import appeng.items.storage.SpatialStorageCellItem;
import appeng.server.ISubCommand;
import appeng.spatial.SpatialStorageDimensionIds;
import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.SpatialStoragePlotManager;
import appeng.spatial.TransitionInfo;

/**
 * This admin command allows management of spatial storage plots.
 */
public class SpatialStorageCommand implements ISubCommand {

    @Override
    public void addArguments(LiteralArgumentBuilder<ServerCommandSource> builder) {
        // Shows info about a given plot or about the plot the player is currently in
        builder.then(literal("info").executes(ctx -> {
            showPlotInfo(ctx.getSource(), getCurrentPlot(ctx.getSource()));
            return 1;
        }).then(CommandManager.argument("plotId", IntegerArgumentType.integer(1)).executes(ctx -> {
            int plotId = IntegerArgumentType.getInteger(ctx, "plotId");
            showPlotInfo(ctx.getSource(), getPlot(plotId));
            return 1;
        })));

        // Teleport into the plot
        builder.then(
                literal("tp").then(CommandManager.argument("plotId", IntegerArgumentType.integer(1)).executes(ctx -> {
                    int plotId = IntegerArgumentType.getInteger(ctx, "plotId");
                    teleportToPlot(ctx.getSource(), plotId);
                    return 1;
                })));

        // Teleport from the current plot back to the source of its content, or do the
        // same for a given plot id
        builder.then(literal("tpback").executes(ctx -> {
            teleportBack(ctx.getSource());
            return 1;
        }).then(CommandManager.argument("plotId", IntegerArgumentType.integer(1)).executes(ctx -> {
            int plotId = IntegerArgumentType.getInteger(ctx, "plotId");
            teleportBack(ctx.getSource(), getPlot(plotId));
            return 1;
        })));

        // Creates a storage cell for the given plot id and gives it to the player
        builder.then(literal("givecell")
                .then(CommandManager.argument("plotId", IntegerArgumentType.integer(1)).executes(ctx -> {
                    int plotId = IntegerArgumentType.getInteger(ctx, "plotId");
                    giveCell(ctx.getSource(), plotId);
                    return 1;
                })));
    }

    /**
     * If the player is currently within a spatial storage plot, teleports them back
     * to the last source of transition.
     */
    private void teleportBack(ServerCommandSource source) {

        if (source.getWorld().getRegistryKey() != SpatialStorageDimensionIds.WORLD_ID) {
            throw new CommandException(Text.of("Must be within the spatial storage world."));
        }

        BlockPos playerPos = new BlockPos(source.getPosition());
        int x = playerPos.getX();
        int z = playerPos.getZ();

        // This is slow, but for an admin-command it's acceptable

        for (SpatialStoragePlot plot : SpatialStoragePlotManager.INSTANCE.getPlots()) {
            BlockPos origin = plot.getOrigin();
            BlockPos size = plot.getSize();
            if (x >= origin.getX() && x <= origin.getX() + size.getX() && z >= origin.getZ()
                    && z <= origin.getZ() + size.getZ()) {
                teleportBack(source, plot);
                return;
            }
        }

        throw new CommandException(new LiteralText("Couldn't find a plot for the current position."));

    }

    /**
     * Teleports back from the given plot.
     */
    private void teleportBack(ServerCommandSource source, SpatialStoragePlot plot) {
        TransitionInfo lastTransition = plot.getLastTransition();
        if (lastTransition == null) {
            throw new CommandException(new LiteralText("This plot doesn't have a last known transition."));
        }

        String command = getTeleportCommand(lastTransition.getWorldId(), lastTransition.getMin().add(0, 1, 0));
        runCommandFor(source, command);
    }

    /**
     * Shows detailed information about a spatial storage plot.
     */
    private static void showPlotInfo(ServerCommandSource source, SpatialStoragePlot plot) {

        sendKeyValuePair(source, "Plot ID", String.valueOf(plot.getId()));
        // Show the owner of the spatial storage plot
        int playerId = plot.getOwner();
        if (playerId != -1) {
            UUID profileId = WorldData.instance().playerData().getProfileId(playerId);

            if (profileId == null) {
                sendKeyValuePair(source, "Owner", "Unknown AE2 player (" + playerId + ")");
            } else {
                MinecraftServer server = AppEng.instance().getServer();
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(profileId);
                if (player == null) {
                    sendKeyValuePair(source, "Owner", "Unknown Minecraft profile (" + profileId + ")");
                } else {
                    sendKeyValuePair(source, "Owner", player.getDisplayName());
                }
            }
        } else {
            sendKeyValuePair(source, "Owner", "Unknown");
        }

        sendKeyValuePair(source, "Size", formatBlockPos(plot.getSize(), "x"));

        // Show the plot's origin and make it clickable to teleport directly to it
        String teleportToPlotCommand = getTeleportCommand(SpatialStorageDimensionIds.WORLD_ID.getValue(),
                plot.getOrigin());
        sendKeyValuePair(source, "Origin", new LiteralText(formatBlockPos(plot.getOrigin(), ","))
                .styled(makeCommandLink(teleportToPlotCommand, "Teleport into plot")));

        sendKeyValuePair(source, "Region file:", plot.getRegionFilename());

        // Show information about what was last transfered into the plot (with a
        // clickable link to the source)
        TransitionInfo lastTransition = plot.getLastTransition();
        if (lastTransition != null) {
            source.sendFeedback(new LiteralText("Last Transition:").formatted(Formatting.UNDERLINE, Formatting.BOLD),
                    true);

            String sourceWorldId = lastTransition.getWorldId().toString();
            MutableText sourceLink = new LiteralText(
                    sourceWorldId + " - " + formatBlockPos(lastTransition.getMin(), ",") + " to "
                            + formatBlockPos(lastTransition.getMax(), ","));
            String tpCommand = getTeleportCommand(lastTransition.getWorldId(), lastTransition.getMin().add(0, 1, 0));
            sourceLink.styled(makeCommandLink(tpCommand, "Click to teleport"));

            sendKeyValuePair(source, "Source", sourceLink);
            sendKeyValuePair(source, "When", lastTransition.getTimestamp().toString());
        } else {
            source.sendFeedback(Text.of("Last Transition unknown"), true);
        }

    }

    private static void teleportToPlot(ServerCommandSource source, int plotId) {
        SpatialStoragePlot plot = getPlot(plotId);

        String teleportCommand = getTeleportCommand(SpatialStorageDimensionIds.WORLD_ID.getValue(), plot.getOrigin());

        runCommandFor(source, teleportCommand);
    }

    private void giveCell(ServerCommandSource source, int plotId) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();

        SpatialStoragePlot plot = getPlot(plotId);

        ItemStack cell;
        int longestSide = getLongestSide(plot.getSize());
        if (longestSide <= 2) {
            cell = Api.instance().definitions().items().spatialCell2().stack(1);
        } else if (longestSide <= 16) {
            cell = Api.instance().definitions().items().spatialCell16().stack(1);
        } else {
            cell = Api.instance().definitions().items().spatialCell128().stack(1);
        }

        if (!(cell.getItem() instanceof SpatialStorageCellItem)) {
            throw new CommandException(
                    new LiteralText("Storage cell items don't implement the storage cell interface!"));
        }

        SpatialStorageCellItem spatialCellItem = (SpatialStorageCellItem) cell.getItem();
        spatialCellItem.setStoredDimension(cell, plotId, plot.getSize());

        player.giveItemStack(cell);
    }

    private static int getLongestSide(BlockPos size) {
        return Math.max(size.getX(), Math.max(size.getY(), size.getZ()));
    }

    @Override
    public void call(MinecraftServer srv, CommandContext<ServerCommandSource> ctx, ServerCommandSource sender) {

        List<SpatialStoragePlot> plots = new ArrayList<>(SpatialStoragePlotManager.INSTANCE.getPlots());

        // Prints the least recently used plots
        plots.sort(Comparator.comparing((SpatialStoragePlot plot) -> {
            TransitionInfo lastTransition = plot.getLastTransition();
            if (lastTransition != null) {
                return lastTransition.getTimestamp();
            } else {
                return Instant.MIN;
            }
        }).reversed());

        for (int i = 0; i < Math.min(5, plots.size()); i++) {
            SpatialStoragePlot plot = plots.get(i);
            String size = formatBlockPos(plot.getSize(), "x");
            BlockPos originPos = plot.getOrigin();
            String origin = formatBlockPos(originPos, ",");

            Text infoLink = new LiteralText("Plot #" + plot.getId())
                    .styled(makeCommandLink("/ae2 spatial info " + plot.getId(), "Click to show details"));
            Text tpLink = new LiteralText("Origin: " + origin)
                    .styled(makeCommandLink("/ae2 spatial tp " + plot.getId(), "Click to teleport into plot"));

            Text message = new LiteralText("").append(infoLink).append(" Size: " + size + " ").append(tpLink);

            sender.sendFeedback(message, true);
        }

    }

    private static String formatBlockPos(BlockPos size, String separator) {
        return size.getX() + separator + size.getY() + separator + size.getZ();
    }

    private static UnaryOperator<Style> makeCommandLink(String command, String tooltip) {

        return style -> style.withFormatting(Formatting.UNDERLINE)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(tooltip)));

    }

    private static void runCommandFor(ServerCommandSource source, String command) {
        CommandManager commandManager = AppEng.instance().getServer().getCommandManager();
        commandManager.execute(source, command);
    }

    private static String getTeleportCommand(Identifier worldId, BlockPos pos) {
        return "/execute in " + worldId + " run tp @s " + pos.getX() + " " + (pos.getY() + 1) + " " + pos.getZ();
    }

    private static SpatialStoragePlot getPlot(int plotId) {
        SpatialStoragePlot plot = SpatialStoragePlotManager.INSTANCE.getPlot(plotId);
        if (plot == null) {
            throw new CommandException(Text.of("Plot not found: " + plotId));
        }
        return plot;
    }

    private static void sendKeyValuePair(ServerCommandSource source, String label, Text value) {
        source.sendFeedback(
                new LiteralText("").append(new LiteralText(label + ": ").formatted(Formatting.BOLD)).append(value),
                true);
    }

    private static void sendKeyValuePair(ServerCommandSource source, String label, String value) {
        sendKeyValuePair(source, label, Text.of(value));
    }

    /**
     * Gets the spatial storage plot that the command source is currently in.
     */
    private static SpatialStoragePlot getCurrentPlot(ServerCommandSource source) {
        if (source.getWorld().getRegistryKey() != SpatialStorageDimensionIds.WORLD_ID) {
            throw new CommandException(Text.of("Must be within the spatial storage world."));
        }

        BlockPos playerPos = new BlockPos(source.getPosition());
        int x = playerPos.getX();
        int z = playerPos.getZ();

        // This is slow, but for an admin-command it's acceptable
        for (SpatialStoragePlot plot : SpatialStoragePlotManager.INSTANCE.getPlots()) {
            BlockPos origin = plot.getOrigin();
            BlockPos size = plot.getSize();
            if (x >= origin.getX() && x <= origin.getX() + size.getX() && z >= origin.getZ()
                    && z <= origin.getZ() + size.getZ()) {
                return plot;
            }
        }

        throw new CommandException(new LiteralText("Couldn't find a plot for the current position."));
    }

}
