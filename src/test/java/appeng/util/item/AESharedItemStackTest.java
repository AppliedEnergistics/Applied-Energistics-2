package appeng.util.item;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.IdentityHashMap;
import java.util.Map;

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

    private final TestItemWithCaps TEST_ITEM = new TestItemWithCaps();

    /**
     * Creates a bunch of item stacks that should all be considered not-equal to one another.
     */
    @Test
    void testEquals() {
        // Test stack -> Name for debuggin the tests
        Map<AESharedItemStack, String> stacks = new IdentityHashMap<>();

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

        // Start by sanity checking compareTo & equals
        for (AESharedItemStack stack : stacks.keySet()) {
            for (AESharedItemStack otherStack : stacks.keySet()) {
                String stackName = stacks.get(stack);
                String otherStackName = stacks.get(otherStack);

                if (stack == otherStack) {
                    assertThat(stack).as("%s.equals(%s)", stackName, stackName)
                            .isEqualTo(otherStack);
                } else {
                    assertThat(stack)
                            .as("!%s.equals(%s)", stackName, otherStackName)
                            .isNotEqualTo(otherStack);
                }
            }
        }
    }

}