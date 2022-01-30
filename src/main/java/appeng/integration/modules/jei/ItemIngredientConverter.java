package appeng.integration.modules.jei;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;

import appeng.api.integrations.rei.IngredientConverter;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;

public class ItemIngredientConverter implements IngredientConverter<ItemStack> {
    @Override
    public EntryType<ItemStack> getIngredientType() {
        return VanillaEntryTypes.ITEM;
    }

    @Nullable
    @Override
    public EntryStack<ItemStack> getIngredientFromStack(GenericStack stack) {
        if (stack.what() instanceof AEItemKey itemKey) {
            return EntryStack.of(getIngredientType(), itemKey.toStack(
                    Math.max(1, Ints.saturatedCast(stack.amount()))));
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public GenericStack getStackFromIngredient(EntryStack<ItemStack> ingredient) {
        if (ingredient.getType() == getIngredientType()) {
            ItemStack itemStack = ingredient.castValue();
            var itemKey = AEItemKey.of(itemStack);
            if (itemKey != null) {
                return new GenericStack(itemKey, itemStack.getCount());
            }
        }
        return null;
    }
}
