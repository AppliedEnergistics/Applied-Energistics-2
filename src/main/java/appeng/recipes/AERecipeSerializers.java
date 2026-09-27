package appeng.recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import appeng.core.AppEng;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.game.AddItemUpgradeRecipe;
import appeng.recipes.game.CraftingUnitTransformRecipe;
import appeng.recipes.game.FacadeRecipe;
import appeng.recipes.game.RemoveItemUpgradeRecipe;
import appeng.recipes.game.StorageCellDisassemblyRecipe;
import appeng.recipes.game.StorageCellUpgradeRecipe;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.mattercannon.MatterCannonAmmo;
import appeng.recipes.quartzcutting.QuartzCuttingRecipe;
import appeng.recipes.transform.TransformRecipe;

public final class AERecipeSerializers {
    private AERecipeSerializers() {
    }

    public static final DeferredRegister<RecipeSerializer<?>> DR = DeferredRegister
            .create(Registries.RECIPE_SERIALIZER, AppEng.MOD_ID);

    static {
        register("inscriber", InscriberRecipe.SERIALIZER);
        register("facade", FacadeRecipe.SERIALIZER);
        register("entropy", EntropyRecipe.SERIALIZER);
        register("matter_cannon", MatterCannonAmmo.SERIALIZER);
        register("transform", TransformRecipe.SERIALIZER);
        register("charger", ChargerRecipe.SERIALIZER);
        register("storage_cell_upgrade", StorageCellUpgradeRecipe.SERIALIZER);
        register("add_item_upgrade", AddItemUpgradeRecipe.SERIALIZER);
        register("remove_item_upgrade", RemoveItemUpgradeRecipe.SERIALIZER);
        register("quartz_cutting", QuartzCuttingRecipe.SERIALIZER);
        register("crafting_unit_transform", CraftingUnitTransformRecipe.SERIALIZER);
        register("storage_cell_disassembly", StorageCellDisassemblyRecipe.SERIALIZER);
    }

    private static void register(String id, RecipeSerializer<?> serializer) {
        DR.register(id, () -> serializer);
    }
}
