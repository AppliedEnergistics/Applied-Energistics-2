package appeng.crafting.simulation;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.crafting.simulation.helpers.ProcessingPatternBuilder;
import appeng.crafting.simulation.helpers.SimulationEnv;
import appeng.util.BootstrapMinecraft;
import appeng.util.item.AEItemStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

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

		IAEItemStack bucket = AEItemStack.fromItemStack(new ItemStack(Items.BUCKET));
		IAEItemStack waterBucket = AEItemStack.fromItemStack(new ItemStack(Items.WATER_BUCKET));
		IAEItemStack dirt = AEItemStack.fromItemStack(new ItemStack(Items.DIRT));
		IAEItemStack grass = AEItemStack.fromItemStack(new ItemStack(Items.GRASS));
		// We use diamonds instead of water because fluid stacks are not yet supported.
		IAEItemStack water1mb = AEItemStack.fromItemStack(new ItemStack(Items.DIAMOND));
		IAEItemStack water1000mb = water1mb.copyWithStackSize(1000);

		// 1 dirt + 2 water buckets -> 1 grass.
		var grassPattern = env.addPattern(new ProcessingPatternBuilder(grass)
				.addStrictInput(1, dirt)
				.addStrictInput(2, waterBucket)
				.build()
		);
		// 1 empty bucket + 1000 water -> 1 water bucket.
		var waterBucketPattern = env.addPattern(new ProcessingPatternBuilder(waterBucket)
				.addStrictInput(1, bucket)
				.addStrictInput(1, water1000mb)
				.build()
		);

		// Dirt, water and empty buckets are infinite.
		env.setEmitable(dirt);
		env.setEmitable(bucket);
		env.setEmitable(water1mb);

		// Let's add 1 stored water bucket.
		env.addStoredItem(waterBucket);

		// Crafting should use the water bucket from the network, and craft 3.
		var plan = env.doCrafting(grass.copyWithStackSize(2));

		assertThat(plan.simulation()).isFalse();
		assertThat(plan.missingItems()).isEmpty();

		assertThat(plan.patternTimes()).isEqualTo(Map.of(grassPattern, 2L, waterBucketPattern, 3L));

		assertStackSize(plan.emittedItems(), dirt, 2);
		assertStackSize(plan.emittedItems(), bucket, 3);
		assertStackSize(plan.emittedItems(), water1mb, 3000);

		assertStackSize(plan.usedItems(), waterBucket, 1);
	}

	private static <T extends IAEStack<T>> void assertStackSize(IItemList<T> list, T what, long count) {
		assertThat(list.findPrecise(what).getStackSize()).isEqualTo(count);
	}
}
