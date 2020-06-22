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

import static net.minecraft.command.Commands.literal;

import java.util.*;

import com.google.common.math.StatsAccumulator;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import appeng.core.AELog;
import appeng.server.ISubCommand;
import appeng.worldgen.meteorite.MeteoriteStructure;
import appeng.worldgen.meteorite.MeteoriteStructurePiece;
import appeng.worldgen.meteorite.PlacedMeteoriteSettings;

/**
 * This is a testing command to validate quartz ore generation.
 */
public class TestMeteoritesCommand implements ISubCommand {

    @Override
    public void addArguments(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("force").executes(ctx -> {
            test(ServerLifecycleHooks.getCurrentServer(), ctx.getSource(), true);
            return 1;
        }));
    }

    @Override
    public void call(final MinecraftServer srv, final CommandContext<CommandSource> ctx, final CommandSource sender) {
        test(srv, sender, false);
    }

    private static void test(MinecraftServer srv, final CommandSource sender, boolean force) {
        int radius = 100;

        ServerPlayerEntity player = null;
        try {
            player = sender.asPlayer();
        } catch (CommandSyntaxException ignored) {
        }
        ServerWorld world;
        BlockPos centerBlock;
        if (player != null) {
            world = player.getServerWorld();
            centerBlock = new BlockPos(player.getPosX(), 0, player.getPosZ());
        } else {
            world = srv.getWorld(DimensionType.OVERWORLD);
            centerBlock = world.getSpawnPoint();
        }

        ChunkPos center = new ChunkPos(centerBlock);

        ChunkGenerator<?> generator = world.getChunkProvider().getChunkGenerator();

        // Find all meteorites in the given rectangle
        List<PlacedMeteoriteSettings> found = new ArrayList<>();
        int chunksChecked = 0;
        for (int cx = center.x - radius; cx <= center.x + radius; cx++) {
            for (int cz = center.z - radius; cz <= center.z + radius; cz++) {
                chunksChecked++;
                ChunkPos cp = new ChunkPos(cx, cz);
                BlockPos p = new BlockPos(cp.getXStart(), 0, cp.getZStart());
                BlockPos nearest = MeteoriteStructure.INSTANCE.findNearest(world, generator, p, 0, false);
                if (nearest != null) {
                    IChunk chunk = world.getChunk(cx, cz, ChunkStatus.STRUCTURE_STARTS);
                    // The actual relevant information is in the structure piece
                    MeteoriteStructurePiece piece = getMeteoritePieceFromChunk(chunk);
                    if (piece != null) {
                        found.add(piece.getSettings());
                    }
                }
            }
        }

        // Create stats on how far apart the meteorites are
        StatsAccumulator stats = new StatsAccumulator();
        for (PlacedMeteoriteSettings settings : found) {
            double closestOther = Double.NaN;
            for (PlacedMeteoriteSettings otherSettings : found) {
                if (otherSettings != settings) {
                    double d = settings.getPos().distanceSq(otherSettings.getPos());
                    if (Double.isNaN(closestOther) || d < closestOther) {
                        closestOther = d;
                    }
                }
            }

            if (!Double.isNaN(closestOther)) {
                stats.add(Math.sqrt(closestOther));
            }
        }

        found.sort(Comparator.comparingDouble(settings -> settings.getPos().distanceSq(centerBlock)));

        sendLine(sender, "Chunks checked: %d", chunksChecked);
        sendLine(sender, "Meteorites found: %d", found.size());
        sendLine(sender, "Closest: min=%.2f max=%.2f mean=%.2f stddev=%.2f", stats.min(), stats.max(), stats.mean(),
                stats.populationStandardDeviation());

        int closestCount = Math.min(5, found.size());
        found.forEach(entry -> {
            AELog.info(entry.toString());
        });
        for (int i = 0; i < closestCount; i++) {
            PlacedMeteoriteSettings settings = found.get(i);
            BlockPos pos = settings.getPos();

            String state = "not final";

            if (force && settings.getFallout() == null) {
                IChunk chunk = world.getChunk(pos);
                MeteoriteStructurePiece piece = getMeteoritePieceFromChunk(chunk);
                if (piece == null) {
                    state = "removed";
                } else {
                    settings = piece.getSettings();
                    pos = settings.getPos();
                }
            }

            if (settings.getFallout() == null) {
                sendLine(sender, " #%d: pos=%d,%d,%d, radius=%.2f [%s]", i + 1, pos.getX(), pos.getY(), pos.getZ(),
                        settings.getMeteoriteRadius(), state);
            } else {
                sendLine(sender, " #%d: pos=%d,%d,%d, radius=%.2f, crater=%s, lava=%s, fallout=%s", i + 1, pos.getX(),
                        pos.getY(), pos.getZ(), settings.getMeteoriteRadius(),
                        settings.getCraterType().name().toLowerCase(), settings.getFallout().name().toLowerCase());
            }
        }
    }

    private static MeteoriteStructurePiece getMeteoritePieceFromChunk(IChunk chunk) {
        StructureStart start = chunk.getStructureStart(MeteoriteStructure.INSTANCE.getStructureName());

        if (start != null && start.getComponents().size() > 0
                && start.getComponents().get(0) instanceof MeteoriteStructurePiece) {
            return (MeteoriteStructurePiece) start.getComponents().get(0);
        }
        return null;
    }

    private static void sendLine(CommandSource sender, String text, Object... args) {
        sender.sendFeedback(new StringTextComponent(String.format(Locale.ROOT, text, args)), true);
    }

}
