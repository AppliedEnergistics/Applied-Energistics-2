package appeng.data.providers.recipes;


import appeng.core.AppEng;
import appeng.data.providers.IAE2DataProvider;
import net.minecraft.block.Block;
import net.minecraft.data.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Consumer;


public class SlabStairRecipes extends RecipeProvider implements IAE2DataProvider
{

	Block[][] blocks = {
			{ BLOCKS.skyStoneBlock().block(), BLOCKS.skyStoneSlab().block(), BLOCKS.skyStoneStairs().block() },
			{ BLOCKS.smoothSkyStoneBlock().block(), BLOCKS.smoothSkyStoneSlab().block(), BLOCKS.smoothSkyStoneStairs().block() },
			{ BLOCKS.skyStoneBrick().block(), BLOCKS.skyStoneBrickSlab().block(), BLOCKS.skyStoneBrickStairs().block() },
			{ BLOCKS.skyStoneSmallBrick().block(), BLOCKS.skyStoneSmallBrickSlab().block(), BLOCKS.skyStoneSmallBrickStairs().block() },
			{ BLOCKS.fluixBlock().block(), BLOCKS.fluixSlab().block(), BLOCKS.fluixStairs().block() },
			{ BLOCKS.quartzBlock().block(), BLOCKS.quartzSlab().block(), BLOCKS.quartzStairs().block() },
			{ BLOCKS.chiseledQuartzBlock().block(), BLOCKS.chiseledQuartzSlab().block(), BLOCKS.chiseledQuartzStairs().block() },
			{ BLOCKS.quartzPillar().block(), BLOCKS.quartzPillarSlab().block(), BLOCKS.quartzPillarStairs().block() },
	};

	public SlabStairRecipes( DataGenerator generatorIn )
	{
		super( generatorIn );
	}

	@Override
	public void registerRecipes( @Nonnull Consumer<IFinishedRecipe> consumer )
	{
		for( Block[] block : blocks )
		{
			slabRecipe( consumer, block[0], block[1] );
			stairRecipe( consumer, block[0], block[2] );
		}
	}

	private void slabRecipe( Consumer<IFinishedRecipe> consumer, Block block, Block slabs )
	{
		ShapedRecipeBuilder.shapedRecipe( slabs, 6 )
				.patternLine( "###" )
				.key( '#', block )
				.addCriterion( criterionName( block ), hasItem( block ) )
				.build( consumer );

		SingleItemRecipeBuilder.stonecuttingRecipe( Ingredient.fromItems( block ), slabs, 2 )
				.addCriterion( criterionName( block ), hasItem( block ) )
				.build( consumer, blockCutterName( block, slabs ) );
	}

	private void stairRecipe( Consumer<IFinishedRecipe> consumer, Block block, Block stairs )
	{
		ShapedRecipeBuilder.shapedRecipe( stairs, 4 )
				.patternLine( "#  " )
				.patternLine( "## " )
				.patternLine( "###" )
				.key( '#', block )
				.addCriterion( criterionName( block ), hasItem( block ) )
				.build( consumer );

		SingleItemRecipeBuilder.stonecuttingRecipe( Ingredient.fromItems( block ), stairs )
				.addCriterion( criterionName( block ), hasItem( block ) )
				.build( consumer, blockCutterName( block, stairs ) );
	}

	private String criterionName( Block block )
	{
		return String.format( "has_%s", Objects.requireNonNull( block.getRegistryName() ).getPath() );
	}

	private ResourceLocation blockCutterName( Block input, Block result )
	{
		return new ResourceLocation( AppEng.MOD_ID,
				String.format( "%s_from_%s_stonecutting",
						Objects.requireNonNull( result.getRegistryName() ).getPath(),
						Objects.requireNonNull( input.getRegistryName() ).getPath() ) );
	}

	@Nonnull
	@Override
	public String getDataPath()
	{
		return "recipes/slabs_and_stairs";
	}

}
