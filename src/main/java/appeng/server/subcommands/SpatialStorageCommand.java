package appeng.server.subcommands;

import static net.minecraft.command.Commands.literal;

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
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import appeng.core.Api;
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
    public void addArguments(LiteralArgumentBuilder<CommandSource> builder) {
        // Shows info about a given plot or about the plot the player is currently in
        builder.then(literal("info").executes(ctx -> {
            showPlotInfo(ctx.getSource(), getCurrentPlot(ctx.getSource()));
            return 1;
        }).then(Commands.argument("plotId", IntegerArgumentType.integer(1)).executes(ctx -> {
            int plotId = IntegerArgumentType.getInteger(ctx, "plotId");
            showPlotInfo(ctx.getSource(), getPlot(plotId));
            return 1;
        })));

        // Teleport into the plot
        builder.then(literal("tp").then(Commands.argument("plotId", IntegerArgumentType.integer(1)).executes(ctx -> {
            int plotId = IntegerArgumentType.getInteger(ctx, "plotId");
            teleportToPlot(ctx.getSource(), plotId);
            return 1;
        })));

        // Teleport from the current plot back to the source of its content, or do the
        // same for a given plot id
        builder.then(literal("tpback").executes(ctx -> {
            teleportBack(ctx.getSource());
            return 1;
        }).then(Commands.argument("plotId", IntegerArgumentType.integer(1)).executes(ctx -> {
            int plotId = IntegerArgumentType.getInteger(ctx, "plotId");
            teleportBack(ctx.getSource(), getPlot(plotId));
            return 1;
        })));

        // Creates a storage cell for the given plot id and gives it to the player
        builder.then(
                literal("givecell").then(Commands.argument("plotId", IntegerArgumentType.integer(1)).executes(ctx -> {
                    int plotId = IntegerArgumentType.getInteger(ctx, "plotId");
                    giveCell(ctx.getSource(), plotId);
                    return 1;
                })));
    }

    /**
     * If the player is currently within a spatial storage plot, teleports them back to the last source of transition.
     */
    private void teleportBack(CommandSource source) {

        if (source.getWorld().getDimensionKey() != SpatialStorageDimensionIds.WORLD_ID) {
            throw new CommandException(new StringTextComponent("Must be within the spatial storage world."));
        }

        BlockPos playerPos = new BlockPos(source.getPos());
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

        throw new CommandException(new StringTextComponent("Couldn't find a plot for the current position."));

    }

    /**
     * Teleports back from the given plot.
     */
    private void teleportBack(CommandSource source, SpatialStoragePlot plot) {
        TransitionInfo lastTransition = plot.getLastTransition();
        if (lastTransition == null) {
            throw new CommandException(new StringTextComponent("This plot doesn't have a last known transition."));
        }

        String command = getTeleportCommand(lastTransition.getWorldId(), lastTransition.getMin().add(0, 1, 0));
        runCommandFor(source, command);
    }

    /**
     * Shows detailed information about a spatial storage plot.
     */
    private static void showPlotInfo(CommandSource source, SpatialStoragePlot plot) {

        sendKeyValuePair(source, "Plot ID", String.valueOf(plot.getId()));
        // Show the owner of the spatial storage plot
        int playerId = plot.getOwner();
        if (playerId != -1) {
            UUID profileId = WorldData.instance().playerData().getProfileId(playerId);

            if (profileId == null) {
                sendKeyValuePair(source, "Owner", "Unknown AE2 player (" + playerId + ")");
            } else {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                ServerPlayerEntity player = server.getPlayerList().getPlayerByUUID(profileId);
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
        String teleportToPlotCommand = getTeleportCommand(SpatialStorageDimensionIds.WORLD_ID.getLocation(),
                plot.getOrigin());
        sendKeyValuePair(source, "Origin", new StringTextComponent(formatBlockPos(plot.getOrigin(), ","))
                .modifyStyle(makeCommandLink(teleportToPlotCommand, "Teleport into plot")));

        sendKeyValuePair(source, "Region file:", plot.getRegionFilename());

        // Show information about what was last transfered into the plot (with a
        // clickable link to the source)
        TransitionInfo lastTransition = plot.getLastTransition();
        if (lastTransition != null) {
            source.sendFeedback(new StringTextComponent("Last Transition:").mergeStyle(TextFormatting.UNDERLINE,
                    TextFormatting.BOLD), true);

            String sourceWorldId = lastTransition.getWorldId().toString();
            IFormattableTextComponent sourceLink = new StringTextComponent(
                    sourceWorldId + " - " + formatBlockPos(lastTransition.getMin(), ",") + " to "
                            + formatBlockPos(lastTransition.getMax(), ","));
            String tpCommand = getTeleportCommand(lastTransition.getWorldId(), lastTransition.getMin().add(0, 1, 0));
            sourceLink.modifyStyle(makeCommandLink(tpCommand, "Click to teleport"));

            sendKeyValuePair(source, "Source", sourceLink);
            sendKeyValuePair(source, "When", lastTransition.getTimestamp().toString());
        } else {
            source.sendFeedback(new StringTextComponent("Last Transition unknown"), true);
        }

    }

    private static void teleportToPlot(CommandSource source, int plotId) {
        SpatialStoragePlot plot = getPlot(plotId);

        String teleportCommand = getTeleportCommand(SpatialStorageDimensionIds.WORLD_ID.getLocation(),
                plot.getOrigin());

        runCommandFor(source, teleportCommand);
    }

    private void giveCell(CommandSource source, int plotId) throws CommandSyntaxException {
        ServerPlayerEntity player = source.asPlayer();

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
                    new StringTextComponent("Storage cell items don't implement the storage cell interface!"));
        }

        SpatialStorageCellItem spatialCellItem = (SpatialStorageCellItem) cell.getItem();
        spatialCellItem.setStoredDimension(cell, plotId, plot.getSize());

        player.addItemStackToInventory(cell);
    }

    private static int getLongestSide(BlockPos size) {
        return Math.max(size.getX(), Math.max(size.getY(), size.getZ()));
    }

    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSource> ctx, CommandSource sender) {

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

            ITextComponent infoLink = new StringTextComponent("Plot #" + plot.getId())
                    .modifyStyle(makeCommandLink("/ae2 spatial info " + plot.getId(), "Click to show details"));
            ITextComponent tpLink = new StringTextComponent("Origin: " + origin)
                    .modifyStyle(makeCommandLink("/ae2 spatial tp " + plot.getId(), "Click to teleport into plot"));

            IFormattableTextComponent message = new StringTextComponent("").append(infoLink)
                    .appendString(" Size: " + size + " ").append(tpLink);

            sender.sendFeedback(message, true);
        }

    }

    private static String formatBlockPos(BlockPos size, String separator) {
        return size.getX() + separator + size.getY() + separator + size.getZ();
    }

    private static UnaryOperator<Style> makeCommandLink(String command, String tooltip) {

        return style -> style.applyFormatting(TextFormatting.UNDERLINE)
                .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(tooltip)));

    }

    private static void runCommandFor(CommandSource source, String command) {
        Commands commandManager = ServerLifecycleHooks.getCurrentServer().getCommandManager();
        commandManager.handleCommand(source, command);
    }

    private static String getTeleportCommand(ResourceLocation worldId, BlockPos pos) {
        return "/execute in " + worldId + " run tp @s " + pos.getX() + " " + (pos.getY() + 1) + " " + pos.getZ();
    }

    private static SpatialStoragePlot getPlot(int plotId) {
        SpatialStoragePlot plot = SpatialStoragePlotManager.INSTANCE.getPlot(plotId);
        if (plot == null) {
            throw new CommandException(new StringTextComponent("Plot not found: " + plotId));
        }
        return plot;
    }

    private static void sendKeyValuePair(CommandSource source, String label, ITextComponent value) {
        source.sendFeedback(
                new StringTextComponent("")
                        .append(new StringTextComponent(label + ": ").mergeStyle(TextFormatting.BOLD)).append(value),
                true);
    }

    private static void sendKeyValuePair(CommandSource source, String label, String value) {
        sendKeyValuePair(source, label, new StringTextComponent(value));
    }

    /**
     * Gets the spatial storage plot that the command source is currently in.
     */
    private static SpatialStoragePlot getCurrentPlot(CommandSource source) {
        if (source.getWorld().getDimensionKey() != SpatialStorageDimensionIds.WORLD_ID) {
            throw new CommandException(new StringTextComponent("Must be within the spatial storage world."));
        }

        BlockPos playerPos = new BlockPos(source.getPos());
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

        throw new CommandException(new StringTextComponent("Couldn't find a plot for the current position."));
    }

}
