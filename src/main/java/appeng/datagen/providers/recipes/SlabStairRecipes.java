package appeng.datagen.providers.recipes;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.SingleItemRecipeBuilder;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import appeng.api.definitions.IBlockDefinition;
import appeng.core.AppEng;
import appeng.datagen.providers.IAE2DataProvider;

public class SlabStairRecipes extends RecipeProvider implements IAE2DataProvider {

    IBlockDefinition[][] blocks = { { BLOCKS.skyStoneBlock(), BLOCKS.skyStoneSlab(), BLOCKS.skyStoneStairs() },
            { BLOCKS.smoothSkyStoneBlock(), BLOCKS.smoothSkyStoneSlab(), BLOCKS.smoothSkyStoneStairs() },
            { BLOCKS.skyStoneBrick(), BLOCKS.skyStoneBrickSlab(), BLOCKS.skyStoneBrickStairs() },
            { BLOCKS.skyStoneSmallBrick(), BLOCKS.skyStoneSmallBrickSlab(), BLOCKS.skyStoneSmallBrickStairs() },
            { BLOCKS.fluixBlock(), BLOCKS.fluixSlab(), BLOCKS.fluixStairs() },
            { BLOCKS.quartzBlock(), BLOCKS.quartzSlab(), BLOCKS.quartzStairs() },
            { BLOCKS.chiseledQuartzBlock(), BLOCKS.chiseledQuartzSlab(), BLOCKS.chiseledQuartzStairs() },
            { BLOCKS.quartzPillar(), BLOCKS.quartzPillarSlab(), BLOCKS.quartzPillarStairs() }, };

    public SlabStairRecipes(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    public void registerRecipes(@Nonnull Consumer<IFinishedRecipe> consumer) {
        for (IBlockDefinition[] block : blocks) {
            slabRecipe(consumer, block[0], block[1]);
            stairRecipe(consumer, block[0], block[2]);
        }
    }

    private void slabRecipe(Consumer<IFinishedRecipe> consumer, IBlockDefinition block, IBlockDefinition slabs) {
        Block inputBlock = block.block();
        Block outputBlock = slabs.block();

        ShapedRecipeBuilder.shapedRecipe(slabs.block(), 6).patternLine("###").key('#', inputBlock)
                .addCriterion(criterionName(block), hasItem(inputBlock))
                .build(consumer, new ResourceLocation(AppEng.MOD_ID, "shaped/slabs/" + block.identifier()));

        SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromItems(inputBlock), outputBlock, 2)
                .addCriterion(criterionName(block), hasItem(inputBlock))
                .build(consumer, new ResourceLocation(AppEng.MOD_ID, "block_cutter/slabs/" + slabs.identifier()));
    }

    private void stairRecipe(Consumer<IFinishedRecipe> consumer, IBlockDefinition block, IBlockDefinition stairs) {
        Block inputBlock = block.block();
        Block outputBlock = stairs.block();

        ShapedRecipeBuilder.shapedRecipe(outputBlock, 4).patternLine("#  ").patternLine("## ").patternLine("###")
                .key('#', inputBlock).addCriterion(criterionName(block), hasItem(inputBlock))
                .build(consumer, new ResourceLocation(AppEng.MOD_ID, "shaped/stairs/" + block.identifier()));

        SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromItems(inputBlock), outputBlock)
                .addCriterion(criterionName(block), hasItem(inputBlock))
                .build(consumer, new ResourceLocation(AppEng.MOD_ID, "block_cutter/stairs/" + stairs.identifier()));

    }

    private String criterionName(IBlockDefinition block) {
        return String.format("has_%s", block.identifier());
    }

    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Slabs and Stairs";
    }

}
