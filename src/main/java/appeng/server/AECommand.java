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

package appeng.server;

import static net.minecraft.commands.Commands.literal;

import java.util.Locale;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;

import appeng.core.AEConfig;
import appeng.core.AppEng;

public final class AECommand {

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        LiteralArgumentBuilder<CommandSourceStack> builder = literal("ae2");
        for (Commands command : Commands.values()) {
            if (command.test && !AEConfig.instance().isDebugToolsEnabled()) {
                continue;
            }
            add(builder, command);
        }

        dispatcher.register(builder);
    }

    private void add(LiteralArgumentBuilder<CommandSourceStack> builder, Commands subCommand) {

        LiteralArgumentBuilder<CommandSourceStack> subCommandBuilder = literal(
                subCommand.name().toLowerCase(Locale.ROOT))
                        .requires(src -> src.hasPermission(subCommand.level));
        subCommand.command.addArguments(subCommandBuilder);
        subCommandBuilder.executes(ctx -> {
            subCommand.command.call(AppEng.instance().getCurrentServer(), ctx, ctx.getSource());
            return 1;
        });
        builder.then(subCommandBuilder);

    }

}
