package appeng.data.providers.recipes;


import appeng.data.providers.IAE2DataProvider;
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

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Consumer;


public class QuartzToolRecipes extends RecipeProvider implements IAE2DataProvider
{

	private Map<net.minecraft.tags.Tag<Item>, Item[]> items = ImmutableMap.of(
			Tags.Items.GEMS_QUARTZ,
			new Item[] {
					ITEMS.netherQuartzAxe().item(),
					ITEMS.netherQuartzHoe().item(),
					ITEMS.netherQuartzShovel().item(),
					ITEMS.netherQuartzPick().item(),
					ITEMS.netherQuartzSword().item(),
					ITEMS.netherQuartzWrench().item(),
					ITEMS.netherQuartzKnife().item()
			}
			// FIXME certus quartz
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
		for( Map.Entry<Tag<Item>, Item[]> entry : items.entrySet() )
		{
			Tag<Item> tag = entry.getKey();
			Item[] tools = entry.getValue();

			for( int i = 0; i < tools.length; i++ )
			{
				CharSet keySet = new CharOpenHashSet( new char[] { 'X', ' ' } );
				ShapedRecipeBuilder builder = ShapedRecipeBuilder.shapedRecipe( tools[i] );
				for( String s : patterns[i] )
				{
					builder.patternLine( s );

					for( int j = 0; j < s.length(); j++ )
					{
						char c = s.charAt( j );
						if( !keySet.contains( c ) )
						{
							keySet.add( c );
							builder.key( c, keys.get( c ) );
						}
					}
				}

				builder.key( 'X', tag )
						.addCriterion( "has_" + tag.toString(), hasItem( tag ) )
						.build( consumer );

			}

		}
	}

	@Nonnull
	@Override
	public String getDataPath()
	{
		return "recipes/tools";
	}

}
