
package appeng.recipes;


import appeng.core.AppEng;
import appeng.recipes.handlers.GrinderHandler;
import appeng.recipes.handlers.InscriberHandler;
import appeng.recipes.handlers.SmeltingHandler;
import com.google.gson.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class AERecipeLoader
{
	private static final String AERECIPE_BASE = "/aerecipes";
	private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private final Map<ResourceLocation, IAERecipeFactory> factories = new HashMap<>();

	IResourceManager resourceManager;

	public AERecipeLoader()
	{
		this.initFactories();
	}

	public boolean loadProcessingRecipes(IForgeRegistry<IRecipe> registry)
	{
		//TODO add recipes
		//CraftingHelper
		//return CraftingHelper.findFiles( this.mod, "assets/" + AppEng.MOD_ID + AERECIPE_BASE, this::preprocess, this::process, true, true );

		Collection<ResourceLocation> recipes = resourceManager.getAllResourceLocations( "data/" + AppEng.MOD_ID + AERECIPE_BASE, this::process );

		for(ResourceLocation recipe : recipes) {
			//add
		}
		return true;
	}

	private boolean preprocess( final String root )
	{
		return true;
	}

	private boolean process( final String file )
	{
		//String relative = root.relativize( file ).toString();
		if( !"json".equals( FilenameUtils.getExtension( file ) ) || file.startsWith( "_" ) )
		{
			return true;
		}

		String name = FilenameUtils.removeExtension( file ).replaceAll( "\\\\", "/" );
		ResourceLocation key = new ResourceLocation( AppEng.MOD_ID, name );

		BufferedReader reader = null;
		try
		{
			reader = Files.newBufferedReader( Paths.get(file) );
			JsonObject json = GSON.fromJson( reader, JsonObject.class );
			if( json.has( "conditions" ) && !CraftingHelper.processConditions( json, "conditions" ) )
			{
				return true;
			}

			this.register( json );
		}
		catch( JsonParseException e )
		{
			FMLLog.log.error( "Parsing error loading recipe {}", key, e );
			return false;
		}
		catch( IOException e )
		{
			FMLLog.log.error( "Couldn't read recipe {} from {}", key, file, e );
			return false;
		}
		finally
		{
			IOUtils.closeQuietly( reader );
		}

		return true;
	}

	private void register( JsonObject json )
	{
		if( json == null || json.isJsonNull() )
		{
			throw new JsonSyntaxException( "Json cannot be null" );
		}

		//TODO check wtf happens here
		String type = json.getAsJsonObject("type").getAsString();
		if( type.isEmpty() )
		{
			throw new JsonSyntaxException( "Recipe type can not be an empty string" );
		}

		IAERecipeFactory factory = this.factories.get( new ResourceLocation( type ) );
		if( factory == null )
		{
			throw new JsonSyntaxException( "Unknown recipe type: " + type );
		}

		factory.register( json );
	}

	private void initFactories()
	{
		this.factories.put( new ResourceLocation( AppEng.MOD_ID, "inscriber" ), new InscriberHandler() );
		this.factories.put( new ResourceLocation( AppEng.MOD_ID, "smelt" ), new SmeltingHandler() );
		this.factories.put( new ResourceLocation( AppEng.MOD_ID, "grinder" ), new GrinderHandler() );
	}
}
