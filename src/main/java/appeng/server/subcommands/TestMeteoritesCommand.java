/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.google.common.math.StatsAccumulator;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.structure.Structure;

import appeng.server.ISubCommand;
import appeng.worldgen.meteorite.MeteoriteStructure;
import appeng.worldgen.meteorite.MeteoriteStructurePiece;
import appeng.worldgen.meteorite.PlacedMeteoriteSettings;

/**
 * This is a testing command to validate quartz ore generation.
 */
public class TestMeteoritesCommand implements ISubCommand {

    @Override
    public void addArguments(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(literal("force").executes(ctx -> {
            test(ctx.getSource().getServer(), ctx.getSource(), true);
            return 1;
        }));
    }

    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> ctx,
            CommandSourceStack sender) {
        test(srv, sender, false);
    }

    private static void test(MinecraftServer srv, CommandSourceStack sender, boolean force) {
        int radius = 100;

        ServerPlayer player = null;
        try {
            player = sender.getPlayerOrException();
        } catch (CommandSyntaxException ignored) {
        }
        ServerLevel level;
        BlockPos centerBlock;
        if (player != null) {
            level = player.serverLevel();
            centerBlock = BlockPos.containing(player.getX(), 0, player.getZ());
        } else {
            level = srv.getLevel(Level.OVERWORLD);
            centerBlock = level.getSharedSpawnPos();
        }

        ChunkPos center = new ChunkPos(centerBlock);

        var structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var structure = structures.get(MeteoriteStructure.KEY);
        var structureSets = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
        var structureSet = structureSets.getHolderOrThrow(MeteoriteStructure.STRUCTURE_SET_KEY);

        var generatorState = level.getChunkSource().getGeneratorState();

        // Find all meteorites in the given rectangle
        List<PlacedMeteoriteSettings> found = new ArrayList<>();
        int chunksChecked = 0;
        for (int cx = center.x - radius; cx <= center.x + radius; cx++) {
            for (int cz = center.z - radius; cz <= center.z + radius; cz++) {
                chunksChecked++;
                if (!generatorState.hasStructureChunkInRange(structureSet, cx, cz, 0)) {
                    continue;
                }

                var chunk = level.getChunk(cx, cz, ChunkStatus.STRUCTURE_STARTS);
                // The actual relevant information is in the structure piece
                MeteoriteStructurePiece piece = getMeteoritePieceFromChunk(chunk, structure);
                if (piece != null) {
                    found.add(piece.getSettings());
                }
            }
        }

        // Create stats on how far apart the meteorites are
        StatsAccumulator stats = new StatsAccumulator();
        for (PlacedMeteoriteSettings settings : found) {
            double closestOther = Double.NaN;
            for (PlacedMeteoriteSettings otherSettings : found) {
                if (otherSettings != settings) {
                    double d = settings.getPos().distSqr(otherSettings.getPos());
                    if (Double.isNaN(closestOther) || d < closestOther) {
                        closestOther = d;
                    }
                }
            }

            if (!Double.isNaN(closestOther)) {
                stats.add(Math.sqrt(closestOther));
            }
        }

        found.sort(Comparator.comparingDouble(settings -> settings.getPos().distSqr(centerBlock)));

        sendLine(sender, "Chunks checked: %d", chunksChecked);
        sendLine(sender, "Meteorites found: %d", found.size());
        if (stats.count() > 0) {
            sendLine(sender, "Closest: min=%.2f max=%.2f mean=%.2f stddev=%.2f", stats.min(), stats.max(), stats.mean(),
                    stats.populationStandardDeviation());
        }

        int closestCount = Math.min(10, found.size());
        for (int i = 0; i < closestCount; i++) {
            PlacedMeteoriteSettings settings = found.get(i);
            BlockPos pos = settings.getPos();

            String state = "not final";

            if (force && settings.getFallout() == null) {
                ChunkAccess chunk = level.getChunk(pos);
                MeteoriteStructurePiece piece = getMeteoritePieceFromChunk(chunk, structure);
                if (piece == null) {
                    state = "removed";
                } else {
                    settings = piece.getSettings();
                    pos = settings.getPos();
                }
            }

            Component restOfLine;
            if (settings.getFallout() == null) {
                restOfLine = Component.literal(
                        String.format(Locale.ROOT, ", radius=%.2f [%s]", settings.getMeteoriteRadius(), state));
            } else {
                restOfLine = Component.literal(String.format(Locale.ROOT, ", radius=%.2f, crater=%s, fallout=%s",
                        settings.getMeteoriteRadius(), settings.getCraterType().name().toLowerCase(),
                        settings.getFallout().name().toLowerCase()));
            }

            MutableComponent msg = Component.literal(" #" + (i + 1) + " ");
            msg.append(getClickablePosition(level, settings, pos)).append(restOfLine);

            // Add a tooltip
            String biomeId = level.getBiome(pos).unwrapKey().map(bk -> bk.location().toString()).orElse("unknown");
            Component tooltip = Component.literal(settings + "\nBiome: ").copy()
                    .append(biomeId);
            msg.withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip)));

            sender.sendSuccess(() -> msg, true);
        }
    }

    // Add a clickable link to teleport the user to the Meteorite
    private static Component getClickablePosition(ServerLevel level, PlacedMeteoriteSettings settings,
            BlockPos pos) {
        BlockPos tpPos = pos.above((int) Math.ceil(settings.getMeteoriteRadius()));
        int surfaceY = level.getHeightmapPos(Types.WORLD_SURFACE, tpPos).getY();
        if (surfaceY > tpPos.getY()) {
            tpPos = new BlockPos(tpPos.getX(), surfaceY, tpPos.getZ());
        }

        String displayText = String.format(Locale.ROOT, "pos=%d,%d,%d", tpPos.getX(), tpPos.getY(), tpPos.getZ());
        String tpCommand = String.format(Locale.ROOT, "/tp @s %d %d %d", tpPos.getX(), tpPos.getY(), tpPos.getZ());

        return Component.literal(displayText).withStyle(ChatFormatting.UNDERLINE)
                .withStyle(style -> style.withClickEvent(new ClickEvent(Action.RUN_COMMAND, tpCommand)));
    }

    private static MeteoriteStructurePiece getMeteoritePieceFromChunk(ChunkAccess chunk,
            Structure structure) {
        var start = chunk.getStartForStructure(structure);

        if (start != null && start.getPieces().size() > 0
                && start.getPieces().get(0) instanceof MeteoriteStructurePiece) {
            return (MeteoriteStructurePiece) start.getPieces().get(0);
        }
        return null;
    }

    private static void sendLine(CommandSourceStack sender, String text, Object... args) {
        sender.sendSuccess(() -> Component.literal(String.format(Locale.ROOT, text, args)), true);
    }

}
