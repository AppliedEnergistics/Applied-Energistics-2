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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import com.google.common.math.StatsAccumulator;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

import appeng.core.definitions.AEBlocks;
import appeng.server.ISubCommand;

/**
 * This is a testing command to validate quartz ore generation.
 */
public class TestOreGenCommand implements ISubCommand {

    private final BlockState quartzOre;
    private final BlockState deepslateQuartzOre;

    public TestOreGenCommand() {
        quartzOre = AEBlocks.QUARTZ_ORE.block().defaultBlockState();
        deepslateQuartzOre = AEBlocks.DEEPSLATE_QUARTZ_ORE.block().defaultBlockState();
    }

    @Override
    public void call(final MinecraftServer srv, final CommandContext<CommandSourceStack> data,
            final CommandSourceStack sender) {

        int radius = 1000;

        ServerLevel level;
        BlockPos center;
        try {
            ServerPlayer player = sender.getPlayerOrException();
            level = player.getLevel();
            center = new BlockPos(player.getX(), 0, player.getZ());
        } catch (CommandSyntaxException e) {
            level = srv.getLevel(Level.OVERWORLD);
            center = level.getSharedSpawnPos();
        }

        ChunkPos tl = new ChunkPos(center.offset(-radius, 0, -radius));
        ChunkPos br = new ChunkPos(center.offset(radius, 0, radius));

        Stats stats = new Stats();
        for (int cx = tl.x; cx <= br.x; cx++) {
            for (int cz = tl.z; cz <= br.z; cz++) {
                ChunkPos cp = new ChunkPos(cx, cz);
                checkChunk(sender, level, cp, stats);
            }
        }

        sendLine(sender, "Checked %d chunks", stats.chunks.size());

        /*
         * Report on normal quartz ore.
         */
        var oreCount = AggregatedStats.create(stats.chunks, cs -> (double) cs.quartzOreCount);
        var chunksWithOre = stats.chunks.stream().filter(c -> c.quartzOreCount > 0)
                .collect(Collectors.toList());
        var minHeight = AggregatedStats.create(chunksWithOre, cs -> (double) cs.minHeight);
        var maxHeight = AggregatedStats.create(chunksWithOre, cs -> (double) cs.maxHeight);

        sendLine(sender, "  Normal Quartz Ore: %s", oreCount);
        sendLine(sender, "  Min-Height: %s", minHeight);
        sendLine(sender, "  Max-Height: %s", maxHeight);

        /*
         * Report on deepslate quartz ore.
         */
        var deepslateOreCount = AggregatedStats.create(stats.chunks, cs -> (double) cs.deepslateQuartzOreCount);
        var chunksWithDeeepslateOre = stats.chunks.stream().filter(c -> c.deepslateQuartzOreCount > 0)
                .collect(Collectors.toList());
        var deepslateMinHeight = AggregatedStats.create(chunksWithDeeepslateOre, cs -> (double) cs.deepslateMinHeight);
        var deepslateMaxHeight = AggregatedStats.create(chunksWithDeeepslateOre, cs -> (double) cs.deepslateMaxHeight);

        sendLine(sender, "  Deepslate Quartz Ore: %s", deepslateOreCount);
        sendLine(sender, "  Min-Height: %s", deepslateMinHeight);
        sendLine(sender, "  Max-Height: %s", deepslateMaxHeight);
    }

    private void checkChunk(CommandSourceStack sender, ServerLevel level, ChunkPos cp, Stats stats) {
        ChunkAccess chunk = level.getChunk(cp.x, cp.z, ChunkStatus.FULL, false);
        if (chunk == null) {
            sendLine(sender, "Skipping chunk %s", cp);
            return;
        }

        ChunkStats chunkStats = new ChunkStats();

        MutableBlockPos blockPos = new MutableBlockPos();
        sendLine(sender, "Checking chunk %s", cp);
        for (int x = cp.getMinBlockX(); x <= cp.getMaxBlockX(); x++) {
            blockPos.setX(x);
            for (int z = cp.getMinBlockZ(); z <= cp.getMaxBlockZ(); z++) {
                blockPos.setZ(z);
                for (int y = 0; y < level.getHeight(); y++) {
                    blockPos.setY(y);
                    BlockState state = chunk.getBlockState(blockPos);
                    if (state == quartzOre) {
                        chunkStats.minHeight = Math.min(chunkStats.minHeight, y);
                        chunkStats.maxHeight = Math.max(chunkStats.maxHeight, y);
                        chunkStats.quartzOreCount++;
                    } else if (state == deepslateQuartzOre) {
                        chunkStats.deepslateMinHeight = Math.min(chunkStats.deepslateMinHeight, y);
                        chunkStats.deepslateMaxHeight = Math.max(chunkStats.deepslateMaxHeight, y);
                        chunkStats.deepslateQuartzOreCount++;
                    }
                }
            }
        }
        stats.chunks.add(chunkStats);
    }

    private static void sendLine(CommandSourceStack sender, String text, Object... args) {
        sender.sendSuccess(new TextComponent(String.format(Locale.ROOT, text, args)), true);
    }

    private static class Stats {
        public final List<ChunkStats> chunks = new ArrayList<>();
    }

    private static class ChunkStats {
        public int quartzOreCount = 0;
        public int minHeight = Integer.MAX_VALUE;
        public int maxHeight = Integer.MIN_VALUE;

        public int deepslateQuartzOreCount = 0;
        public int deepslateMinHeight = Integer.MAX_VALUE;
        public int deepslateMaxHeight = Integer.MIN_VALUE;
    }

    private static class AggregatedStats {
        public final double min;
        public final double max;
        public final double mean;
        public final double stdDev;

        public AggregatedStats(double min, double max, double mean, double stdDev) {
            this.min = min;
            this.max = max;
            this.mean = mean;
            this.stdDev = stdDev;
        }

        public static <T> AggregatedStats create(List<T> values, ToDoubleFunction<T> getter) {
            if (values.isEmpty()) {
                return new AggregatedStats(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
            }

            StatsAccumulator accumulator = new StatsAccumulator();
            for (T value : values) {
                accumulator.add(getter.applyAsDouble(value));
            }

            return new AggregatedStats(accumulator.min(), accumulator.max(), accumulator.mean(),
                    accumulator.populationStandardDeviation());
        }

        @Override
        public String toString() {
            if (Double.isNaN(min)) {
                return "Invalid";
            }
            return String.format(Locale.ROOT, "min=%.2f, max=%.2f, mean=%.2f, stdDev=%.2f", min, max, mean, stdDev);
        }
    }

}
