package appeng.recipes;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.items.parts.FacadeItem;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.entropy.EntropyRecipeSerializer;
import appeng.recipes.game.AddItemUpgradeRecipe;
import appeng.recipes.game.AddItemUpgradeRecipeSerializer;
import appeng.recipes.game.FacadeRecipe;
import appeng.recipes.game.RemoveItemUpgradeRecipe;
import appeng.recipes.game.RemoveItemUpgradeRecipeSerializer;
import appeng.recipes.game.StorageCellUpgradeRecipe;
import appeng.recipes.game.StorageCellUpgradeRecipeSerializer;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.ChargerRecipeSerializer;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.handlers.InscriberRecipeSerializer;
import appeng.recipes.mattercannon.MatterCannonAmmo;
import appeng.recipes.mattercannon.MatterCannonAmmoSerializer;
import appeng.recipes.transform.TransformRecipe;
import appeng.recipes.transform.TransformRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AERecipeSerializers {
    private AERecipeSerializers() {
    }

    public static final DeferredRegister<RecipeSerializer<?>> DR = DeferredRegister
            .create(Registries.RECIPE_SERIALIZER, AppEng.MOD_ID);

    static {
        FacadeItem facadeItem = AEItems.FACADE.get();
        register("inscriber", InscriberRecipeSerializer.INSTANCE);
        register("facade", FacadeRecipe.SERIALIZER);
        register("entropy", EntropyRecipeSerializer.INSTANCE);
        register("matter_cannon", MatterCannonAmmoSerializer.INSTANCE);
        register("transform", TransformRecipeSerializer.INSTANCE);
        register("charger", ChargerRecipeSerializer.INSTANCE);
        register("storage_cell_upgrade", StorageCellUpgradeRecipeSerializer.INSTANCE);
        register("add_item_upgrade", AddItemUpgradeRecipeSerializer.INSTANCE);
        register("remove_item_upgrade", RemoveItemUpgradeRecipeSerializer.INSTANCE);
    }

    private static void register(String id, RecipeSerializer<?> serializer) {
        DR.register(id, () -> serializer);
    }
}
