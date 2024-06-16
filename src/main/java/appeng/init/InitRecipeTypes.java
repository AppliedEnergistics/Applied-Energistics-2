package appeng.init;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public class InitRecipeTypes {
    private record ToRegister(RecipeType<?> recipeType, ResourceLocation id) {
    }

    private static final List<ToRegister> toRegister = new ArrayList<>();

    public static <T extends Recipe<?>> RecipeType<T> register(String id) {
        RecipeType<T> type = RecipeType.simple(ResourceLocation.parse(id));
        toRegister.add(new ToRegister(type, ResourceLocation.parse(id)));
        return type;
    }

    public static void init(Registry<RecipeType<?>> registry) {
        for (var toRegister : toRegister) {
            Registry.register(registry, toRegister.id, toRegister.recipeType);
        }
    }

}
