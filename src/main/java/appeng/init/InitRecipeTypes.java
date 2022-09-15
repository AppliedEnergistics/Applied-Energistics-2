package appeng.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.mattercannon.MatterCannonAmmo;
import appeng.recipes.transform.TransformRecipe;

public class InitRecipeTypes {

    public static void init(IForgeRegistry<RecipeType<?>> registry) {
        ChargerRecipe.TYPE = register(registry, ChargerRecipe.TYPE_ID);
        InscriberRecipe.TYPE = register(registry, InscriberRecipe.TYPE_ID);
        MatterCannonAmmo.TYPE = register(registry, MatterCannonAmmo.TYPE_ID);
        EntropyRecipe.TYPE = register(registry, EntropyRecipe.TYPE_ID);
        TransformRecipe.TYPE = register(registry, TransformRecipe.TYPE_ID);
    }

    private static <T extends Recipe<?>> RecipeType<T> register(IForgeRegistry<RecipeType<?>> registry,
            ResourceLocation id) {
        var type = new RecipeType<T>() {
            @Override
            public String toString() {
                return id.toString();
            }
        };
        registry.register(id, type);
        return type;
    }

}
