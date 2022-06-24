package appeng.crafting.simulation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.core.AELog;
import appeng.crafting.inv.CraftingSimulationState;
import appeng.crafting.simulation.helpers.ProcessingPatternBuilder;
import appeng.crafting.simulation.helpers.SimulationEnv;
import appeng.util.BootstrapMinecraft;

@BootstrapMinecraft
public class CraftingSimulationTest {
    @BeforeAll
    static void enableLog() {
        AELog.setCraftingLogEnabled(true);
        AELog.setDebugLogEnabled(true);
    }

    @Test
    public void testLazyPatternExploration() {
        var env = new SimulationEnv();

        var a = item(Items.COBBLESTONE);
        var b = item(Items.STONE);
        var c = item(Items.IRON_INGOT);

        var aToB = env.addPattern(new ProcessingPatternBuilder(b).addPreciseInput(1, a).build());
        var bToC = env.addPattern(new ProcessingPatternBuilder(c).addPreciseInput(1, b).build());

        // First plan should go through all the patterns because some items are missing.
        var firstPlan = env.runSimulation(c, CalculationStrategy.REPORT_MISSING_ITEMS);
        assertThatPlan(firstPlan)
                .failed()
                .patternsMatch(aToB, 1, bToC, 1)
                .emittedMatch()
                .usedMatch()
                .missingMatch(a)
                .bytesMatch(3, 3, 0);

        // Now, add some of b in the network. The plan should never explore a -> b, because it already has enough of b.
        // Notice the smaller node count for the bytes.
        env.addStoredItem(b);
        var secondPlan = env.runSimulation(c, CalculationStrategy.REPORT_MISSING_ITEMS);
        assertThatPlan(secondPlan)
                .succeeded()
                .patternsMatch(bToC, 1)
                .emittedMatch()
                .usedMatch(b)
                .bytesMatch(2, 2, 0);
    }

    @Test
    public void testWaterSubstitution() {
        var env = new SimulationEnv();

        var waterBucket = item(Items.WATER_BUCKET);
        var dirt = item(Items.DIRT);
        var grass = item(Items.GRASS);
        var water1mb = fluid(Fluids.WATER, 1);
        var water1000mb = mult(water1mb, 1000);

        // 1 dirt + 2x [1000 water or 1 water bucket] -> 1 grass.
        var grassPattern = env.addPattern(new ProcessingPatternBuilder(grass)
                .addPreciseInput(1, dirt)
                .addPreciseInput(2, water1000mb, waterBucket)
                .build());

        // Dirt is infinite.
        env.addEmitable(dirt.what());

        // Let's add 1 stored water bucket and 3500 mb.
        // We add a little bit more water to test that exact multiples of the template get extracted.
        env.addStoredItem(waterBucket);
        env.addStoredItem(fluid(Fluids.WATER, 3500));

        // Crafting should use the water bucket from the network and then the water directly.
        var plan = env.runSimulation(new GenericStack(grass.what(), 2), CalculationStrategy.REPORT_MISSING_ITEMS);
        assertThatPlan(plan)
                .succeeded()
                .patternsMatch(grassPattern, 2)
                .emittedMatch(mult(dirt, 2))
                .usedMatch(mult(waterBucket, 1), mult(water1mb, 3000))
                .bytesMatch(3, 8, 0);
    }

    @Test
    public void testMultiplePlanks() {
        var env = new SimulationEnv();

        var acaciaLog = item(Items.ACACIA_LOG);
        var acaciaPlanks = item(Items.ACACIA_PLANKS);
        var birchPlanks = item(Items.BIRCH_PLANKS);
        var oakPlanks = item(Items.OAK_PLANKS);
        var craftingTable = item(Items.CRAFTING_TABLE);

        var acaciaPattern = env.addPattern(new ProcessingPatternBuilder(new GenericStack(acaciaPlanks.what(), 4))
                .addPreciseInput(1, acaciaLog)
                .build());
        var tablePattern = env.addPattern(new ProcessingPatternBuilder(craftingTable)
                .addPreciseInput(4, acaciaPlanks, birchPlanks, oakPlanks)
                .build());

        env.addStoredItem(mult(acaciaPlanks, 2));
        env.addStoredItem(birchPlanks);
        env.addStoredItem(mult(oakPlanks, 2));

        // Network has 2 acacia planks, 1 birch plank, 2 oak planks and 1 acacia log. Should be enough for two tables.
        var successEnv = env.copy();
        successEnv.addStoredItem(acaciaLog);

        var successPlan = successEnv.runSimulation(new GenericStack(craftingTable.what(), 2),
                CalculationStrategy.REPORT_MISSING_ITEMS);
        assertThatPlan(successPlan)
                .succeeded()
                .patternsMatch(tablePattern, 2, acaciaPattern, 1)
                .emittedMatch()
                .usedMatch(mult(acaciaPlanks, 2), birchPlanks, mult(oakPlanks, 2), acaciaLog);
        // Note that 2 oak planks were used even though only 1 is actually useful.
        // That's because the simulation sees it can extract 5 planks before it attempts to craft more.

        // Network only has 2 acacia planks, 1 birch plank, 2 oak planks for two tables. 1 acacia log is missing.
        var failurePlan = env.runSimulation(new GenericStack(craftingTable.what(), 2),
                CalculationStrategy.REPORT_MISSING_ITEMS);
        assertThatPlan(failurePlan)
                .failed()
                .patternsMatch(tablePattern, 2, acaciaPattern, 1)
                .emittedMatch()
                .missingMatch(acaciaLog)
                .usedMatch(mult(acaciaPlanks, 2), birchPlanks, mult(oakPlanks, 2));
    }

    @Test
    public void testReusedBuckets() {
        var env = new SimulationEnv();

        var emptyBucket = item(Items.BUCKET);
        var waterBucket = item(Items.WATER_BUCKET);
        var grass = item(Items.GRASS);
        var dirt = item(Items.DIRT);
        var water1000mb = fluid(Fluids.WATER, 1000);

        var grassPattern = env.addPattern(new ProcessingPatternBuilder(grass)
                .addPreciseInput(1, dirt)
                .addPreciseInput(1, true, waterBucket)
                .build());
        var bucketFilling = env.addPattern(new ProcessingPatternBuilder(waterBucket)
                .addPreciseInput(1, emptyBucket)
                .addPreciseInput(1, water1000mb)
                .build());

        env.addStoredItem(emptyBucket);
        env.addStoredItem(mult(dirt, 10000));
        env.addEmitable(water1000mb.what());

        var plan = env.runSimulation(mult(grass, 100), CalculationStrategy.REPORT_MISSING_ITEMS);
        assertThatPlan(plan)
                .succeeded()
                .patternsMatch(grassPattern, 100, bucketFilling, 100)
                .emittedMatch(mult(water1000mb, 100))
                .usedMatch(emptyBucket, mult(dirt, 100))
                .bytesMatch(5, 500, 100);
        // the important thing is that the bucket was reused, so only 1 needed to be extracted from the network!
    }

    @Test
    public void testDamagedOutput() {
        testDamagedOutput(false);
        testDamagedOutput(true);
    }

    @Test
    public void testNonsensicalRecursivePattern() {
        var env = new SimulationEnv();

        var water1B = fluid(Fluids.WATER, 1000);

        env.addPattern(new ProcessingPatternBuilder(water1B)
                .addPreciseInput(1, water1B)
                .build());

        env.addStoredItem(water1B);

        var plan = env.runSimulation(water1B, CalculationStrategy.REPORT_MISSING_ITEMS);
        assertThatPlan(plan).failed();
    }

    public void testDamagedOutput(boolean branching) {
        var env = new SimulationEnv();

        var pickaxeStack = new ItemStack(Items.DIAMOND_PICKAXE);
        pickaxeStack.setDamageValue(100);
        var damagedPickaxe = GenericStack.fromItemStack(pickaxeStack);
        var cobble = item(Items.COBBLESTONE);
        var stone = item(Items.STONE);
        var diamond = item(Items.DIAMOND);
        var stick = item(Items.STICK);

        var cobblePattern = env.addPattern(new ProcessingPatternBuilder(cobble)
                .addPreciseInput(1, stone)
                .addDamageableInput(((AEItemKey) damagedPickaxe.what()).getItem())
                .build());
        // "By mistake" the pattern was encoded with a damaged output, test that it still behaves as expected!
        // In some cases, this mistake is not easy to spot (e.g. with seeds), hence we support this mistake.
        // This is what the weird checks in CraftingService#getCraftingFor are for.
        var pickaxePattern = env.addPattern(new ProcessingPatternBuilder(damagedPickaxe)
                .addPreciseInput(3, diamond)
                .addPreciseInput(2, stick)
                .build());
        if (branching) {
            // Just to toggle the branching behavior in the node.
            // This pattern is not craftable so it has no effect on the results of the calculation.
            env.addPattern(new ProcessingPatternBuilder(damagedPickaxe)
                    .addPreciseInput(42, item(Items.BEDROCK))
                    .build());
        }

        env.addStoredItem(mult(stone, 1000));
        env.addStoredItem(mult(diamond, 3));
        env.addStoredItem(mult(stick, 2));

        var plan = env.runSimulation(mult(cobble, 100), CalculationStrategy.REPORT_MISSING_ITEMS);
        assertThatPlan(plan)
                .succeeded()
                .patternsMatch(cobblePattern, 100, pickaxePattern, 1)
                .emittedMatch()
                .usedMatch(mult(stone, 100), mult(diamond, 3), mult(stick, 2))
                .bytesMatch(branching ? 6 : 5, 300 + 3 + 2, 100);
        // note that the pickaxe is only crafted once, and then reused!
    }

    /**
     * Ensure that the {@link CraftingSimulationState#ignore} call doesn't crash when the network has a fuzzy equivalent
     * item, but not the exact output.
     *
     * See https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/5595.
     */
    @Test
    public void testCraftWithFuzzyInNetwork() {
        var env = new SimulationEnv();

        var input = item(Items.COBBLESTONE);
        var undamagedOutput = item(Items.DIAMOND_PICKAXE);
        var damagedOutputStack = new ItemStack(Items.DIAMOND_PICKAXE);
        damagedOutputStack.setDamageValue(10);
        var damagedOutput = GenericStack.fromItemStack(damagedOutputStack);

        var pattern = env.addPattern(new ProcessingPatternBuilder(undamagedOutput)
                .addPreciseInput(1, input)
                .build());

        env.addStoredItem(damagedOutput.what(), damagedOutput.amount());

        // Make sure this doesn't crash.
        var plan = env.runSimulation(undamagedOutput, CalculationStrategy.REPORT_MISSING_ITEMS);
        assertThatPlan(plan)
                .failed()
                .patternsMatch(pattern, 1)
                .emittedMatch()
                .missingMatch(input)
                .usedMatch()
                .bytesMatch(2, 2, 0);
    }

    /**
     * Basic test for multiple paths (multiple patterns that produce the same output).
     */
    @Test
    public void testMultiplePaths() {
        var env = new SimulationEnv();

        var input1 = item(Items.COBBLESTONE);
        var input2 = item(Items.OAK_PLANKS);
        var output = item(Items.DIAMOND);

        var pattern1 = env.addPattern(new ProcessingPatternBuilder(output).addPreciseInput(1, input1).build());
        var pattern2 = env.addPattern(new ProcessingPatternBuilder(output).addPreciseInput(1, input2).build());

        env.addStoredItem(input1.what(), 5);
        env.addStoredItem(input2.what(), 3);

        // should be able to make 3+5 = 8 items
        var plan = env.runSimulation(mult(output, 8), CalculationStrategy.REPORT_MISSING_ITEMS);
        assertThatPlan(plan)
                .succeeded()
                .patternsMatch(pattern1, 5, pattern2, 3)
                .emittedMatch()
                .missingMatch()
                .usedMatch(mult(input1, 5), mult(input2, 3));
    }

    /**
     * Basic test for {@link CalculationStrategy#CRAFT_LESS}.
     */
    @Test
    public void testAdaptiveOrder() {
        var env = new SimulationEnv();

        var input = item(Items.COBBLESTONE);
        var output = item(Items.STONE);

        var pattern = env.addPattern(new ProcessingPatternBuilder(output).addPreciseInput(1, input).build());

        // should fallback to REPORT_MISSING_ITEMS since not even 1 can be crafted
        var plan1 = env.runSimulation(mult(output, 1000), CalculationStrategy.CRAFT_LESS);
        assertThatPlan(plan1)
                .failed()
                .patternsMatch(pattern, 1000)
                .outputMatches(mult(output, 1000));

        // should succeed with exactly 547 crafted
        env.addStoredItem(mult(input, 547));
        var plan2 = env.runSimulation(mult(output, 1000), CalculationStrategy.CRAFT_LESS);
        assertThatPlan(plan2)
                .succeeded()
                .patternsMatch(pattern, 547)
                .outputMatches(mult(output, 547))
                .usedMatch(mult(input, 547));
    }

    /**
     * This tests that leftovers from previous crafting iterations reused in a subsequent iteration, are not scheduled
     * for extraction from the network.
     * <p>
     * Regression test for https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/6391.
     */
    @Test
    public void testReusedLeftovers() {
        var env = new SimulationEnv();

        var output = item(Items.DIAMOND_BLOCK);
        // There is a recipe to produce 4 of this item, and one to use 3 only. The extra 1 should be reused.
        var reusedItem = item(Items.DIAMOND);
        // Used to produce reusedItem
        var sourceItem = item(Items.DIAMOND_ORE);
        // Alternative ingredient for output. Not present in the network, but will create a branch in the crafting
        // tree that will force our interesting recipe to be crafted 1 by 1.
        var alternativeIngredient = item(Items.GOLD_INGOT);

        var targetPattern = env.addPattern(new ProcessingPatternBuilder(output).addPreciseInput(3, reusedItem).build());
        var alternativePattern = env
                .addPattern(new ProcessingPatternBuilder(output).addPreciseInput(1, alternativeIngredient).build());
        var sourcePattern = env
                .addPattern(new ProcessingPatternBuilder(mult(reusedItem, 4)).addPreciseInput(1, sourceItem).build());

        env.addStoredItem(mult(sourceItem, 3));

        var plan = env.runSimulation(mult(output, 4), CalculationStrategy.REPORT_MISSING_ITEMS);
        assertThatPlan(plan)
                .succeeded()
                .patternsMatch(targetPattern, 4, sourcePattern, 3)
                .usedMatch(mult(sourceItem, 3));
    }

    private static GenericStack item(Item item) {
        return GenericStack.fromItemStack(new ItemStack(item));
    }

    private static GenericStack fluid(Fluid fluid, int amount) {
        // The tag prevents a ClassCastException when Fluid is cast to FluidVariantCache
        return new GenericStack(AEFluidKey.of(fluid), amount * AEFluidKey.AMOUNT_BUCKET / 1000);
    }

    private static GenericStack mult(GenericStack template, long multiplier) {
        return new GenericStack(template.what(), template.amount() * multiplier);
    }

    private static CraftingPlanAssert assertThatPlan(ICraftingPlan plan) {
        return new CraftingPlanAssert(plan);
    }

    private static class CraftingPlanAssert {
        private final ICraftingPlan plan;

        private CraftingPlanAssert(ICraftingPlan plan) {
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

        private CraftingPlanAssert listMatches(KeyCounter actualList, GenericStack... expectedStacks) {
            var expectedList = new KeyCounter();
            for (var stack : expectedStacks) {
                expectedList.add(stack.what(), stack.amount());
            }
            assertThat(actualList.size()).isEqualTo(expectedList.size());
            for (var expected : expectedList) {
                var actual = actualList.get(expected.getKey());

                assertThat(actual).isEqualTo(expected.getLongValue());
            }

            return this;
        }

        public CraftingPlanAssert emittedMatch(GenericStack... emittedStacks) {
            return listMatches(plan.emittedItems(), emittedStacks);
        }

        public CraftingPlanAssert missingMatch(GenericStack... missingStacks) {
            return listMatches(plan.missingItems(), missingStacks);
        }

        public CraftingPlanAssert usedMatch(GenericStack... usedStacks) {
            return listMatches(plan.usedItems(), usedStacks);
        }

        public CraftingPlanAssert bytesMatch(long nodeCount, long nodeRequests, long containerItems) {
            long patternBytes = plan.patternTimes().values().stream().reduce(0L, Long::sum);
            long totalBytes = nodeCount * 8 + patternBytes + nodeRequests + containerItems;
            assertThat(plan.bytes()).isEqualTo(totalBytes);
            return this;
        }

        public CraftingPlanAssert outputMatches(GenericStack output) {
            assertThat(plan.finalOutput()).isEqualTo(output);
            return this;
        }
    }
}
