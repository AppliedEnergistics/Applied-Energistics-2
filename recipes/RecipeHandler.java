package appeng.recipes;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import appeng.api.recipes.IRecipeHandler;
import appeng.api.recipes.IRecipeLoader;
import appeng.core.AELog;
import appeng.recipes.handlers.CraftHandler;
import appeng.recipes.handlers.Grind;
import appeng.recipes.handlers.OreRegistration;
import appeng.recipes.handlers.Pureify;
import appeng.recipes.handlers.Shaped;
import appeng.recipes.handlers.Shapeless;
import appeng.recipes.handlers.Smelt;

public class RecipeHandler implements IRecipeHandler
{

	final public List<String> tokens = new LinkedList<String>();
	final RecipeData data;

	public RecipeHandler() {
		data = new RecipeData();
	}

	RecipeHandler(RecipeHandler parent) {
		data = parent.data;
	}

	private void addCrafting(CraftHandler ch)
	{
		data.Handlers.add( ch );
	}

	@Override
	public void registerHandlers()
	{
		HashMap<Class, Integer> processed = new HashMap<Class, Integer>();
		try
		{
			for (CraftHandler ch : data.Handlers)
			{
				try
				{
					ch.register();

					Class clz = ch.getClass();
					Integer i = processed.get( clz );
					if ( i == null )
						processed.put( clz, 1 );
					else
						processed.put( clz, i + 1 );
				}
				catch (RegistrationError e)
				{
					AELog.warning( "Unable to regsiter a recipe." );
					AELog.error( e );
					if ( data.crash )
						throw e;
				}
				catch (MissingIngredientError e)
				{
					if ( data.erroronmissing )
					{
						AELog.warning( "Unable to regsiter a recipe." );
						AELog.error( e );
						if ( data.crash )
							throw e;
					}
				}
			}
		}
		catch (Throwable e)
		{
			AELog.error( e );
			if ( data.crash )
				throw new RuntimeException( e );
		}

		for (Entry<Class, Integer> e : processed.entrySet())
		{
			AELog.info( "Recipes Loading: " + e.getKey().getSimpleName() + ": " + e.getValue() + " loaded." );
		}
	}

	public String alias(String in)
	{
		String out = data.aliases.get( in );

		if ( out != null )
			return out;

		return in;
	}

	@Override
	public void parseRecipes(IRecipeLoader loader, String path)
	{
		try
		{
			BufferedReader reader = null;
			try
			{
				reader = loader.getFile( path );
			}
			catch (Exception err)
			{
				AELog.warning( "Error Loading Recipe File:" + path );
				AELog.error( err );
				return;
			}

			boolean inQuote = false;
			boolean inComment = false;

			String token = "";
			int line = 0;

			int val = -1;
			while ((val = reader.read()) != -1)
			{
				char c = (char) val;

				if ( c == '\n' )
					line++;

				if ( inComment )
				{
					if ( c == '\n' || c == '\r' )
						inComment = false;
				}
				else if ( inQuote )
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

						processTokens( loader, path, line );

						if ( token.length() > 0 )
							tokens.add( token );
						token = "";

						break;

					case '#':
						inComment = true;
						// then add a token if you can...

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
			reader.close();
			processTokens( loader, path, line );
		}
		catch (Throwable e)
		{
			AELog.error( e );
			if ( data.crash )
				throw new RuntimeException( e );
		}
	}

	private void processTokens(IRecipeLoader loader, String file, int line) throws RecipeError
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
						data.aliases.put( tokens.get( 0 ), tokens.get( 2 ) );
					else
						throw new RecipeError( "Alias must have exactly 1 input and 1 output." );
				}
				else if ( operation.equals( "group" ) )
				{
					List<String> pre = tokens.subList( 0, split - 1 );
					List<String> post = tokens.subList( split, tokens.size() );

					List<List<Ingredient>> inputs = parseLines( pre );

					if ( inputs.size() >= 1 && post.size() == 1 )
					{

					}
					else
						throw new RecipeError( "Group must have exactly 1 output, and 1 or more inputs." );
				}
				else if ( operation.equals( "ore" ) )
				{
					List<String> pre = tokens.subList( 0, split - 1 );
					List<String> post = tokens.subList( split, tokens.size() );

					List<List<Ingredient>> inputs = parseLines( pre );

					if ( inputs.size() == 1 && inputs.get( 0 ).size() > 0 && post.size() == 1 )
					{
						CraftHandler ch = new OreRegistration( inputs.get( 0 ), post.get( 0 ) );
						addCrafting( ch );
					}
					else
						throw new RecipeError( "Group must have exactly 1 output, and 1 or more inputs in a single row." );
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
					else if ( operation.equals( "shapeless" ) )
						ch = new Shapeless();
					else if ( operation.equals( "smelt" ) )
						ch = new Smelt();
					else if ( operation.equals( "pureify" ) )
						ch = new Pureify();
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
			{
				String operation = tokens.remove( 0 ).toLowerCase();

				if ( operation.equals( "crash" ) && (tokens.get( 0 ).equals( "true" ) || tokens.get( 0 ).equals( "false" )) )
				{
					if ( tokens.size() == 1 )
					{
						data.crash = tokens.get( 0 ).equals( "true" );
					}
					else
						throw new RecipeError( "crash must be true or false explicitly." );
				}
				else if ( operation.equals( "erroronmissing" ) )
				{
					if ( tokens.size() == 1 && (tokens.get( 0 ).equals( "true" ) || tokens.get( 0 ).equals( "false" )) )
					{
						data.erroronmissing = tokens.get( 0 ).equals( "true" );
					}
					else
						throw new RecipeError( "erroronmissing must be true or false explicitly." );
				}
				else if ( operation.equals( "import" ) )
				{
					if ( tokens.size() == 1 )
						(new RecipeHandler( this )).parseRecipes( loader, tokens.get( 0 ) );
					else
						throw new RecipeError( "Import must have exactly 1 input." );
				}
				else
					throw new RecipeError( operation + ": " + tokens.toString() + "; recipe without an output." );
			}

		}
		catch (RecipeError e)
		{
			AELog.warning( "Recipe Error near line:" + line + " in " + file + " with: " + tokens.toString() );
			AELog.error( e );
			if ( data.crash )
				throw e;
		}

		tokens.clear();
	}

	private List<List<Ingredient>> parseLines(List<String> subList) throws RecipeError
	{
		List<List<Ingredient>> out = new LinkedList<List<Ingredient>>();
		List<Ingredient> cList = new LinkedList<Ingredient>();

		boolean hasQty = false;
		int qty = 1;

		for (String v : subList)
		{
			if ( v.equals( "," ) )
			{
				if ( hasQty )
					throw new RecipeError( "Qty found with no item." );
				if ( !cList.isEmpty() )
					out.add( cList );
				cList = new LinkedList<Ingredient>();
			}
			else
			{
				if ( isNumber( v ) )
				{
					if ( hasQty )
						throw new RecipeError( "Qty found with no item." );
					hasQty = true;
					qty = Integer.parseInt( v );
				}
				else
				{
					if ( hasQty )
					{
						cList.add( new Ingredient( this, v, qty ) );
						hasQty = false;
					}
					else
						cList.add( new Ingredient( this, v, 1 ) );
				}
			}
		}

		if ( !cList.isEmpty() )
			out.add( cList );

		return out;
	}

	private boolean isNumber(String v)
	{
		if ( v.length() <= 0 )
			return false;

		int l = v.length();
		for (int x = 0; x < l; x++)
		{
			if ( !Character.isDigit( v.charAt( x ) ) )
				return false;
		}

		return true;
	}
}
