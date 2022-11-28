package appeng.datagen.providers.recipes;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.recipes.handlers.ChargerRecipeBuilder;

public class ChargerRecipes extends AE2RecipeProvider {
    public ChargerRecipes(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildAE2CraftingRecipes(Consumer<FinishedRecipe> consumer) {
        ChargerRecipeBuilder.charge(consumer,
                AppEng.makeId("charger/charged_certus_quartz_crystal"),
                AEItems.CERTUS_QUARTZ_CRYSTAL,
                AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem());
        ChargerRecipeBuilder.charge(consumer,
                AppEng.makeId("charger/meteorite_compass"),
                Items.COMPASS,
                AEItems.METEORITE_COMPASS.asItem());
    }
}
