package appeng.datagen.providers.recipes;

import net.minecraft.advancements.Advancement;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.recipes.handlers.ChargerRecipeBuilder;

public class ChargerRecipes extends AE2RecipeProvider {
    public ChargerRecipes(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
    }

    @Override
    public void buildRecipes() {
        ChargerRecipeBuilder.charge(output,
                AppEng.makeId("charger/charged_certus_quartz_crystal"),
                AEItems.CERTUS_QUARTZ_CRYSTAL, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED);
        ChargerRecipeBuilder.charge(output,
                AppEng.makeId("charger/meteorite_compass"),
                Items.COMPASS, AEItems.METEORITE_COMPASS);
        ChargerRecipeBuilder.charge(output,
                AppEng.makeId("charger/guide"),
                Items.BOOK, AEItems.GUIDE);
    }

}
