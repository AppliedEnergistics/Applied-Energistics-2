package appeng.server.subcommands;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

import appeng.metrics.Metrics;
import appeng.metrics.reporter.PrintStreamReporter;
import appeng.server.ISubCommand;

/**
 * A simple way of printing out AE2's metrics on the server console.
 */
public class MetricsCommand implements ISubCommand {
    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSource> ctx, CommandSource sender) {
        System.out.println("-------- AE2 Metrics:");
        Metrics.visit(new PrintStreamReporter(System.out));
        System.out.println("-------- END");

        sender.sendFeedback(new StringTextComponent("Metrics reported to server console..."), false);
    }
}
