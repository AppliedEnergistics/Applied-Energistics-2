package appeng.client.integrations.jei;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.client.api.integrations.jei.IngredientConverter;

public class ItemIngredientConverter implements IngredientConverter<ItemStack> {
    @Override
    public IIngredientType<ItemStack> getIngredientType() {
        return VanillaTypes.ITEM_STACK;
    }

    @Nullable
    @Override
    public ItemStack getIngredientFromStack(GenericStack stack) {
        if (stack.what() instanceof AEItemKey itemKey) {
            return itemKey.toStack(Math.max(1, Ints.saturatedCast(stack.amount())));
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public GenericStack getStackFromIngredient(ItemStack ingredient) {
        return GenericStack.fromItemStack(ingredient);
    }
}
