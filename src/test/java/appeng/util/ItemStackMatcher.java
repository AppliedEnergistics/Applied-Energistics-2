package appeng.util;

import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import net.minecraft.world.item.ItemStack;

public class ItemStackMatcher implements ArgumentMatcher<ItemStack> {
    private final ItemStack stack;
    private final boolean matchCount;

    private ItemStackMatcher(ItemStack stack, boolean matchCount) {
        this.stack = stack;
        this.matchCount = matchCount;
    }

    public static ItemStack isSameItemTags(ItemStack stack) {
        return Mockito.argThat(new ItemStackMatcher(stack, false));
    }

    public static ItemStack isSameItemTagsCount(ItemStack stack) {
        return Mockito.argThat(new ItemStackMatcher(stack, true));
    }

    @Override
    public boolean matches(ItemStack argument) {
        return ItemStack.isSameItemSameComponents(stack, argument)
                && !matchCount || stack.getCount() == argument.getCount();
    }

    @Override
    public String toString() {
        var result = new StringBuilder();
        if (matchCount) {
            result.append(stack.getCount()).append(' ');
        }
        result.append(stack.getItem());
        if (!stack.getComponentsPatch().isEmpty()) {
            result.append(" [has components]");
        }
        return result.toString();
    }
}
