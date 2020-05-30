
package appeng.recipes;


import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.JSONUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import appeng.core.AppEng;
import appeng.recipes.handlers.GrinderHandler;
import appeng.recipes.handlers.InscriberHandler;
import appeng.recipes.handlers.SmeltingHandler;


public class AERecipeLoader
{
	private static final String AERECIPE_BASE = "/aerecipes";
	private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private final Map<ResourceLocation, IAERecipeFactory> factories = new HashMap<>();

	private final ModContainer mod;
	private final JsonContext ctx;

	public AERecipeLoader()
	{
		this.mod = Loader.instance().getIndexedModList().get( AppEng.MOD_ID );
		this.ctx = new JsonContext( AppEng.MOD_ID );

		this.initFactories();
	}

	public boolean loadProcessingRecipes()
	{
		return CraftingHelper.findFiles( this.mod, "assets/" + AppEng.MOD_ID + AERECIPE_BASE, this::preprocess, this::process, true, true );
	}

	private boolean preprocess( final Path root )
	{
		return true;
	}

	private boolean process( final Path root, final Path file )
	{
		String relative = root.relativize( file ).toString();
		if( !"json".equals( FilenameUtils.getExtension( file.toString() ) ) || relative.startsWith( "_" ) )
		{
			return true;
		}

		String name = FilenameUtils.removeExtension( relative ).replaceAll( "\\\\", "/" );
		ResourceLocation key = new ResourceLocation( this.ctx.getModId(), name );

		BufferedReader reader = null;
		try
		{
			reader = Files.newBufferedReader( file );
			JsonObject json = JSONUtils.fromJson( GSON, reader, JsonObject.class );
			if( json.has( "conditions" ) && !CraftingHelper.processConditions( JSONUtils.getJsonArray( json, "conditions" ), this.ctx ) )
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

		String type = this.ctx.appendModId( JSONUtils.getString( json, "type" ) );
		if( type.isEmpty() )
		{
			throw new JsonSyntaxException( "Recipe type can not be an empty string" );
		}

		IAERecipeFactory factory = this.factories.get( new ResourceLocation( type ) );
		if( factory == null )
		{
			throw new JsonSyntaxException( "Unknown recipe type: " + type );
		}

		factory.register( json, this.ctx );
	}

	private void initFactories()
	{
		this.factories.put( new ResourceLocation( AppEng.MOD_ID, "inscriber" ), new InscriberHandler() );
		this.factories.put( new ResourceLocation( AppEng.MOD_ID, "smelt" ), new SmeltingHandler() );
		this.factories.put( new ResourceLocation( AppEng.MOD_ID, "grinder" ), new GrinderHandler() );
	}
}
