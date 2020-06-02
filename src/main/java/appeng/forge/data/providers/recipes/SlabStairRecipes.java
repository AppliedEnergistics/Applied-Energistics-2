package appeng.forge.data.providers.recipes;


import appeng.api.definitions.IBlockDefinition;
import appeng.core.AppEng;
import appeng.forge.data.providers.IAE2DataProvider;
import appeng.recipes.conditions.FeaturesEnabled;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.data.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.ConditionalRecipe;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.function.Consumer;


public class SlabStairRecipes extends RecipeProvider implements IAE2DataProvider
{

	IBlockDefinition[][] blocks = {
			{ BLOCKS.skyStoneBlock(), BLOCKS.skyStoneSlab(), BLOCKS.skyStoneStairs() },
			{ BLOCKS.smoothSkyStoneBlock(), BLOCKS.smoothSkyStoneSlab(), BLOCKS.smoothSkyStoneStairs() },
			{ BLOCKS.skyStoneBrick(), BLOCKS.skyStoneBrickSlab(), BLOCKS.skyStoneBrickStairs() },
			{ BLOCKS.skyStoneSmallBrick(), BLOCKS.skyStoneSmallBrickSlab(), BLOCKS.skyStoneSmallBrickStairs() },
			{ BLOCKS.fluixBlock(), BLOCKS.fluixSlab(), BLOCKS.fluixStairs() },
			{ BLOCKS.quartzBlock(), BLOCKS.quartzSlab(), BLOCKS.quartzStairs() },
			{ BLOCKS.chiseledQuartzBlock(), BLOCKS.chiseledQuartzSlab(), BLOCKS.chiseledQuartzStairs() },
			{ BLOCKS.quartzPillar(), BLOCKS.quartzPillarSlab(), BLOCKS.quartzPillarStairs() },
	};

	public SlabStairRecipes( DataGenerator generatorIn )
	{
		super( generatorIn );
	}

	@Override
	public void registerRecipes( @Nonnull Consumer<IFinishedRecipe> consumer )
	{
		for( IBlockDefinition[] block : blocks )
		{
			slabRecipe( consumer, block[0], block[1] );
			stairRecipe( consumer, block[0], block[2] );
		}
	}

	private void slabRecipe( Consumer<IFinishedRecipe> consumer, IBlockDefinition block, IBlockDefinition slabs )
	{
		Block inputBlock = block.block();
		Block outputBlock = slabs.block();

		FeaturesEnabled condition = new FeaturesEnabled( Sets.union( block.features(), slabs.features() ) );
		ConditionalRecipe.builder()
				.addCondition( condition )
				.addRecipe(
						ShapedRecipeBuilder.shapedRecipe( slabs.block(), 6 )
								.patternLine( "###" )
								.key( '#', inputBlock )
								.addCriterion( criterionName( block ), hasItem( inputBlock ) )
								::build
				)
				.build( consumer, AppEng.MOD_ID, "slabs/" + block.identifier() );

		ConditionalRecipe.builder()
				.addCondition( condition )
				.addRecipe(
						c -> SingleItemRecipeBuilder.stonecuttingRecipe( Ingredient.fromItems( inputBlock ), outputBlock, 2 )
								.addCriterion( criterionName( block ), hasItem( inputBlock ) )
								.build( c, new ResourceLocation( AppEng.MOD_ID, slabs.identifier() ) )
				)
				.build( consumer, AppEng.MOD_ID, "slabs/block_cutter/" + block.identifier() );

	}

	private void stairRecipe( Consumer<IFinishedRecipe> consumer, IBlockDefinition block, IBlockDefinition stairs )
	{
		Block inputBlock = block.block();
		Block outputBlock = stairs.block();

		FeaturesEnabled condition = new FeaturesEnabled( Sets.union( block.features(), stairs.features() ) );
		ConditionalRecipe.builder()
				.addCondition( condition )
				.addRecipe(
						ShapedRecipeBuilder.shapedRecipe( outputBlock, 4 )
								.patternLine( "#  " )
								.patternLine( "## " )
								.patternLine( "###" )
								.key( '#', inputBlock )
								.addCriterion( criterionName( block ), hasItem( inputBlock ) )
								::build
				)
				.build( consumer, AppEng.MOD_ID, "stairs/" + block.identifier() );

		ConditionalRecipe.builder()
				.addCondition( condition )
				.addRecipe(
						c -> SingleItemRecipeBuilder.stonecuttingRecipe( Ingredient.fromItems( inputBlock ), outputBlock )
								.addCriterion( criterionName( block ), hasItem( inputBlock ) )
								.build( c, new ResourceLocation( AppEng.MOD_ID, stairs.identifier() ) )
				)
				.build( consumer, AppEng.MOD_ID, "stairs/block_cutter/" + block.identifier() );

	}

	private String criterionName( IBlockDefinition block )
	{
		return String.format( "has_%s", block.identifier() );
	}

	@Override public String getName()
	{
		return AppEng.MOD_NAME + " Slabs and Stairs";
	}

}
