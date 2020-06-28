package appeng.core;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;

public class AERecipeType<T extends Recipe<?>> implements RecipeType<T> {
    private final String id;

    public AERecipeType(Identifier registryName) {
        this.id = registryName.toString();
    }

    @Override
    public String toString() {
        return id;
    }
}
