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

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;

import appeng.core.localization.PlayerMessages;
import appeng.server.ISubCommand;

public class TestCompassCommand implements ISubCommand {
    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> ctx, CommandSourceStack sender) {
        var level = sender.getLevel();
        var chunkPos = new ChunkPos(new BlockPos(sender.getPosition()));
        var compassRegion = CompassRegion.get(level, chunkPos);

        for (var i = 0; i <= level.getSectionsCount(); i++) {
            var hasSkyStone = compassRegion.hasSkyStone(chunkPos.x, chunkPos.z, i);
            var yMin = i * SectionPos.SECTION_SIZE;
            var yMax = (i + 1) * SectionPos.SECTION_SIZE - 1;
            sender.sendSuccess(PlayerMessages.CompassTestSection.text(yMin, yMax, i, hasSkyStone), false);
        }
    }
}
