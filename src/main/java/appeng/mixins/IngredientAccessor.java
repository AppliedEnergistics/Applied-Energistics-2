package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

@Mixin(Ingredient.class)
public interface IngredientAccessor {

    @Invoker
    void callCacheMatchingStacks();

    @Accessor
    ItemStack[] getMatchingStacks();

}
