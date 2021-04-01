package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

@Mixin(Ingredient.class)
public interface IngredientAccessor {

    // Temporary fix for https://github.com/SpongePowered/Mixin/issues/430
    @Invoker("determineMatchingStacks")
    void appeng_cacheMatchingStacks();

    @Accessor
    ItemStack[] getMatchingStacks();

}
