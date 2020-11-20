package appeng.recipes.handlers;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.collection.DefaultedList;

import appeng.items.tools.quartz.QuartzCuttingKnifeItem;
import appeng.mixins.ShapelessRecipeMixin;

/**
 * An extended version of ShapelessRecipe to support damaging the QuartzKnife whenever it is used to craft something.
 */
public class QuartzKnifeRecipe extends ShapelessRecipe {

    public QuartzKnifeRecipe(ShapelessRecipe original) {
        super(original.getId(), ((ShapelessRecipeMixin) original).getGroup(), original.getOutput(),
                original.getPreviewInputs());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return QuartzKnifeRecipeSerializer.INSTANCE;
    }

    @Override
    public DefaultedList<ItemStack> getRemainingStacks(CraftingInventory inventory) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);

        for (int i = 0; i < defaultedList.size(); ++i) {
            ItemStack stack = inventory.getStack(i);
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
            } else if (item.hasRecipeRemainder()) {
                defaultedList.set(i, new ItemStack(item.getRecipeRemainder()));
            }
        }

        return defaultedList;
    }

}
