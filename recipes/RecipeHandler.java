package appeng.recipes;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.recipes.handlers.CraftHandler;
import appeng.recipes.handlers.Grind;
import appeng.recipes.handlers.Shaped;
import appeng.recipes.handlers.Smelt;

public class RecipeHandler
{

	HashMap<String, String> aliases = new HashMap<String,String>();
	List<CraftHandler> Handlers = new LinkedList<CraftHandler>();
	List<String> tokens = new LinkedList<String>();

	boolean crash = true;
	boolean erroronmissing = true;
	
	private void addCrafting(CraftHandler ch)
	{
		Handlers.add( ch );
	}

	public void registerHandlers()
	{
		try
		{
			for (CraftHandler ch : Handlers)
			{
				try
				{
					ch.register();
				}
				catch (RegistrationError e)
				{
					AELog.warning( "Unable to regsiter a recipe." );
					AELog.error( e );
					if ( crash ) throw e;
				}
				catch (MissingIngredientError e)
				{
					if ( erroronmissing )
					{
						AELog.warning( "Unable to regsiter a recipe." );
						AELog.error( e );	
						if ( crash ) throw e;			
					}
				}
			}
		}
		catch( Throwable e )
		{
			AELog.error( e );
			if ( crash )
				throw new RuntimeException(e);
		}
	}

	public String alias(String in)
	{
		String out = aliases.get( in );

		if ( out != null )
			return out;

		return in;
	}
	
	public void parseRecipes(String path)
	{
		try
		{
			ResourceLocation r =  new ResourceLocation(AppEng.instance.modid, path );
			InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(r).getInputStream();
			DataInputStream reader = new DataInputStream(in);
			
			boolean inQuote = false;
	
			String token = "";
			int line = 0;
	
			while ( in.available() > 0)
			{
				char c =  reader.readChar();
	
				if ( c == '\n' )
					line++;
	
				if ( inQuote )
				{
					switch (c)
					{
					case '"':
						inQuote = !inQuote;
						break;
					default:
						token = token + c;
					}
				}
				else
				{
					switch (c)
					{
					case '"':
						inQuote = !inQuote;
						break;
					case ',':
	
						if ( token.length() > 0 )
						{
							tokens.add( token );
							tokens.add( "," );
						}
						token = "";
						break;
	
					case '=':
	
						processTokens( path,line );
	
						if ( token.length() > 0 )
							tokens.add( token );
						token = "";
	
						break;
	
					case '\n':
					case '\t':
					case '\r':
					case ' ':
	
						if ( token.length() > 0 )
							tokens.add( token );
						token = "";
	
						break;
					default:
						token = token + c;
					}
				}
	
			}
			processTokens( path,line );
		}
		catch( Throwable e )
		{
			AELog.error( e );
			if ( crash )
				throw new RuntimeException(e);
		}
	}

	private void processTokens(String file, int line) throws RecipeError
	{
		try
		{
			if ( tokens.isEmpty() )
				return;

			int split = tokens.indexOf( "->" );
			if ( split != -1 )
			{
				String operation = tokens.remove( 0 ).toLowerCase();

				if ( operation.equals( "alias" ) )
				{
					if ( tokens.size() == 3 && tokens.indexOf( "->" ) == 1 )
						aliases.put( tokens.get( 0 ), tokens.get( 2 ) );
					else
						throw new RecipeError( "Alias must have exactly 1 input and 1 output." );
				}
				else if ( operation.equals( "crash" )&& ( tokens.get( 0 ).equals("true")  ||  tokens.get( 0 ).equals("false")  ))
				{
					if ( tokens.size() == 1 )
					{
						crash = tokens.get(0).equals("true");
					}
					else
						throw new RecipeError( "crash must be true or false explicitly." );
				}
				else if ( operation.equals( "erroronmissing" ) )
				{
					if ( tokens.size() == 1 && ( tokens.get( 0 ).equals("true")  ||  tokens.get( 0 ).equals("false")  ))
					{
						erroronmissing = tokens.get(0).equals("true");
					}
					else
						throw new RecipeError( "erroronmissing must be true or false explicitly." );
				}
				else if ( operation.equals( "import" ) )
				{
					if ( tokens.size() == 1 )
						parseRecipes( tokens.get(0) );
					else
						throw new RecipeError( "Import must have exactly 1 input." );
				}
				else
				{
					List<String> pre = tokens.subList( 0, split - 1 );
					List<String> post = tokens.subList( split, tokens.size() );

					List<List<Ingredient>> inputs = parseLines( pre );
					List<List<Ingredient>> outputs = parseLines( post );

					CraftHandler ch = null;

					if ( operation.equals( "shaped" ) )
						ch = new Shaped();
					else if ( operation.equals( "smelt" ) )
						ch = new Smelt();
					else if ( operation.equals( "grind" ) )
						ch = new Grind();

					if ( ch != null )
					{
						ch.setup( inputs, outputs );
						addCrafting( ch );
					}
					else
						throw new RecipeError( "Invalid crafting type: " + operation );
				}
			}
			else
				throw new RecipeError( tokens.toString() + "; recipe without an output." );

		}
		catch (RecipeError e)
		{
			AELog.warning( "Recipe Error near line:" + line + " in "+file+" with: " + tokens.toString() );
			AELog.error( e );
			if ( crash ) throw e;
		}

		tokens.clear();
	}

	private List<List<Ingredient>> parseLines(List<String> subList) throws RecipeError
	{
		List<List<Ingredient>> out = new LinkedList<List<Ingredient>>();
		List<Ingredient> cList = new LinkedList<Ingredient>();

		for (String v : subList)
		{
			if ( v.equals( "," ) )
			{
				if ( !cList.isEmpty() )
					out.add( cList );
				cList = new LinkedList<Ingredient>();
			}
			else
			{
				cList.add( new Ingredient( this, v ) );
			}
		}

		if ( !cList.isEmpty() )
			out.add( cList );

		return out;
	}
}
