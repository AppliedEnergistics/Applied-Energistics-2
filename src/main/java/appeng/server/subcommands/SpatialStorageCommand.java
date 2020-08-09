package appeng.server.subcommands;

import appeng.core.worlddata.WorldData;
import appeng.server.ISubCommand;
import appeng.spatial.SpatialStorageDimensionIds;
import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.SpatialStoragePlotManager;
import appeng.spatial.TransitionInfo;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static net.minecraft.command.Commands.literal;

public class SpatialStorageCommand implements ISubCommand {

    @Override
    public void addArguments(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("info")
                .then(Commands.argument("plotId", IntegerArgumentType.integer(0))
                        .executes(ctx -> {
                                    int plotId = IntegerArgumentType.getInteger(ctx, "plotId");
                                    showPlotInfo(ctx.getSource(), plotId);
                                    return 1;
                                }
                        )));
        builder.then(literal("tp")
                .then(Commands.argument("plotId", IntegerArgumentType.integer(0))
                        .executes(ctx -> {
                                    int plotId = IntegerArgumentType.getInteger(ctx, "plotId");
                                    teleportToPlot(ctx.getSource(), plotId);
                                    return 1;
                                }
                        )));
    }

    private void showPlotInfo(CommandSource source, int plotId) {

        SpatialStoragePlot plot = SpatialStoragePlotManager.INSTANCE.getPlot(plotId);
        if (plot == null) {
            source.sendErrorMessage(new StringTextComponent("Plot not found: " + plotId));
            return;
        }

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
        String teleportToPlotCommand = getTeleportCommand(SpatialStorageDimensionIds.WORLD_ID.func_240901_a_(), plot.getOrigin());
        sendKeyValuePair(source, "Origin",
                new StringTextComponent(formatBlockPos(plot.getOrigin(), ","))
                        .modifyStyle(makeCommandLink(teleportToPlotCommand, "Teleport into plot"))
        );

        // Show information about what was last transfered into the plot (with a clickable link to the source)
        TransitionInfo lastTransition = plot.getLastTransition();
        if (lastTransition != null) {
            source.sendFeedback(new StringTextComponent("Last Transition:").mergeStyle(TextFormatting.UNDERLINE, TextFormatting.BOLD), true);

            String sourceWorldId = lastTransition.getWorldId().toString();
            IFormattableTextComponent sourceLink = new StringTextComponent(sourceWorldId + " - "
                    + formatBlockPos(lastTransition.getMin(), ",")
                    + " to " + formatBlockPos(lastTransition.getMax(), ","));
            String tpCommand = "/execute in " + sourceWorldId + " run tp @s "
                    + lastTransition.getMin().getX() + " " + (lastTransition.getMin().getY() + 1) + " "
                    + lastTransition.getMin().getZ();
            sourceLink.modifyStyle(makeCommandLink(tpCommand, "Click to teleport"));

            sendKeyValuePair(source, "Source", sourceLink);
            sendKeyValuePair(source, "When", lastTransition.getTimestamp().toString());
        } else {
            source.sendFeedback(new StringTextComponent("Last Transition unknown"), true);
        }

    }

    private void teleportToPlot(CommandSource source, int plotId) {

        SpatialStoragePlot plot = SpatialStoragePlotManager.INSTANCE.getPlot(plotId);
        if (plot == null) {
            source.sendErrorMessage(new StringTextComponent("Plot not found: " + plotId));
            return;
        }

        String teleportCommand = getTeleportCommand(
                SpatialStorageDimensionIds.WORLD_ID.func_240901_a_(),
                plot.getOrigin()
        );

        Commands commandManager = ServerLifecycleHooks.getCurrentServer().getCommandManager();
        commandManager.handleCommand(source, teleportCommand);
    }

    private static void sendKeyValuePair(CommandSource source, String label, ITextComponent value) {
        source.sendFeedback(
                new StringTextComponent("")
                        .append(new StringTextComponent(label + ": ").mergeStyle(TextFormatting.BOLD))
                        .append(value)
                ,
                true
        );
    }

    private static void sendKeyValuePair(CommandSource source, String label, String value) {
        sendKeyValuePair(source, label, new StringTextComponent(value));
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

            IFormattableTextComponent message = new StringTextComponent("")
                    .append(infoLink)
                    .appendString(" Size: " + size + " ")
                    .append(tpLink);

            sender.sendFeedback(message, true);
        }

    }

    private String formatBlockPos(BlockPos size, String separator) {
        return size.getX() + separator + size.getY() + separator + size.getZ();
    }

    private static UnaryOperator<Style> makeCommandLink(String command, String tooltip) {

        return style -> style.applyFormatting(TextFormatting.UNDERLINE)
                .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(tooltip)));

    }

    private static String getTeleportCommand(ResourceLocation worldId, BlockPos pos) {
        return "/execute in " + worldId + " run tp @s " + pos.getX() + " " + (pos.getY() + 1) + " " + pos.getZ();
    }

}
