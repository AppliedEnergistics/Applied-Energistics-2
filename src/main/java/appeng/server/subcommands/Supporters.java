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

import com.google.common.base.Joiner;

import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

import appeng.server.ISubCommand;

public class Supporters implements ISubCommand {

    @Override
    public String getHelp(final MinecraftServer srv) {
        return "commands.ae2.Supporters";
    }

    @Override
    public void call(final MinecraftServer srv, final String[] data, final CommandSource sender) {
        final String[] who = { "Stig Halvorsen", "Josh Ricker", "Jenny \"Othlon\" Sutherland", "Hristo Bogdanov",
                "BevoLJ" };
        sender.sendFeedback(new StringTextComponent("Special thanks to " + Joiner.on(", ").join(who)), true);
    }
}
