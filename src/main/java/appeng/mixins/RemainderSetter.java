package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.item.Item;

@Mixin(Item.class)
public interface RemainderSetter {
    @Accessor("recipeRemainder")
    void setRecipeRemainder(Item item);
}
