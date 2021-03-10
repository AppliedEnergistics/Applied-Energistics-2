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
    public void buildShapelessRecipes(@Nonnull Consumer<IFinishedRecipe> consumer) {
        for (IBlockDefinition[] block : blocks) {
            slabRecipe(consumer, block[0], block[1]);
            stairRecipe(consumer, block[0], block[2]);
        }
    }

    private void slabRecipe(Consumer<IFinishedRecipe> consumer, IBlockDefinition block, IBlockDefinition slabs) {
        Block inputBlock = block.block();
        Block outputBlock = slabs.block();

        ShapedRecipeBuilder.shaped(slabs.block(), 6).pattern("###").define('#', inputBlock)
                .unlockedBy(criterionName(block), has(inputBlock))
                .save(consumer, new ResourceLocation(AppEng.MOD_ID, "shaped/slabs/" + block.identifier()));

        SingleItemRecipeBuilder.stonecutting(Ingredient.of(inputBlock), outputBlock, 2)
                .unlocks(criterionName(block), has(inputBlock))
                .save(consumer, new ResourceLocation(AppEng.MOD_ID, "block_cutter/slabs/" + slabs.identifier()));
    }

    private void stairRecipe(Consumer<IFinishedRecipe> consumer, IBlockDefinition block, IBlockDefinition stairs) {
        Block inputBlock = block.block();
        Block outputBlock = stairs.block();

        ShapedRecipeBuilder.shaped(outputBlock, 4).pattern("#  ").pattern("## ").pattern("###")
                .define('#', inputBlock).unlockedBy(criterionName(block), has(inputBlock))
                .save(consumer, new ResourceLocation(AppEng.MOD_ID, "shaped/stairs/" + block.identifier()));

        SingleItemRecipeBuilder.stonecutting(Ingredient.of(inputBlock), outputBlock)
                .unlocks(criterionName(block), has(inputBlock))
                .save(consumer, new ResourceLocation(AppEng.MOD_ID, "block_cutter/stairs/" + stairs.identifier()));

    }

    private String criterionName(IBlockDefinition block) {
        return String.format("has_%s", block.identifier());
    }

    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Slabs and Stairs";
    }

}
