package appeng.mixins;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.class)
public interface RemainderSetter {
    @Accessor("recipeRemainder")
    void setRecipeRemainder(Item item);
}
