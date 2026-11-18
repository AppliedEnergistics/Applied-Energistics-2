package appeng.server.testplots;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

import appeng.api.stacks.AEItemKey;
import appeng.core.definitions.AEParts;
import appeng.core.network.serverbound.PartLeftClickPacket;
import appeng.parts.reporting.ConversionMonitorPart;
import appeng.server.testworld.PlotBuilder;

@TestPlotClass
public final class PartLeftClickTestPlots {
    private PartLeftClickTestPlots() {
    }

    /**
     * The hit position in {@link PartLeftClickPacket} comes from the client, so clicking a part must only work when the
     * player could actually reach it.
     */
    @TestPlot("part_left_click_requires_reach")
    public static void leftClickRequiresReach(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.drive(origin).addCreativeCell().add(Items.DIAMOND);
        plot.cable(origin.above()).part(Direction.NORTH, AEParts.CONVERSION_MONITOR, monitor -> {
            monitor.setConfiguredItem(AEItemKey.of(Items.DIAMOND));
        });

        plot.test(helper -> {
            var monitorPos = origin.above();
            helper.startSequence()
                    .thenWaitUntil(() -> helper.check(
                            helper.getPart(monitorPos, Direction.NORTH, ConversionMonitorPart.class).isActive(),
                            "conversion monitor is not active"))
                    .thenExecute(() -> {
                        var absPos = helper.absolutePos(monitorPos);
                        // Click on the monitor, which sits on the north face of the cable bus
                        var hitResult = new BlockHitResult(Vec3.atLowerCornerOf(absPos).add(0.5, 0.5, 0.05),
                                Direction.NORTH, absPos, false);

                        // Use a separate fake player, so we don't interfere with the shared one
                        var player = new FakePlayer(helper.getLevel(),
                                new GameProfile(UUID.randomUUID(), "test-part-left-click"));

                        // Out of reach: nothing should be extracted
                        var farPos = absPos.north(20);
                        player.setPos(farPos.getX() + 0.5, farPos.getY() - 1, farPos.getZ() + 0.5);
                        new PartLeftClickPacket(hitResult, false).handleOnServer(player);
                        helper.check(player.getInventory().countItem(Items.DIAMOND) == 0,
                                "player out of reach should not receive diamonds");

                        // Within reach: a full stack should be extracted
                        var nearPos = absPos.north(2);
                        player.setPos(nearPos.getX() + 0.5, nearPos.getY() - 1, nearPos.getZ() + 0.5);
                        new PartLeftClickPacket(hitResult, false).handleOnServer(player);
                        helper.check(player.getInventory().countItem(Items.DIAMOND) == 64,
                                "player within reach should receive a stack of diamonds, but got "
                                        + player.getInventory().countItem(Items.DIAMOND));
                    })
                    .thenSucceed();
        });
    }
}
