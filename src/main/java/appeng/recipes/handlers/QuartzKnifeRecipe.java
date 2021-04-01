package appeng.recipes.handlers;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.NonNullList;
import appeng.items.tools.quartz.QuartzCuttingKnifeItem;
import appeng.mixins.ShapelessRecipeMixin;

/**
 * An extended version of ShapelessRecipe to support damaging the QuartzKnife whenever it is used to craft something.
 */
public class QuartzKnifeRecipe extends ShapelessRecipe {

    public QuartzKnifeRecipe(ShapelessRecipe original) {
        super(original.getId(), ((ShapelessRecipeMixin) original).getGroup(), original.getRecipeOutput(),
                original.getIngredients());
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return QuartzKnifeRecipeSerializer.INSTANCE;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inventory) {
        NonNullList<ItemStack> defaultedList = NonNullList.withSize(inventory.getSizeInventory(), ItemStack.EMPTY);

        for (int i = 0; i < defaultedList.size(); ++i) {
            ItemStack stack = inventory.getStackInSlot(i);
            Item item = stack.getItem();
            if (item instanceof QuartzCuttingKnifeItem) {
                // If it still has durability left, put a more damaged one back, otherwise
                // consume
                int newDamage = stack.getDamage() + 1;
                if (newDamage < stack.getMaxDamage()) {
                    stack = stack.copy();
                    stack.setDamage(newDamage);
                    defaultedList.set(i, stack);
                }
            } else if (item.hasContainerItem()) {
                defaultedList.set(i, new ItemStack(item.getContainerItem()));
            }
        }

        return defaultedList;
    }

}
