package appeng.util.item;

import java.util.IdentityHashMap;
import java.util.Map;

import com.google.common.testing.EqualsTester;
import net.minecraft.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.registry.Bootstrap;
import net.minecraft.util.text.StringTextComponent;

class AESharedItemStackTest {

    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
    }

    // Test stack -> Name for debugging the tests
    final Map<AESharedItemStack, String> stacks = new IdentityHashMap<>();

    AESharedItemStackTest() {
        TestItemWithCaps TEST_ITEM = new TestItemWithCaps();

        ItemStack nameTag1 = new ItemStack(TEST_ITEM);
        stacks.put(new AESharedItemStack(nameTag1), "no-nbt");

        // NBT
        ItemStack nameTag2 = new ItemStack(TEST_ITEM);
        nameTag2.setDisplayName(new StringTextComponent("Hello World"));
        stacks.put(new AESharedItemStack(nameTag2), "nbt1");

        // Different NBT
        ItemStack nameTag3 = new ItemStack(TEST_ITEM);
        nameTag3.setDisplayName(new StringTextComponent("ABCDEFGH"));
        stacks.put(new AESharedItemStack(nameTag3), "nbt2");

        // NBT + Cap
        CompoundNBT capNbt = new CompoundNBT();
        capNbt.putInt("Parent", 1);
        ItemStack nameTag4 = new ItemStack(TEST_ITEM, 1, capNbt);
        nameTag4.setDisplayName(new StringTextComponent("Hello World"));
        stacks.put(new AESharedItemStack(nameTag4), "nbt1+cap1");

        // NBT + Different Cap
        CompoundNBT capNbt2 = new CompoundNBT();
        capNbt2.putInt("Parent", 123);
        ItemStack nameTag5 = new ItemStack(TEST_ITEM, 1, capNbt2);
        nameTag5.setDisplayName(new StringTextComponent("Hello World"));
        stacks.put(new AESharedItemStack(nameTag5), "nbt1+cap2");
    }

    /**
     * Tests equality between shared item stacks.
     */
    @Test
    void testEquals() {
        EqualsTester tester = new EqualsTester();
        for (AESharedItemStack stack : stacks.keySet()) {
            // Add the stack, and a pristine copy of the stack
            tester.addEqualityGroup(stack, new AESharedItemStack(stack.getDefinition().copy()));
        }

        // Test that using the same item stack instance makes two separate shared stacks equal
        ItemStack itemStack = new ItemStack(Items.CRAFTING_TABLE);
        tester.addEqualityGroup(
                new AESharedItemStack(itemStack),
                new AESharedItemStack(itemStack));

        tester.testEquals();
    }

}