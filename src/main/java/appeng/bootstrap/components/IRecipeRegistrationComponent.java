
package appeng.bootstrap.components;

import net.fabricmc.api.EnvType;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.bootstrap.IBootstrapComponent;

@FunctionalInterface
public interface IRecipeRegistrationComponent extends IBootstrapComponent {
    void recipeRegistration(EnvType dist, IForgeRegistry<IRecipeSerializer<?>> recipeRegistry);
}
