package appeng.recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;

import appeng.core.AppEng;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.mattercannon.MatterCannonAmmo;
import appeng.recipes.transform.TransformRecipe;

public final class AERecipeTypes {
    private AERecipeTypes() {
    }

    public static final DeferredRegister<RecipeType<?>> DR = DeferredRegister
            .create(Registries.RECIPE_TYPE, AppEng.MOD_ID);

    public static final RecipeType<TransformRecipe> TRANSFORM = register("transform");
    public static final RecipeType<EntropyRecipe> ENTROPY = register("entropy");
    public static final RecipeType<InscriberRecipe> INSCRIBER = register("inscriber");
    public static final RecipeType<ChargerRecipe> CHARGER = register("charger");
    public static final RecipeType<MatterCannonAmmo> MATTER_CANNON_AMMO = register("matter_cannon");

    private static <T extends Recipe<?>> RecipeType<T> register(String id) {
        RecipeType<T> type = RecipeType.simple(AppEng.makeId(id));
        DR.register(id, () -> type);
        return type;
    }
}
