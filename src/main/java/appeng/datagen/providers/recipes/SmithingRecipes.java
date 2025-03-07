package appeng.datagen.providers.recipes;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.datagen.providers.tags.ConventionTags;

public class SmithingRecipes extends AE2RecipeProvider {
    public SmithingRecipes(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
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
                .smithing(Ingredient.of(AEItems.FLUIX_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(items.getOrThrow(quartzTool)),
                        Ingredient.of(AEBlocks.FLUIX_BLOCK), RecipeCategory.MISC, fluixTool.asItem())
                .unlocks("has_crystals/fluix", has(ConventionTags.ALL_FLUIX))
                .save(output, makeId("tools/" + getItemName(fluixTool)));
    }
}
