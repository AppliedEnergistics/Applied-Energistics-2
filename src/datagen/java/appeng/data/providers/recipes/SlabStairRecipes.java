package appeng.data.providers.recipes;

import static net.minecraft.data.server.RecipesProvider.*;

import java.nio.file.Path;
import java.util.function.Consumer;

import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.data.DataCache;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonFactory;
import net.minecraft.data.server.recipe.SingleItemRecipeJsonFactory;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;

import appeng.api.definitions.IBlockDefinition;
import appeng.core.AppEng;
import appeng.data.providers.IAE2DataProvider;

public class SlabStairRecipes implements IAE2DataProvider {

    IBlockDefinition[][] blocks = { { BLOCKS.skyStoneBlock(), BLOCKS.skyStoneSlab(), BLOCKS.skyStoneStairs() },
            { BLOCKS.smoothSkyStoneBlock(), BLOCKS.smoothSkyStoneSlab(), BLOCKS.smoothSkyStoneStairs() },
            { BLOCKS.skyStoneBrick(), BLOCKS.skyStoneBrickSlab(), BLOCKS.skyStoneBrickStairs() },
            { BLOCKS.skyStoneSmallBrick(), BLOCKS.skyStoneSmallBrickSlab(), BLOCKS.skyStoneSmallBrickStairs() },
            { BLOCKS.fluixBlock(), BLOCKS.fluixSlab(), BLOCKS.fluixStairs() },
            { BLOCKS.quartzBlock(), BLOCKS.quartzSlab(), BLOCKS.quartzStairs() },
            { BLOCKS.chiseledQuartzBlock(), BLOCKS.chiseledQuartzSlab(), BLOCKS.chiseledQuartzStairs() },
            { BLOCKS.quartzPillar(), BLOCKS.quartzPillarSlab(), BLOCKS.quartzPillarStairs() }, };

    private final Path outputPath;

    private final Consumer<RecipeJsonProvider> consumer;

    private DataCache cache;

    public SlabStairRecipes(Path outputPath) {
        this.outputPath = outputPath;
        this.consumer = this::provideRecipe;
    }

    public void run(DataCache cache) {
        this.cache = cache;
        for (IBlockDefinition[] block : blocks) {
            slabRecipe(block[0], block[1]);
            stairRecipe(block[0], block[2]);
        }
    }

    private void slabRecipe(IBlockDefinition block, IBlockDefinition slabs) {
        Block inputBlock = block.block();
        Block outputBlock = slabs.block();

        ShapedRecipeJsonFactory.create(slabs.block(), 6).pattern("###").input('#', inputBlock)
                .criterion(criterionName(block), conditionsFromItem(inputBlock))
                .offerTo(consumer, new Identifier(AppEng.MOD_ID, "shaped/slabs/" + block.identifier()));

        SingleItemRecipeJsonFactory.create(Ingredient.ofItems(inputBlock), outputBlock, 2)
                .create(criterionName(block), conditionsFromItem(inputBlock))
                .offerTo(consumer, new Identifier(AppEng.MOD_ID, "block_cutter/slabs/" + slabs.identifier()));
    }

    private void stairRecipe(IBlockDefinition block, IBlockDefinition stairs) {
        Block inputBlock = block.block();
        Block outputBlock = stairs.block();

        ShapedRecipeJsonFactory.create(outputBlock, 4).pattern("#  ").pattern("## ").pattern("###")
                .input('#', inputBlock).criterion(criterionName(block), conditionsFromItem(inputBlock))
                .offerTo(consumer, new Identifier(AppEng.MOD_ID, "shaped/stairs/" + block.identifier()));

        SingleItemRecipeJsonFactory.create(Ingredient.ofItems(inputBlock), outputBlock)
                .create(criterionName(block), conditionsFromItem(inputBlock))
                .offerTo(consumer, new Identifier(AppEng.MOD_ID, "block_cutter/stairs/" + stairs.identifier()));

    }

    private void provideRecipe(RecipeJsonProvider recipeJsonProvider) {
        saveRecipe(cache, recipeJsonProvider.toJson(),
                outputPath.resolve("data/" + recipeJsonProvider.getRecipeId().getNamespace() + "/recipes/"
                        + recipeJsonProvider.getRecipeId().getPath() + ".json"));
        JsonObject jsonObject = recipeJsonProvider.toAdvancementJson();
        if (jsonObject != null) {
            saveRecipeAdvancement(cache, jsonObject,
                    outputPath.resolve("data/" + recipeJsonProvider.getRecipeId().getNamespace() + "/advancements/"
                            + recipeJsonProvider.getAdvancementId().getPath() + ".json"));
        }
    }

    private String criterionName(IBlockDefinition block) {
        return String.format("has_%s", block.identifier());
    }

    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Slabs and Stairs";
    }

}
