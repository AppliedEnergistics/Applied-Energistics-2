package appeng.server.subcommands;

import static net.minecraft.commands.Commands.literal;

import java.util.ArrayList;
import java.util.Collections;

import com.google.common.base.Stopwatch;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import org.jetbrains.annotations.Nullable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.FlatLevelSource;

import appeng.core.AELog;
import appeng.core.definitions.AEItems;
import appeng.core.localization.PlayerMessages;
import appeng.items.tools.powered.ColorApplicatorItem;
import appeng.server.ISubCommand;
import appeng.server.testplots.KitOutPlayerEvent;
import appeng.server.testplots.TestPlots;
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
                sender.sendFailure(PlayerMessages.TestWorldNotInCreativeMode.text());
                return;
            }

            var level = player.serverLevel();
            if (!isSuperflatWorld(level)) {
                sender.sendFailure(PlayerMessages.TestWorldNotInSuperflat.text());
                return;
            }

            changeGameRules(srv);
            removeAllEntitiesButPlayer(srv);

            // Pick the top layer of the superflat world, or default to 60
            var origin = player.blockPosition();
            // Ensure the origin is 3 blocks above the lower build limit
            if (origin.getY() - 3 < level.getMinBuildHeight()) {
                origin = origin.atY(level.getMinBuildHeight() + 3);
            }
            var generator = new TestWorldGenerator(level, player, origin, plotId);
            generator.generate();

            player.getAbilities().flying = true;
            player.onUpdateAbilities();

            kitOutPlayer(player);

            // Only teleport the player if they're not within the bounds already
            if (!generator.isWithinBounds(player.blockPosition())) {
                var goodStartPos = generator.getSuitableStartPos();
                player.teleportTo(level, goodStartPos.getX(), goodStartPos.getY(), goodStartPos.getZ(), 0, 0);
            }

            sender.sendSuccess(() -> PlayerMessages.TestWorldSetupComplete.text(sw), true);
        } catch (RuntimeException | CommandSyntaxException e) {
            AELog.error(e);
            sender.sendFailure(PlayerMessages.TestWorldSetupFailed.text(e));
        }
    }

    private void removeAllEntitiesButPlayer(MinecraftServer srv) {
        for (var level : srv.getAllLevels()) {
            var entities = new ArrayList<Entity>();
            level.getEntities(EntityTypeTest.forClass(Entity.class), e -> true, entities);
            for (var entity : entities) {
                if (entity instanceof Player) {
                    continue;
                }
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    private void kitOutPlayer(ServerPlayer player) {
        var playerInv = player.getInventory();
        var fullApplicator = ColorApplicatorItem.createFullColorApplicator();
        if (!playerInv.hasAnyOf(Collections.singleton(AEItems.COLOR_APPLICATOR.asItem()))) {
            playerInv.placeItemBackInInventory(fullApplicator);
        }
        KitOutPlayerEvent.EVENT.invoker().accept(player);
    }

    /**
     * Disable night and weather.
     */
    private static void changeGameRules(MinecraftServer srv) {
        makeAlwaysDaytime(srv);
        disableWeather(srv);
        disableMobSpawning(srv);
    }

    private static void makeAlwaysDaytime(MinecraftServer srv) {
        srv.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, srv);
        srv.overworld().setDayTime(1000);
    }

    private static void disableWeather(MinecraftServer srv) {
        srv.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, srv);
        srv.overworld().setWeatherParameters(9999, 0, false, false);
    }

    private static void disableMobSpawning(MinecraftServer srv) {
        srv.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, srv);
    }

    private static boolean isSuperflatWorld(ServerLevel level) {
        var generator = level.getChunkSource().getGenerator();
        return generator instanceof FlatLevelSource;
    }
}
