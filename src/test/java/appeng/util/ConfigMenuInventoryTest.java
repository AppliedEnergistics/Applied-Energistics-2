package appeng.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.annotation.Nullable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import appeng.api.storage.AEKeySpace;
import appeng.api.storage.GenericStack;
import appeng.api.storage.data.AEFluidKey;
import appeng.api.storage.data.AEItemKey;

@BootstrapMinecraft
class ConfigMenuInventoryTest {

    private static final GenericStack STICK = new GenericStack(AEItemKey.of(Items.STICK), 1);
    private static final GenericStack STONE = new GenericStack(AEItemKey.of(Items.STONE), 1);
    private static final GenericStack ZERO_STICK = new GenericStack(AEItemKey.of(Items.STICK), 0);
    private static final GenericStack WATER = new GenericStack(AEFluidKey.of(Fluids.WATER), AEFluidKey.AMOUNT_BUCKET);
    private static final GenericStack WATER_BUCKET = new GenericStack(AEItemKey.of(Items.WATER_BUCKET), 1);

    @TestFactory
    @DisplayName("Test item based config inventory")
    Iterable<DynamicTest> itemTests() {
        return List.of(
                itemTest("Insert empty changes nothing", ItemStack.EMPTY, null, null),
                itemTest("Insert empty clears existing filter", ItemStack.EMPTY, null, STICK),
                itemTest("Insert stick on empty sets filter", new ItemStack(Items.STICK), STICK, null),
                itemTest("Insert stone on stick changes filter", new ItemStack(Items.STONE), STONE, STICK),
                itemTest("Water bucket wont be converted into fluid", new ItemStack(Items.WATER_BUCKET), WATER_BUCKET,
                        null),
                itemTest("Wrapped item will be unwrapped", GenericStack.wrapInItemStack(STICK), STICK, null),
                itemTest("Wrapped fluid will be rejected", GenericStack.wrapInItemStack(WATER), STICK, STICK));
    }

    @TestFactory
    @DisplayName("Test fluid based config inventory")
    Iterable<DynamicTest> fluidTests() {
        return List.of(
                fluidTest("Insert empty changes nothing", ItemStack.EMPTY, null, null),
                fluidTest("Insert empty clears existing filter", ItemStack.EMPTY, null, WATER),
                fluidTest("Stick gets rejected", new ItemStack(Items.STICK), WATER, WATER),
                fluidTest("Water bucket is converted into fluid", new ItemStack(Items.WATER_BUCKET), WATER, null),
                fluidTest("Wrapped item will be rejected", GenericStack.wrapInItemStack(STICK), WATER, WATER),
                fluidTest("Wrapped fluid will be unwrapped", GenericStack.wrapInItemStack(WATER), WATER, null));
    }

    @Test
    void testFluidConfigReadsAsWrappedStackOfCount1() {
        var inv = ConfigInventory.configTypes(AEFluidKey.filter(), 1, null);
        inv.setStack(0, WATER);
        var wrappedStack = inv.createMenuWrapper().getStackInSlot(0);
        assertEquals(1, wrappedStack.getCount());
        var unwrapped = GenericStack.unwrapItemStack(wrappedStack);
        // Types config sets amounts to 0
        assertEquals(new GenericStack(WATER.what(), 0), unwrapped);
    }

    /**
     * For more compatibility, item type configs will read as ItemStacks with amount 1, and not as wrapped stacks.
     */
    @Test
    void testItemTypeConfigReadsAsItemWithAmount1() {
        var inv = ConfigInventory.configTypes(AEItemKey.filter(), 1, null);
        inv.setStack(0, ZERO_STICK);
        var fakeStack = inv.createMenuWrapper().getStackInSlot(0);
        assertEquals(Items.STICK, fakeStack.getItem());
        assertEquals(1, fakeStack.getCount());
    }

    // Test when an item-based machine's config inventory is interacted with
    private DynamicTest itemTest(String displayName, ItemStack inserted, @Nullable GenericStack expectedStack,
            @Nullable GenericStack initialStack) {
        return test(displayName, AEKeySpace.items(), inserted, expectedStack, initialStack);
    }

    private DynamicTest fluidTest(String displayName, ItemStack inserted, @Nullable GenericStack expectedStack,
            @Nullable GenericStack initialStack) {
        return test(displayName, AEKeySpace.fluids(), inserted, expectedStack, initialStack);
    }

    private DynamicTest test(String displayName, AEKeySpace channel, ItemStack inserted,
            @Nullable GenericStack expectedStack, @Nullable GenericStack initialStack) {
        return DynamicTest.dynamicTest(
                displayName,
                () -> {
                    var inv = ConfigInventory.configStacks(channel.filter(), 64, null);
                    if (initialStack != null) {
                        inv.setStack(0, initialStack);
                    }

                    inv.createMenuWrapper().setItemDirect(0, inserted);
                    var actualStack = inv.getStack(0);
                    assertEquals(expectedStack, actualStack);
                });
    }

}
