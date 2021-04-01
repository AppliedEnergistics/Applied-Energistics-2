package appeng.mixins;

import net.minecraft.item.crafting.ShapelessRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShapelessRecipe.class)
public interface ShapelessRecipeMixin {

    @Accessor("group")
    String getGroup();

}
