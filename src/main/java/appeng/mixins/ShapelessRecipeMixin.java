package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.recipe.ShapelessRecipe;

@Mixin(ShapelessRecipe.class)
public interface ShapelessRecipeMixin {

    @Accessor("group")
    String getGroup();

}
