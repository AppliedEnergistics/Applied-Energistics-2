package appeng.datagen.providers.recipes;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;

public class SmeltingRecipes extends AE2RecipeProvider {

    // This is from the default recipe serializer for smelting
    private static final int DEFAULT_SMELTING_TIME = 200;

    public SmeltingRecipes(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(AEItems.FLOUR), Items.BREAD, 0, DEFAULT_SMELTING_TIME)
                .unlockedBy("has_flour", has(AEItems.FLOUR))
                .save(consumer, AppEng.makeId("smelting/bread"));

        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ConventionTags.GOLD_DUST), Items.GOLD_INGOT, 0, DEFAULT_SMELTING_TIME)
                .unlockedBy("has_gold_dust", has(AEItems.GOLD_DUST))
                .save(consumer, AppEng.makeId("smelting/gold_ingot"));

        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ConventionTags.IRON_DUST), Items.IRON_INGOT, 0, DEFAULT_SMELTING_TIME)
                .unlockedBy("has_iron_dust", has(AEItems.IRON_DUST))
                .save(consumer, AppEng.makeId("smelting/iron_ingot"));

        SimpleCookingRecipeBuilder.smelting(Ingredient.merge(List.of(
                Ingredient.of(ConventionTags.NETHER_QUARTZ_DUST),
                Ingredient.of(ConventionTags.CERTUS_QUARTZ_DUST))), AEItems.SILICON, 0, DEFAULT_SMELTING_TIME)
                .unlockedBy("has_certus_quartz_dust", has(ConventionTags.CERTUS_QUARTZ_DUST))
                .save(consumer, AppEng.makeId("smelting/silicon"));

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(AEBlocks.SKY_STONE_BLOCK), AEBlocks.SMOOTH_SKY_STONE_BLOCK,
                0, DEFAULT_SMELTING_TIME)
                .unlockedBy("has_sky_stone_block", has(AEBlocks.SKY_STONE_BLOCK))
                .save(consumer, AppEng.makeId("smelting/smooth_sky_stone_block"));

    }
}
