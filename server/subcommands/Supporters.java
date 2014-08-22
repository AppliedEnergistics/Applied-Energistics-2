package appeng.server.subcommands;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import appeng.server.ISubCommand;

import com.google.common.base.Joiner;

public class Supporters implements ISubCommand
{

	@Override
	public void call(MinecraftServer srv, String[] data, ICommandSender sender)
	{
		String[] who = { "Stig Halvorsen", "Josh Ricker", "Jenny \"Othlon\" Sutherland", "Hristo Bogdanov", "BevoLJ" };
		sender.addChatMessage( new ChatComponentText( "Special thanks to " + Joiner.on( ", " ).join( who ) ) );
	}

	@Override
	public String getHelp(MinecraftServer srv)
	{
		return "commands.ae2.Supporters";
	}

}
