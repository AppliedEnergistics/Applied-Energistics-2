package appeng.client.command;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import jdk.jfr.FlightRecorder;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;

/**
 * Just a client-side equivalent of {@link net.minecraft.server.commands.JfrCommand}.
 */
public class ClientProfilingCommand {
    private static final SimpleCommandExceptionType START_FAILED = new SimpleCommandExceptionType(
            new TranslatableComponent("commands.jfr.start.failed"));
    private static final DynamicCommandExceptionType DUMP_FAILED = new DynamicCommandExceptionType(
            object -> new TranslatableComponent("commands.jfr.dump.failed", object));

    public static void register(CommandDispatcher<FabricClientCommandSource> commandDispatcher) {
        FlightRecorder.register(ClientPacketReceived.class);

        commandDispatcher.register(
                literal("ae2clientjfr")
                        .then(literal("start")
                                .executes(ctx -> startJfr(ctx.getSource())))
                        .then(literal("stop")
                                .executes(ctx -> stopJfr(ctx.getSource()))));
    }

    private static int startJfr(FabricClientCommandSource source) throws CommandSyntaxException {
        if (!JvmProfiler.INSTANCE.start(Environment.CLIENT)) {
            throw START_FAILED.create();
        }
        source.sendFeedback(new TranslatableComponent("commands.jfr.started"));
        return 1;
    }

    private static int stopJfr(FabricClientCommandSource source) throws CommandSyntaxException {
        try {
            Path path = Paths.get(".").relativize(JvmProfiler.INSTANCE.stop().normalize());
            Path path2 = path.toAbsolutePath();
            MutableComponent component = new TextComponent(path.toString()).withStyle(ChatFormatting.UNDERLINE)
                    .withStyle(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, path2.toString()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    new TranslatableComponent("chat.copy.click"))));
            source.sendFeedback(new TranslatableComponent("commands.jfr.stopped", component));
            return 1;
        } catch (Throwable path) {
            throw DUMP_FAILED.create(path.getMessage());
        }
    }
}
