package appeng.crafting.simulation;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.crafting.simulation.helpers.ProcessingPatternBuilder;
import appeng.crafting.simulation.helpers.SimulationEnv;
import appeng.util.BootstrapMinecraft;
import appeng.util.item.AEItemStack;

@BootstrapMinecraft
public class WaterSubstitutionTest {
    @BeforeAll
    static void enableLog() {
        AELog.setCraftingLogEnabled(true);
        AELog.setDebugLogEnabled(true);
    }

    @Test
    public void testWaterSubstitution() {
        var env = new SimulationEnv();

        IAEItemStack waterBucket = AEItemStack.fromItemStack(new ItemStack(Items.WATER_BUCKET));
        IAEItemStack dirt = AEItemStack.fromItemStack(new ItemStack(Items.DIRT));
        IAEItemStack grass = AEItemStack.fromItemStack(new ItemStack(Items.GRASS));
        // We use diamonds instead of water because fluid stacks are not yet supported.
        IAEItemStack water1mb = AEItemStack.fromItemStack(new ItemStack(Items.DIAMOND));
        IAEItemStack water1000mb = water1mb.copyWithStackSize(1000);

        // 1 dirt + 2x [1000 water or 1 water bucket] -> 1 grass.
        var grassPattern = env.addPattern(new ProcessingPatternBuilder(grass)
                .addStrictInput(1, dirt)
                .addStrictInput(2, water1000mb, waterBucket)
                .build());

        // Dirt is infinite.
        env.addEmitable(dirt);

        // Let's add 1 stored water bucket and 3500 mb.
        // We add a little bit more water to test that exact multiples of the template get extracted.
        env.addStoredItem(waterBucket);
        env.addStoredItem(water1000mb.copyWithStackSize(3500));

        // Crafting should use the water bucket from the network and then the water directly.
        var plan = env.runSimulation(grass.copyWithStackSize(2));

        assertThat(plan.simulation()).isFalse();
        assertThat(plan.missingItems()).isEmpty();

        assertThat(plan.patternTimes()).isEqualTo(Map.of(grassPattern, 2L));

        assertStackSize(plan.emittedItems(), dirt, 2);

        assertStackSize(plan.usedItems(), waterBucket, 1);
        assertStackSize(plan.usedItems(), water1mb, 3000);
    }

    private static <T extends IAEStack<T>> void assertStackSize(IItemList<T> list, T what, long count) {
        assertThat(list.findPrecise(what).getStackSize()).isEqualTo(count);
    }
}
