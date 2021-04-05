package appeng.util.item;

import java.util.IdentityHashMap;
import java.util.Map;

import com.google.common.testing.EqualsTester;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
        Item TEST_ITEM = Items.NAME_TAG;

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