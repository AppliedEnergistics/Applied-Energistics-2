package appeng.forge.data.providers.recipes;


import appeng.api.definitions.IItemDefinition;
import appeng.core.AppEng;
import appeng.forge.data.providers.IAE2DataProvider;
import appeng.recipes.AE2Tags;
import appeng.recipes.conditions.FeaturesEnabled;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Consumer;


public class QuartzToolRecipes extends RecipeProvider implements IAE2DataProvider
{

	private Map<net.minecraft.tags.Tag<Item>, IItemDefinition[]> items = ImmutableMap.of(
			Tags.Items.GEMS_QUARTZ,
			new IItemDefinition[] {
					ITEMS.netherQuartzAxe(),
					ITEMS.netherQuartzHoe(),
					ITEMS.netherQuartzShovel(),
					ITEMS.netherQuartzPick(),
					ITEMS.netherQuartzSword(),
					ITEMS.netherQuartzWrench(),
					ITEMS.netherQuartzKnife()
			},
			AE2Tags.Items.GEMS_CERTUS_QUARTZ,
			new IItemDefinition[] {
					ITEMS.certusQuartzAxe(),
					ITEMS.certusQuartzHoe(),
					ITEMS.certusQuartzShovel(),
					ITEMS.certusQuartzPick(),
					ITEMS.certusQuartzSword(),
					ITEMS.certusQuartzWrench(),
					ITEMS.certusQuartzKnife()
			}
	);

	private String[][] patterns = {
			{
					"XX ",
					"X/ ",
					" / "
			},
			{
					"XX ",
					" / ",
					" / "
			},
			{
					" X ",
					" / ",
					" / "
			},
			{
					"XXX",
					" / ",
					" / "
			},
			{
					" X ",
					" X ",
					" / "
			},
			{
					"X X",
					" X ",
					"X X"
			},
			{
					"  /",
					"I/ ",
					"XX "
			}
	};

	private Char2ObjectMap<Tag<Item>> keys = new Char2ObjectArrayMap<>(
			new char[] { 'I', '/' },
			new Tag[] { Tags.Items.INGOTS_IRON, Tags.Items.RODS_WOODEN }
	);

	public QuartzToolRecipes( DataGenerator generatorIn )
	{
		super( generatorIn );
	}

	@Override
	protected void registerRecipes( @Nonnull Consumer<IFinishedRecipe> consumer )
	{
		for( Map.Entry<Tag<Item>, IItemDefinition[]> entry : items.entrySet() )
		{
			Tag<Item> tag = entry.getKey();
			IItemDefinition[] tools = entry.getValue();

			for( int i = 0; i < tools.length; i++ )
			{
				CharSet keySet = new CharOpenHashSet( new char[] { 'X', ' ' } );

				ShapedRecipeBuilder shapedBuilder = ShapedRecipeBuilder.shapedRecipe( tools[i].item() );
				for( String s : patterns[i] )
				{
					shapedBuilder.patternLine( s );

					for( int j = 0; j < s.length(); j++ )
					{
						char c = s.charAt( j );
						if( !keySet.contains( c ) )
						{
							keySet.add( c );
							shapedBuilder.key( c, keys.get( c ) );
						}
					}
				}

				shapedBuilder.key( 'X', tag )
						.addCriterion( "has_" + tag.getId(), hasItem( tag ) );

				ConditionalRecipe.builder()
						.addCondition( new FeaturesEnabled( tools[i].features() ) )
						.addRecipe( shapedBuilder::build )
						.build( consumer, AppEng.MOD_ID, "tools/" + tools[i].identifier() );
			}

		}
	}

	@Nonnull
	@Override
	public String getName()
	{
		return AppEng.MOD_NAME + " Quartz Tools";
	}

}
