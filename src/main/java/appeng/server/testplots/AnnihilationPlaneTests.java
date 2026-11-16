package appeng.server.testplots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import appeng.api.config.Actionable;
import appeng.blockentity.networking.EnergyCellBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.me.service.EnergyService;
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

    /**
     * Regression test for #9074: With extremely high Efficiency, the energy required to break a block drops below the
     * minimum amount that energy cells can hand out, which used to cause the energy cell to be dropped as a power
     * provider and the network to appear drained.
     */
    @TestPlot("annihilation_plane_high_efficiency_power")
    public static void annihilationPlaneHighEfficiencyPower(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        var planePos = origin.above();
        var targetPos = planePos.east();

        plot.block(origin, AEBlocks.ENERGY_CELL);
        plot.cable(planePos)
                .partItem(Direction.EAST, AEParts.ANNIHILATION_PLANE, (registries, stack) -> {
                    var efficiency = registries.lookupOrThrow(Registries.ENCHANTMENT)
                            .getOrThrow(Enchantments.EFFICIENCY);
                    stack.enchant(efficiency, 95);
                });
        plot.storageDrive(planePos.west());

        plot.test(helper -> {
            var initialPower = new double[1];
            helper.startSequence()
                    .thenExecute(() -> helper.getGrid(origin)) // Wait for grid init
                    .thenExecute(() -> {
                        helper.getGrid(origin).getEnergyService().injectPower(1000, Actionable.MODULATE);
                    })
                    .thenIdle(10) // wait for the annihilation plane to get its tick and go to sleep
                    .thenExecute(() -> {
                        initialPower[0] = helper.getBlockEntity(origin, EnergyCellBlockEntity.class)
                                .getAECurrentPower();
                        helper.check(initialPower[0] > 0, "grid should have been charged", origin);
                        helper.setBlock(targetPos, Blocks.DIRT);
                    })
                    .thenWaitUntil(() -> helper.assertBlockPresent(Blocks.AIR, targetPos))
                    .thenExecute(() -> {
                        var cell = helper.getBlockEntity(origin, EnergyCellBlockEntity.class);
                        var energy = (EnergyService) helper.getGrid(origin).getEnergyService();
                        energy.refreshPower();
                        helper.check(energy.getStoredPower() > 0,
                                "energy cell was dropped as a power provider, grid has no power but cell has "
                                        + cell.getAECurrentPower(),
                                origin);
                    })
                    .thenSucceed();
        });
    }

}
