/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021 TeamAppliedEnergistics, All rights reserved.
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

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;

import appeng.me.service.TickManagerService;
import appeng.server.ISubCommand;

public class TickMonitoring implements ISubCommand {

    @Override
    public void addArguments(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.argument("enable", BoolArgumentType.bool()).executes(ctx -> {
            var enable = ctx.getArgument("enable", Boolean.class);
            TickManagerService.MONITORING_ENABLED = enable;
            return 1;
        }));
    }

    @Override
    public void call(final MinecraftServer srv, final CommandContext<CommandSourceStack> data,
            final CommandSourceStack sender) {
    }
}
