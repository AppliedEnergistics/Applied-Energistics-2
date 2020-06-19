package appeng.core;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class AERecipeType<T extends IRecipe<?>> implements IRecipeType<T> {
    private final String id;

    public AERecipeType(ResourceLocation registryName) {
        this.id = registryName.toString();
    }

    @Override
    public String toString() {
        return id;
    }
}
