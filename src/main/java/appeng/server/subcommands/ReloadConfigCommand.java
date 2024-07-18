package appeng.server.subcommands;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import appeng.core.AEConfig;
import appeng.server.ISubCommand;

/**
 * Reload AE2 configuration files.
 */
public class ReloadConfigCommand implements ISubCommand {
    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> ctx, CommandSourceStack sender) {
        AEConfig.instance().reload();

        sender.sendSuccess(() -> Component.literal("AE2 configuration reloaded"), true);

        if (sender.getPlayer() != null) {
            srv.getCommands().sendCommands(sender.getPlayer());
        }
    }
}
