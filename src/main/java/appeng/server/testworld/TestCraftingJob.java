package appeng.server.testworld;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.stacks.AEKey;
import appeng.core.AELog;
import appeng.me.helpers.BaseActionSource;
import appeng.me.helpers.MachineSource;

public class TestCraftingJob {
    private final PlotTestHelper helper;
    private final BlockPos gridOrigin;
    private final AEKey what;
    private final long amount;
    private final CalculationStrategy strategy;
    @Nullable
    private Future<ICraftingPlan> planFuture;
    @Nullable
    private ICraftingPlan plan;
    @Nullable
    private ICraftingLink link;

    public TestCraftingJob(PlotTestHelper helper, BlockPos gridOrigin, AEKey what, long amount) {
        this(helper, gridOrigin, what, amount, CalculationStrategy.REPORT_MISSING_ITEMS);
    }

    public TestCraftingJob(PlotTestHelper helper, BlockPos gridOrigin, AEKey what, long amount,
            CalculationStrategy strategy) {
        this.what = what;
        this.amount = amount;
        this.strategy = strategy;
        this.helper = helper;
        this.gridOrigin = gridOrigin;
    }

    /**
     * Use this with {@link net.minecraft.gametest.framework.GameTestSequence#thenWaitUntil(Runnable)} to wait until the
     * job has been planned and started.
     */
    public void tickUntilStarted() {
        if (planFuture == null) {
            var grid = helper.getGrid(gridOrigin);
            var src = new MachineSource(grid::getPivot);
            var craftingService = grid.getCraftingService();
            ICraftingSimulationRequester simRequester = () -> src;
            planFuture = craftingService.beginCraftingCalculation(grid.getPivot().getLevel(), simRequester,
                    what, amount, strategy);
        }
        if (plan == null) {
            try {
                plan = planFuture.get(0, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException e) {
                AELog.error(e);
                throw new GameTestAssertException("Crafting job planning failed: " + e);
            } catch (TimeoutException e) {
                throw new GameTestAssertException("Crafting job planning did not complete");
            }
        }
        if (link == null) {
            var grid = helper.getGrid(BlockPos.ZERO);
            link = grid.getCraftingService().submitJob(plan, null, null, true,
                    new BaseActionSource());
            helper.check(link != null, "failed to submit job");
        }
    }
}
