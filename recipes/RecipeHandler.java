package appeng.recipes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import appeng.core.AELog;
import appeng.recipes.handlers.CraftHandler;
import appeng.recipes.handlers.Grind;
import appeng.recipes.handlers.Shaped;
import appeng.recipes.handlers.Smelt;

public class RecipeHandler
{

	HashMap<String, String> aliases = new HashMap();
	List<CraftHandler> Handlers = new LinkedList();

	private void addCrafting(CraftHandler ch)
	{
		Handlers.add( ch );
	}

	public void registerHandlers()
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
			}
		}
	}

	public String alias(String in)
	{
		String out = aliases.get( in );

		if ( out != null )
			return out;

		return in;
	}

	List<String> tokens = new LinkedList();

	public void parseRecipes(String file)
	{
		file = org.apache.commons.lang3.StringUtils.join( new String[] { "", "alias=", "	mc", "	-> minecraft", "", "alias=", "	ae2", "	-> appliedenergistics2",
				"", "smelt=", "	ae2:ItemMaterial.IronDust", "	-> mc:iron_ingot", "", "smelt=", "	ae2:ItemMaterial.GoldDust", "	-> mc:gold_ingot", "", "craft=",
				"	mc:stick mc:stick mc:stick,", "	_        _        mc:stick,", "	_        _        mc:stick", "	-> ae2:BlockGrinder", "" }, "\n" );

		int len = file.length();

		boolean inQuote = false;

		String token = "";
		int line = 0;

		for (int x = 0; x < len; x++)
		{
			char c = file.charAt( x );

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

					processTokens( line );

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
		processTokens( line );

	}

	private void processTokens(int line)
	{
		try
		{
			if ( tokens.isEmpty() )
				return;

			int split = tokens.indexOf( "->" );
			if ( split != -1 )
			{
				String operation = tokens.remove( 0 );

				if ( operation.equals( "alias" ) )
				{
					if ( tokens.size() == 3 && tokens.indexOf( "->" ) == 1 )
						aliases.put( tokens.get( 0 ), tokens.get( 2 ) );
					else
						throw new RecipeError( "Alias must have exactly 1 input and 1 output." );
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
			AELog.warning( "Recipe Error near line:" + line + " with: " + tokens.toString() );
			AELog.error( e );
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
