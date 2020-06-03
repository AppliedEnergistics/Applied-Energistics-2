package appeng.forge.data.providers.recipes;


import appeng.api.definitions.IBlockDefinition;
import appeng.api.features.AEFeature;
import appeng.core.AppEng;
import appeng.forge.data.providers.IAE2DataProvider;
import appeng.recipes.AE2Tags;
import appeng.recipes.conditions.FeaturesEnabled;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.*;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;


public class DecorationRecipes extends RecipeProvider implements IAE2DataProvider
{
	public DecorationRecipes( DataGenerator generatorIn )
	{
		super( generatorIn );
	}

	@Override
	protected void registerRecipes( @Nonnull Consumer<IFinishedRecipe> consumer )
	{
		glassAndLight( consumer );
		quartzRecipes( consumer );
		storageBlocks( consumer );
	}

	private void glassAndLight( Consumer<IFinishedRecipe> consumer )
	{
		IBlockDefinition quartzGlass = BLOCKS.quartzGlass();
		ConditionalRecipe.builder()
				.addCondition( new FeaturesEnabled( quartzGlass.features() ) )
				.addRecipe( ShapedRecipeBuilder.shapedRecipe( quartzGlass.block() )
						.patternLine( "dGd" )
						.patternLine( "GdG" )
						.patternLine( "dGd" )
						.key( 'd', AE2Tags.Items.DUSTS_CERTUS_QUARTZ )
						.key( 'G', Tags.Items.GLASS )
						.addCriterion( "has_glass", hasItem( Tags.Items.GLASS ) )
						.addCriterion( "has_" + AE2Tags.Items.DUSTS_CERTUS_QUARTZ.getId().getPath(), hasItem( AE2Tags.Items.DUSTS_CERTUS_QUARTZ ) )
						::build )
				.build( consumer, AppEng.MOD_ID, prefix( quartzGlass.identifier() ) );

		IBlockDefinition vibrantGlass = BLOCKS.quartzVibrantGlass();
		ConditionalRecipe.builder()
				.addCondition( new FeaturesEnabled( vibrantGlass.features() ) )
				.addRecipe( ShapedRecipeBuilder.shapedRecipe( vibrantGlass.block() )
						.patternLine( "dGd" )
						.key( 'd', Tags.Items.DUSTS_GLOWSTONE )
						.key( 'G', quartzGlass.block() )
						.addCriterion( "has_glowstone", hasItem( Tags.Items.DUSTS_GLOWSTONE ) )
						.addCriterion( "has_" + quartzGlass.identifier(), hasItem( quartzGlass.block() ) )
						::build )
				.build( consumer, AppEng.MOD_ID, prefix( vibrantGlass.identifier() ) );

		IBlockDefinition fixture = BLOCKS.quartzFixture();
		ConditionalRecipe.builder()
				.addCondition( new FeaturesEnabled( fixture.features() ) )
				.addRecipe( ShapedRecipeBuilder.shapedRecipe( fixture.block(), 2 )
						.patternLine( "QI" )
						.key( 'Q', AE2Tags.Items.GEMS_CERTUS_QUARTZ_CHARGED )
						.key( 'I', Tags.Items.INGOTS_IRON )
						.addCriterion( "has_iron", hasItem( Tags.Items.INGOTS_IRON ) )
						.addCriterion( "has_" + AE2Tags.Items.GEMS_CERTUS_QUARTZ_CHARGED.getId().getPath(),
								hasItem( AE2Tags.Items.GEMS_CERTUS_QUARTZ_CHARGED ) )
						::build )
				.build( consumer, AppEng.MOD_ID, prefix( fixture.identifier() ) );
	}

	private void quartzRecipes( Consumer<IFinishedRecipe> consumer )
	{
		IBlockDefinition slab = BLOCKS.quartzSlab();
		IBlockDefinition quartz = BLOCKS.quartzBlock();
		IBlockDefinition chiQuartz = BLOCKS.chiseledQuartzBlock();
		IBlockDefinition piQuartz = BLOCKS.quartzPillar();
		FeaturesEnabled condition = new FeaturesEnabled( quartz.features() );
		ConditionalRecipe.builder()
				.addCondition( condition )
				.addRecipe( ShapedRecipeBuilder.shapedRecipe( chiQuartz.block() )
						.patternLine( "X" )
						.patternLine( "X" )
						.key( 'X', slab.block() )
						.addCriterion( "has_" + slab.identifier(), hasItem( slab.block() ) )
						::build )
				.build( consumer, AppEng.MOD_ID, prefix( chiQuartz.identifier() ) );

		ConditionalRecipe.builder()
				.addCondition( condition )
				.addRecipe( ShapedRecipeBuilder.shapedRecipe( piQuartz.block() )
						.patternLine( "X" )
						.patternLine( "X" )
						.key( 'X', quartz.block() )
						.addCriterion( "has_" + quartz.identifier(), hasItem( quartz.block() ) )
						::build )
				.build( consumer, AppEng.MOD_ID, prefix( piQuartz.identifier() ) );

		ConditionalRecipe.builder()
				.addCondition( condition )
				.addRecipe( c -> SingleItemRecipeBuilder.stonecuttingRecipe( Ingredient.fromItems( quartz.block() ), chiQuartz.block() )
						.addCriterion( "has_" + quartz.identifier(), hasItem( quartz.block() ) )
						.build( c, new ResourceLocation( AppEng.MOD_ID, chiQuartz.identifier() ) ) )
				.build( consumer, AppEng.MOD_ID, prefix( "block_cutter/" + chiQuartz.identifier() ) );

		ConditionalRecipe.builder()
				.addCondition( condition )
				.addRecipe( c -> SingleItemRecipeBuilder.stonecuttingRecipe( Ingredient.fromItems( quartz.block() ), piQuartz.block() )
						.addCriterion( "has_" + quartz.identifier(), hasItem( quartz.block() ) )
						.build( c, new ResourceLocation( AppEng.MOD_ID, piQuartz.identifier() ) ) )
				.build( consumer, AppEng.MOD_ID, prefix( "block_cutter/" + piQuartz.identifier() ) );
		
	}

	private Pair<String[], String[]> patterns = Pair.of(
			new String[]
					{
							"XX",
							"XX"
					},
			new String[]
					{
							"XXX",
							"X X",
							"XXX"
					}
	);

	private List<Triple<Tag<Item>, Tag<Item>, Block>> tags = Arrays.asList(
			Triple.of( AE2Tags.Items.GEMS_CERTUS_QUARTZ, AE2Tags.Items.CRYSTAL_PURE_CERTUS_QUARTZ, BLOCKS.quartzBlock().block() ),
			Triple.of( AE2Tags.Items.GEMS_FLUIX, AE2Tags.Items.CRYSTAL_PURE_FLUIX, BLOCKS.fluixBlock().block() ),
			Triple.of( null, AE2Tags.Items.CRYSTAL_PURE_NETHER_QUARTZ, Blocks.QUARTZ_BLOCK )
	);

	private List<Pair<AEFeature[], AEFeature[]>> features = Arrays.asList(
			Pair.of( new AEFeature[] { AEFeature.CERTUS, AEFeature.DECORATIVE_BLOCKS },
					new AEFeature[] { AEFeature.CERTUS, AEFeature.DECORATIVE_BLOCKS, AEFeature.PURE_CRYSTALS } ),
			Pair.of( new AEFeature[] { AEFeature.FLUIX }, new AEFeature[] { AEFeature.FLUIX, AEFeature.PURE_CRYSTALS } ),
			Pair.of( new AEFeature[0], new AEFeature[] { AEFeature.PURE_CRYSTALS } )
	);

	private void storageBlocks( Consumer<IFinishedRecipe> consumer )
	{
		for( int i = 0; i < tags.size(); i++ )
		{
			Triple<Tag<Item>, Tag<Item>, Block> triple = tags.get( i );

			if( triple.getLeft() != null )
			{
				ConditionalRecipe.builder()
						.addCondition( new FeaturesEnabled( features.get( i ).getLeft() ) )
						.addRecipe(
								Arrays.stream( patterns.getLeft() ).reduce(
										ShapedRecipeBuilder.shapedRecipe( triple.getRight() ),
										ShapedRecipeBuilder::patternLine,
										( a, b ) -> b )
										.key( 'X', triple.getLeft() )
										.addCriterion( "has_" + triple.getLeft().getId().getPath(), hasItem( triple.getLeft() ) )
										::build )
						.build( consumer, AppEng.MOD_ID,
								prefix( Objects.requireNonNull( triple.getRight().getRegistryName() ).toString().replace( ':', '_' ) ) );
			}

			ConditionalRecipe.builder()
					.addCondition( new FeaturesEnabled( features.get( i ).getRight() ) )
					.addRecipe(
							Arrays.stream( patterns.getRight() ).reduce(
									ShapedRecipeBuilder.shapedRecipe( triple.getRight() ),
									ShapedRecipeBuilder::patternLine,
									( a, b ) -> b )
									.key( 'X', triple.getMiddle() )
									.addCriterion( "has_" + triple.getMiddle().getId().getPath(), hasItem( triple.getMiddle() ) )
									::build )
					.build( consumer, AppEng.MOD_ID,
							prefix( Objects.requireNonNull( triple.getRight().getRegistryName() ).toString().replace( ':', '_' ) ) + "_pure" );

		}
	}

	private String prefix( String path )
	{
		return "decoration/" + path;
	}

	@Nonnull
	@Override
	public String getName()
	{
		return AppEng.MOD_NAME + " Decoration Recipes";
	}

}
