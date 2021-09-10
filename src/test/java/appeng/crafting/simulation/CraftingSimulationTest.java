package appeng.crafting.simulation;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import java.util.Objects;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import appeng.api.networking.crafting.IPatternDetails;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.crafting.CraftingPlan;
import appeng.crafting.simulation.helpers.ProcessingPatternBuilder;
import appeng.crafting.simulation.helpers.SimulationEnv;
import appeng.util.BootstrapMinecraft;
import appeng.util.item.AEItemStack;

@BootstrapMinecraft
public class CraftingSimulationTest {
    @BeforeAll
    static void enableLog() {
        AELog.setCraftingLogEnabled(true);
        AELog.setDebugLogEnabled(true);
    }

    @Test
    public void testWaterSubstitution() {
        var env = new SimulationEnv();

        IAEItemStack waterBucket = item(Items.WATER_BUCKET);
        IAEItemStack dirt = item(Items.DIRT);
        IAEItemStack grass = item(Items.GRASS);
        // We use diamonds instead of water because fluid stacks are not yet supported.
        IAEItemStack water1mb = item(Items.DIAMOND);
        IAEItemStack water1000mb = water1mb.copyWithStackSize(1000);

        // 1 dirt + 2x [1000 water or 1 water bucket] -> 1 grass.
        var grassPattern = env.addPattern(new ProcessingPatternBuilder(grass)
                .addPreciseInput(1, dirt)
                .addPreciseInput(2, water1000mb, waterBucket)
                .build());

        // Dirt is infinite.
        env.addEmitable(dirt);

        // Let's add 1 stored water bucket and 3500 mb.
        // We add a little bit more water to test that exact multiples of the template get extracted.
        env.addStoredItem(waterBucket);
        env.addStoredItem(water1000mb.copyWithStackSize(3500));

        // Crafting should use the water bucket from the network and then the water directly.
        var plan = env.runSimulation(grass.copyWithStackSize(2));
        assertThatPlan(plan)
                .succeeded()
                .patternsMatch(grassPattern, 2)
                .emittedMatch(mult(dirt, 2))
                .usedMatch(mult(waterBucket, 1), mult(water1mb, 3000));
    }

    @Test
    public void testMultiplePlanks() {
        var env = new SimulationEnv();

        var acaciaLog = item(Items.ACACIA_LOG);
        var acaciaPlanks = item(Items.ACACIA_PLANKS);
        var birchPlanks = item(Items.BIRCH_PLANKS);
        var oakPlanks = item(Items.OAK_PLANKS);
        var craftingTable = item(Items.CRAFTING_TABLE);

        var acaciaPattern = env.addPattern(plankRecipe(acaciaPlanks, acaciaLog));
        var tablePattern = env.addPattern(new ProcessingPatternBuilder(craftingTable)
                .addPreciseInput(4, acaciaPlanks, birchPlanks, oakPlanks)
                .build());

        env.addStoredItem(mult(acaciaPlanks, 2));
        env.addStoredItem(birchPlanks);
        env.addStoredItem(mult(oakPlanks, 2));

        // Network has 2 acacia planks, 1 birch plank, 2 oak planks and 1 acacia log. Should be enough for two tables.
        var successEnv = env.copy();
        successEnv.addStoredItem(acaciaLog);

        var successPlan = successEnv.runSimulation(craftingTable.copyWithStackSize(2));
        assertThatPlan(successPlan)
                .succeeded()
                .patternsMatch(tablePattern, 2, acaciaPattern, 1)
                .emittedMatch()
                .usedMatch(mult(acaciaPlanks, 2), birchPlanks, mult(oakPlanks, 2), acaciaLog);
        // Note that 2 oak planks were used even though only 1 is actually useful.
        // That's because the simulation sees it can extract 5 planks before it attempts to craft more.

        // Network only has 2 acacia planks, 1 birch plank, 2 oak planks for two tables. 1 acacia log is missing.
        var failurePlan = env.runSimulation(craftingTable.copyWithStackSize(2));
        assertThatPlan(failurePlan)
                .failed()
                .patternsMatch(tablePattern, 2, acaciaPattern, 1)
                .emittedMatch()
                .missingMatch(acaciaLog)
                .usedMatch(mult(acaciaPlanks, 2), birchPlanks, mult(oakPlanks, 2));
    }

    private static IAEItemStack item(Item item) {
        return AEItemStack.fromItemStack(new ItemStack(item));
    }

    private static IPatternDetails plankRecipe(IAEItemStack plank, IAEItemStack log) {
        return new ProcessingPatternBuilder(plank.copyWithStackSize(4)).addPreciseInput(1, log).build();
    }

    private static IAEItemStack mult(IAEItemStack template, long multiplier) {
        return template.copy().multStackSize(multiplier);
    }

    private static CraftingPlanAssert assertThatPlan(CraftingPlan plan) {
        return new CraftingPlanAssert(plan);
    }

    private static class CraftingPlanAssert {
        private final CraftingPlan plan;

        private CraftingPlanAssert(CraftingPlan plan) {
            this.plan = Objects.requireNonNull(plan);
        }

        public CraftingPlanAssert succeeded() {
            assertThat(plan.simulation()).isFalse();
            assertThat(plan.missingItems()).isEmpty();
            return this;
        }

        public CraftingPlanAssert failed() {
            assertThat(plan.simulation()).isTrue();
            assertThat(plan.missingItems()).isNotEmpty();
            return this;
        }

        public CraftingPlanAssert patternsMatch(Map<IPatternDetails, Long> patternTimes) {
            assertThat(plan.patternTimes()).isEqualTo(patternTimes);
            return this;
        }

        public CraftingPlanAssert patternsMatch(IPatternDetails p1, long t1) {
            return patternsMatch(Map.of(p1, t1));
        }

        public CraftingPlanAssert patternsMatch(IPatternDetails p1, long t1, IPatternDetails p2, long t2) {
            return patternsMatch(Map.of(p1, t1, p2, t2));
        }

        public CraftingPlanAssert patternsMatch(IPatternDetails p1, long t1, IPatternDetails p2, long t2,
                IPatternDetails p3, long t3) {
            return patternsMatch(Map.of(p1, t1, p2, t2, p3, t3));
        }

        private CraftingPlanAssert listMatches(IItemList<IAEItemStack> actualList, IAEItemStack... expectedStacks) {
            var expectedList = StorageChannels.items().createList();
            for (IAEItemStack stack : expectedStacks) {
                expectedList.addStorage(stack);
            }
            var expected = Lists.newArrayList(expectedList);
            var actual = Lists.newArrayList(actualList);
            assertThat(expected.size()).isEqualTo(actual.size());

            for (int i = 0; i < expected.size(); ++i) {
                var expectedStack = expected.get(i);
                var actualStack = actual.get(i);
                assertThat(actualStack).isEqualTo(expectedStack);
                // Must check the amount, equals only checks the type.
                assertThat(actualStack.getStackSize()).isEqualTo(expectedStack.getStackSize());
            }

            return this;
        }

        public CraftingPlanAssert emittedMatch(IAEItemStack... emittedStacks) {
            return listMatches(plan.emittedItems(), emittedStacks);
        }

        public CraftingPlanAssert missingMatch(IAEItemStack... missingStacks) {
            return listMatches(plan.missingItems(), missingStacks);
        }

        public CraftingPlanAssert usedMatch(IAEItemStack... usedStacks) {
            return listMatches(plan.usedItems(), usedStacks);
        }
    }
}
