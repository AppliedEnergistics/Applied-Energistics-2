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


import com.google.common.base.Joiner;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;


public final class AECommand extends CommandBase {
    private final MinecraftServer srv;

    public AECommand(final MinecraftServer server) {
        this.srv = server;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getName() {
        return "ae2";
    }

    @Override
    public String getUsage(final ICommandSender icommandsender) {
        return "commands.ae2.usage";
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        if (args.length == 0) {
            throw new WrongUsageException("commands.ae2.usage");
        } else if ("help".equals(args[0])) {
            try {
                if (args.length > 1) {
                    final Commands c = Commands.valueOf(args[1]);
                    throw new WrongUsageException(c.command.getHelp(this.srv));
                }
            } catch (final WrongUsageException wrong) {
                throw wrong;
            } catch (final Throwable er) {
                throw new WrongUsageException("commands.ae2.usage");
            }
        } else if ("list".equals(args[0])) {
            throw new WrongUsageException(Joiner.on(", ").join(Commands.values()));
        } else {
            try {
                final Commands c = Commands.valueOf(args[0]);
                if (sender.canUseCommand(c.level, this.getName())) {
                    c.command.call(this.srv, args, sender);
                } else {
                    throw new WrongUsageException("commands.ae2.permissions");
                }
            } catch (final WrongUsageException wrong) {
                throw wrong;
            } catch (final Throwable er) {
                throw new WrongUsageException("commands.ae2.usage");
            }
        }
    }
}
