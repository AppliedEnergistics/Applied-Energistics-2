/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.server.subcommands;

import static net.minecraft.commands.Commands.literal;

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

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.ItemStack;

import appeng.core.definitions.AEItems;
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
    public void addArguments(LiteralArgumentBuilder<CommandSourceStack> builder) {
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
    private void teleportBack(CommandSourceStack source) {

        if (source.getLevel().dimension() != SpatialStorageDimensionIds.WORLD_ID) {
            throw new CommandRuntimeException(new TextComponent("Must be within the spatial storage world."));
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

        throw new CommandRuntimeException(new TextComponent("Couldn't find a plot for the current position."));

    }

    /**
     * Teleports back from the given plot.
     */
    private void teleportBack(CommandSourceStack source, SpatialStoragePlot plot) {
        TransitionInfo lastTransition = plot.getLastTransition();
        if (lastTransition == null) {
            throw new CommandRuntimeException(new TextComponent("This plot doesn't have a last known transition."));
        }

        String command = getTeleportCommand(lastTransition.getWorldId(), lastTransition.getMin().offset(0, 1, 0));
        runCommandFor(source, command);
    }

    /**
     * Shows detailed information about a spatial storage plot.
     */
    private static void showPlotInfo(CommandSourceStack source, SpatialStoragePlot plot) {

        sendKeyValuePair(source, "Plot ID", String.valueOf(plot.getId()));
        // Show the owner of the spatial storage plot
        int playerId = plot.getOwner();
        if (playerId != -1) {
            UUID profileId = WorldData.instance().playerData().getProfileId(playerId);

            if (profileId == null) {
                sendKeyValuePair(source, "Owner", "Unknown AE2 player (" + playerId + ")");
            } else {
                ServerPlayer player = source.getServer().getPlayerList().getPlayer(profileId);
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
        String teleportToPlotCommand = getTeleportCommand(SpatialStorageDimensionIds.WORLD_ID.location(),
                plot.getOrigin());
        sendKeyValuePair(source, "Origin", new TextComponent(formatBlockPos(plot.getOrigin(), ","))
                .withStyle(makeCommandLink(teleportToPlotCommand, "Teleport into plot")));

        sendKeyValuePair(source, "Region file:", plot.getRegionFilename());

        // Show information about what was last transfered into the plot (with a
        // clickable link to the source)
        TransitionInfo lastTransition = plot.getLastTransition();
        if (lastTransition != null) {
            source.sendSuccess(new TextComponent("Last Transition:").withStyle(ChatFormatting.UNDERLINE,
                    ChatFormatting.BOLD), true);

            String sourceWorldId = lastTransition.getWorldId().toString();
            MutableComponent sourceLink = new TextComponent(
                    sourceWorldId + " - " + formatBlockPos(lastTransition.getMin(), ",") + " to "
                            + formatBlockPos(lastTransition.getMax(), ","));
            String tpCommand = getTeleportCommand(lastTransition.getWorldId(), lastTransition.getMin().offset(0, 1, 0));
            sourceLink.withStyle(makeCommandLink(tpCommand, "Click to teleport"));

            sendKeyValuePair(source, "Source", sourceLink);
            sendKeyValuePair(source, "When", lastTransition.getTimestamp().toString());
        } else {
            source.sendSuccess(new TextComponent("Last Transition unknown"), true);
        }

    }

    private static void teleportToPlot(CommandSourceStack source, int plotId) {
        SpatialStoragePlot plot = getPlot(plotId);

        String teleportCommand = getTeleportCommand(SpatialStorageDimensionIds.WORLD_ID.location(),
                plot.getOrigin());

        runCommandFor(source, teleportCommand);
    }

    private void giveCell(CommandSourceStack source, int plotId) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        SpatialStoragePlot plot = getPlot(plotId);

        ItemStack cell;
        int longestSide = getLongestSide(plot.getSize());
        if (longestSide <= 2) {
            cell = AEItems.SPATIAL_CELL2.stack();
        } else if (longestSide <= 16) {
            cell = AEItems.SPATIAL_CELL16.stack();
        } else {
            cell = AEItems.SPATIAL_CELL128.stack();
        }

        if (!(cell.getItem() instanceof SpatialStorageCellItem spatialCellItem)) {
            throw new CommandRuntimeException(
                    new TextComponent("Storage cell items don't implement the storage cell interface!"));
        }

        spatialCellItem.setStoredDimension(cell, plotId, plot.getSize());

        player.addItem(cell);
    }

    private static int getLongestSide(BlockPos size) {
        return Math.max(size.getX(), Math.max(size.getY(), size.getZ()));
    }

    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> ctx, CommandSourceStack sender) {

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

            Component infoLink = new TextComponent("Plot #" + plot.getId())
                    .withStyle(makeCommandLink("/ae2 spatial info " + plot.getId(), "Click to show details"));
            Component tpLink = new TextComponent("Origin: " + origin)
                    .withStyle(makeCommandLink("/ae2 spatial tp " + plot.getId(), "Click to teleport into plot"));

            MutableComponent message = new TextComponent("").append(infoLink)
                    .append(" Size: " + size + " ").append(tpLink);

            sender.sendSuccess(message, true);
        }

    }

    private static String formatBlockPos(BlockPos size, String separator) {
        return size.getX() + separator + size.getY() + separator + size.getZ();
    }

    private static UnaryOperator<Style> makeCommandLink(String command, String tooltip) {

        return style -> style.applyFormat(ChatFormatting.UNDERLINE)
                .withClickEvent(new ClickEvent(Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(tooltip)));

    }

    private static void runCommandFor(CommandSourceStack source, String command) {
        Commands commandManager = source.getServer().getCommands();
        commandManager.performCommand(source, command);
    }

    private static String getTeleportCommand(ResourceLocation worldId, BlockPos pos) {
        return "/execute in " + worldId + " run tp @s " + pos.getX() + " " + (pos.getY() + 1) + " " + pos.getZ();
    }

    private static SpatialStoragePlot getPlot(int plotId) {
        SpatialStoragePlot plot = SpatialStoragePlotManager.INSTANCE.getPlot(plotId);
        if (plot == null) {
            throw new CommandRuntimeException(new TextComponent("Plot not found: " + plotId));
        }
        return plot;
    }

    private static void sendKeyValuePair(CommandSourceStack source, String label, Component value) {
        source.sendSuccess(
                new TextComponent("")
                        .append(new TextComponent(label + ": ").withStyle(ChatFormatting.BOLD))
                        .append(value),
                true);
    }

    private static void sendKeyValuePair(CommandSourceStack source, String label, String value) {
        sendKeyValuePair(source, label, new TextComponent(value));
    }

    /**
     * Gets the spatial storage plot that the command source is currently in.
     */
    private static SpatialStoragePlot getCurrentPlot(CommandSourceStack source) {
        if (source.getLevel().dimension() != SpatialStorageDimensionIds.WORLD_ID) {
            throw new CommandRuntimeException(new TextComponent("Must be within the spatial storage world."));
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

        throw new CommandRuntimeException(new TextComponent("Couldn't find a plot for the current position."));
    }

}
