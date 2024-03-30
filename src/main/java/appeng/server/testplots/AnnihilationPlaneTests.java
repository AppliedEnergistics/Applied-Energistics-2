package appeng.server.testplots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import appeng.core.definitions.AEParts;
import appeng.server.testworld.PlotBuilder;
import appeng.util.Platform;

@TestPlotClass
public class AnnihilationPlaneTests {

    @TestPlot("annihilation_plane_seed_farm")
    public static void annihilationPlaneSeedFarm(PlotBuilder plot) {

        var origin = BlockPos.ZERO;
        var grassPos = origin.east();

        plot.block(grassPos, Blocks.GRASS_BLOCK);
        plot.creativeEnergyCell(origin);
        plot.cable(origin.above())
                .part(Direction.EAST, AEParts.ANNIHILATION_PLANE);
        plot.storageDrive(origin.above().west());

        plot.test(helper -> {
            helper.startSequence()
                    .thenExecute(() -> helper.getGrid(origin)) // Wait for grid init
                    .thenIdle(10) // wait for the annihilation plane to get its tick and go to sleep
                    .thenExecute(() -> {
                        // Bone-meal the grassblock
                        var stack = Items.BONE_MEAL.getDefaultInstance();
                        var hitOnTop = new BlockHitResult(new Vec3(0.5, 1, 0.5), Direction.UP,
                                helper.absolutePos(grassPos), false);
                        var fakePlayer = Platform.getFakePlayer(helper.getLevel(), null);
                        var useCtx = new UseOnContext(helper.getLevel(), fakePlayer, InteractionHand.MAIN_HAND, stack,
                                hitOnTop);
                        stack.useOn(useCtx);
                    })
                    // assert something was placed
                    .thenExecute(() -> helper.assertBlockNotPresent(Blocks.AIR, grassPos.above()))
                    .thenWaitUntil(() -> {
                        // Assume the grass is gone
                        helper.assertBlock(grassPos.above(), Blocks.AIR::equals, "expected air");
                    })
                    .thenSucceed();
        });

    }

}
