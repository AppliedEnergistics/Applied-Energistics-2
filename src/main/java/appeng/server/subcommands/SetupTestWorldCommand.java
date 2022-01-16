package appeng.server.subcommands;

import static net.minecraft.commands.Commands.literal;

import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.Stopwatch;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.levelgen.FlatLevelSource;

import appeng.core.AELog;
import appeng.server.ISubCommand;
import appeng.server.testworld.TestPlots;
import appeng.server.testworld.TestWorldGenerator;

/**
 * This command will verify the user is in creative mode, the world is a flat world with void preset, and then start
 * setting up a testing world for AE2.
 */
public class SetupTestWorldCommand implements ISubCommand {
    @Override
    public void addArguments(LiteralArgumentBuilder<CommandSourceStack> builder) {
        for (var plotId : TestPlots.getPlotIds()) {
            builder.then(literal(plotId.toString()).executes(ctx -> {
                setupTestWorld(ctx.getSource().getServer(), ctx.getSource(), plotId);
                return 1;
            }));
        }
    }

    @Override
    public void call(MinecraftServer srv, CommandContext<CommandSourceStack> ctx, CommandSourceStack sender) {
        setupTestWorld(srv, sender, null);
    }

    private void setupTestWorld(MinecraftServer srv, CommandSourceStack sender, @Nullable ResourceLocation plotId) {
        var sw = Stopwatch.createStarted();
        try {
            var player = sender.getPlayerOrException();
            if (!player.isCreative()) {
                sender.sendFailure(new TextComponent("Command can only be used in creative mode."));
                return;
            }

            var level = srv.overworld();
            if (!isVoidWorld(level)) {
                sender.sendFailure(new TextComponent(
                        "A test world can only be set up in a Superflat world with the Void preset!"));
                return;
            }

            changeGameRules(srv);

            var origin = new BlockPos(0, 60, 0);
            var generator = new TestWorldGenerator(level, player, origin, plotId);
            generator.generate();

            player.getAbilities().flying = true;
            player.onUpdateAbilities();

            // Only teleport the player if they're not within the bounds already
            if (!generator.isWithinBounds(player.blockPosition())) {
                var goodStartPos = generator.getSuitableStartPos();
                player.teleportTo(level, goodStartPos.getX(), goodStartPos.getY(), goodStartPos.getZ(), 0, 0);
            }

            sender.sendSuccess(new TextComponent("Test world setup completed in " + sw), true);
        } catch (RuntimeException | CommandSyntaxException e) {
            AELog.error(e);
            sender.sendFailure(new TextComponent("Setting up the test world failed: " + e));
        }
    }

    /**
     * Disable night and weather.
     */
    private static void changeGameRules(MinecraftServer srv) {
        makeAlwaysDaytime(srv);
        disableWeather(srv);
    }

    private static void makeAlwaysDaytime(MinecraftServer srv) {
        srv.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, srv);
        srv.overworld().setDayTime(1000);
    }

    private static void disableWeather(MinecraftServer srv) {
        srv.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, srv);
        srv.overworld().setWeatherParameters(9999, 0, false, false);
    }

    private static boolean isVoidWorld(ServerLevel level) {
        var generator = level.getChunkSource().getGenerator();
        if (!(generator instanceof FlatLevelSource flatLevelSource)) {
            return false;
        }

        // Only allow actual void worlds
        return flatLevelSource.settings().getLayers().stream().allMatch(Objects::isNull);
    }
}
