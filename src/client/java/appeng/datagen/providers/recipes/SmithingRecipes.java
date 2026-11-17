package appeng.datagen.providers.recipes;

import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import appeng.core.ConventionTags;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;

public class SmithingRecipes extends AE2RecipeProvider {
    public SmithingRecipes(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
    }

    @Override
    public void buildRecipes() {
        fluixSmithing(output, ConventionTags.QUARTZ_AXE, AEItems.FLUIX_AXE);
        fluixSmithing(output, ConventionTags.QUARTZ_HOE, AEItems.FLUIX_HOE);
        fluixSmithing(output, ConventionTags.QUARTZ_PICK, AEItems.FLUIX_PICK);
        fluixSmithing(output, ConventionTags.QUARTZ_SHOVEL, AEItems.FLUIX_SHOVEL);
        fluixSmithing(output, ConventionTags.QUARTZ_SWORD, AEItems.FLUIX_SWORD);
    }

    private void fluixSmithing(RecipeOutput output, TagKey<Item> quartzTool,
            ItemDefinition<?> fluixTool) {
        SmithingTransformRecipeBuilder
                .smithing(Ingredient.of(AEItems.FLUIX_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(items.getOrThrow(quartzTool)),
                        Ingredient.of(AEBlocks.FLUIX_BLOCK), RecipeCategory.MISC, fluixTool.asItem())
                .unlocks("has_crystals/fluix", has(ConventionTags.ALL_FLUIX))
                .save(output, makeId("tools/" + getItemName(fluixTool)));
    }
}
