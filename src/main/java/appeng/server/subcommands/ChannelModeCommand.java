package appeng.server.subcommands;

import static net.minecraft.commands.Commands.literal;

import java.util.Locale;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

import appeng.api.networking.pathing.ChannelMode;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.localization.PlayerMessages;
import appeng.hooks.ticking.TickHandler;
import appeng.me.Grid;
import appeng.server.ISubCommand;

/**
 * Command to easily change {@link AEConfig#getChannelMode()} at runtime.
 */
public class ChannelModeCommand implements ISubCommand {
    @Override
    public void addArguments(LiteralArgumentBuilder<CommandSourceStack> builder) {
        for (var mode : ChannelMode.values()) {
            builder.then(literal(mode.name().toLowerCase(Locale.ROOT)).executes(ctx -> {
                setChannelMode(ctx, mode);
                return 1;
            }));
        }
    }

    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> ctx, CommandSourceStack sender) {
        var mode = AEConfig.instance().getChannelMode();
        sender.sendSuccess(() -> PlayerMessages.ChannelModeCurrent.text(mode.name().toLowerCase(Locale.ROOT)), true);
    }

    private void setChannelMode(CommandContext<CommandSourceStack> ctx, ChannelMode mode) {
        AELog.info("%s is changing channel mode to %s", ctx.getSource(), mode);

        AEConfig.instance().setChannelModel(mode);
        AEConfig.instance().save();

        var gridCount = 0;
        for (Grid grid : TickHandler.instance().getGridList()) {
            grid.getPathingService().repath();
            gridCount++;
        }
        var finalGridCount = gridCount;

        var modeName = mode.name().toLowerCase(Locale.ROOT);
        ctx.getSource().sendSuccess(() -> PlayerMessages.ChannelModeSet.text(modeName, finalGridCount), true);
    }
}
