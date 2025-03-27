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

package appeng.server.services.compass;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import appeng.core.localization.PlayerMessages;
import appeng.server.ISubCommand;

public class TestCompassCommand implements ISubCommand {
    @Override
    public void addArguments(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.literal("rebuild").executes(ctx -> {
            var level = ctx.getSource().getLevel();
            ServerPlayer player = ctx.getSource().getPlayer();
            ChunkPos origin = player != null ? player.chunkPosition() : new ChunkPos(0, 0);
            ServerCompassService.rebuild(level, origin, ctx.getSource());

            return 1;
        }));
    }

    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> ctx, CommandSourceStack sender) {
        var level = sender.getLevel();
        var chunkPos = new ChunkPos(BlockPos.containing(sender.getPosition()));
        var compassRegion = CompassRegion.get(level, chunkPos);

        for (var i = 0; i <= level.getSectionsCount(); i++) {
            var hasSkyStone = compassRegion.hasCompassTarget(chunkPos.x, chunkPos.z, i);
            var yMin = i * SectionPos.SECTION_SIZE;
            var yMax = (i + 1) * SectionPos.SECTION_SIZE - 1;
            var iFinal = i;
            sender.sendSuccess(() -> PlayerMessages.CompassTestSection.text(yMin, yMax, iFinal, hasSkyStone), false);
        }
    }
}
