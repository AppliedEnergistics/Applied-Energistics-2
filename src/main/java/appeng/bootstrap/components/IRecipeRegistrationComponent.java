package appeng.bootstrap.components;


import appeng.bootstrap.IBootstrapComponent;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;


@FunctionalInterface
public interface IRecipeRegistrationComponent extends IBootstrapComponent {
    void recipeRegistration(Side side, IForgeRegistry<IRecipe> recipeRegistry);
}
