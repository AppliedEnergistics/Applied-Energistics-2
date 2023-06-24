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

import java.util.ArrayList;
import java.util.List;

public class InitRecipeTypes {
    private record ToRegister(RecipeType<?> recipeType, ResourceLocation id) {
    }

    private static final List<ToRegister> toRegister = new ArrayList<>();

    public static <T extends Recipe<?>> RecipeType<T> register(String id) {
        RecipeType<T> type = RecipeType.simple(new ResourceLocation(id));
        toRegister.add(new ToRegister(type, new ResourceLocation(id)));
        return type;
    }

    public static void init(IForgeRegistry<RecipeType<?>> registry) {
        for (var toRegister : toRegister) {
            registry.register(toRegister.id, toRegister.recipeType);
        }
    }

}
