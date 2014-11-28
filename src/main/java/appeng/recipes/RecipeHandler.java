/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.recipes;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

import com.google.common.collect.HashMultimap;

import appeng.api.AEApi;
import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RecipeError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.features.IRecipeHandlerRegistry;
import appeng.api.recipes.ICraftHandler;
import appeng.api.recipes.IIngredient;
import appeng.api.recipes.IRecipeHandler;
import appeng.api.recipes.IRecipeLoader;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.items.materials.ItemMultiMaterial;
import appeng.items.misc.ItemCrystalSeed;
import appeng.items.parts.ItemMultiPart;
import appeng.recipes.handlers.IWebsiteSerializer;
import appeng.recipes.handlers.OreRegistration;

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

	private void addCrafting(ICraftHandler ch)
	{
		data.Handlers.add( ch );
	}

	public List<IWebsiteSerializer> findRecipe(ItemStack output)
	{
		List<IWebsiteSerializer> out = new LinkedList<IWebsiteSerializer>();

		for (ICraftHandler ch : data.Handlers)
		{
			try
			{
				if ( ch instanceof IWebsiteSerializer && ((IWebsiteSerializer) ch).canCraft( output ) )
				{
					out.add( (IWebsiteSerializer) ch );
				}
			}
			catch (Throwable t)
			{
				AELog.error( t );
			}
		}

		return out;
	}

	@Override
	public void injectRecipes()
	{
		if ( cpw.mods.fml.common.Loader.instance().hasReachedState( LoaderState.POSTINITIALIZATION ) )
			throw new RuntimeException( "Recipes must now be loaded in Init." );

		HashMap<Class, Integer> processed = new HashMap<Class, Integer>();
		try
		{
			for (ICraftHandler ch : data.Handlers)
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
					AELog.warning( "Unable to register a recipe: " + e.getMessage() );
					if ( data.exceptions )
						AELog.error( e );
					if ( data.crash )
						throw e;
				}
				catch (MissingIngredientError e)
				{
					if ( data.errorOnMissing )
					{
						AELog.warning( "Unable to register a recipe:" + e.getMessage() );
						if ( data.exceptions )
							AELog.error( e );
						if ( data.crash )
							throw e;
					}
				}
			}
		}
		catch (Throwable e)
		{
			if ( data.exceptions )
				AELog.error( e );
			if ( data.crash )
				throw new RuntimeException( e );
		}

		for (Entry<Class, Integer> e : processed.entrySet())
		{
			AELog.info( "Recipes Loading: " + e.getKey().getSimpleName() + ": " + e.getValue() + " loaded." );
		}

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.WebsiteRecipes ) )
		{
			try
			{
				ZipOutputStream out = new ZipOutputStream( new FileOutputStream( "recipes.zip" ) );

				HashMultimap<String, IWebsiteSerializer> combined = HashMultimap.create();

				for (String s : data.knownItem)
				{
					try
					{

						Ingredient i = new Ingredient( this, s, 1 );

						for (ItemStack is : i.getItemStackSet())
						{
							String realName = getName( is );
							List<IWebsiteSerializer> recipes = findRecipe( is );
							if ( !recipes.isEmpty() )
								combined.putAll( realName, recipes );
						}

					}
					catch (RecipeError ignored)
					{

					}
					catch (MissedIngredientSet ignored)
					{

					}
					catch (RegistrationError ignored)
					{

					}
					catch (MissingIngredientError ignored)
					{

					}
				}

				for (String realName : combined.keySet())
				{
					int offset = 0;

					for (IWebsiteSerializer ws : combined.get( realName ))
					{
						String rew = ws.getPattern( this );
						if ( rew != null && rew.length() > 0 )
						{
							out.putNextEntry( new ZipEntry( realName + '_' + offset + ".txt" ) );
							offset++;
							out.write( rew.getBytes() );
						}
					}
				}

				out.close();
			}
			catch (FileNotFoundException e1)
			{
				AELog.error( e1 );
			}
			catch (IOException e1)
			{
				AELog.error( e1 );
			}

		}
	}

	public String getName(IIngredient i)
	{
		try
		{
			for (ItemStack is : i.getItemStackSet())
			{
				try
				{
					return getName( is );
				}
				catch (RecipeError ignored)
				{
				}
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			// :P
		}

		return i.getNameSpace() + ':' + i.getItemName();
	}

	public String getName(ItemStack is) throws RecipeError
	{
		UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor( is.getItem() );
		String realName = id.modId + ':' + id.name;

		if ( !id.modId.equals( AppEng.modid ) && !id.modId.equals( "minecraft" ) )
			throw new RecipeError( "Not applicable for website" );

		if ( is.getItem() == AEApi.instance().items().itemCrystalSeed.item() )
		{
			int dmg = is.getItemDamage();
			if ( dmg < ItemCrystalSeed.Nether )
				realName += ".Certus";
			else if ( dmg < ItemCrystalSeed.Fluix )
				realName += ".Nether";
			else if ( dmg < ItemCrystalSeed.END )
				realName += ".Fluix";
		}
		else if ( is.getItem() == AEApi.instance().blocks().blockSkyStone.item() )
		{
			switch (is.getItemDamage())
			{
			case 1:
				realName += ".Block";
				break;
			case 2:
				realName += ".Brick";
				break;
			case 3:
				realName += ".SmallBrick";
				break;
			default:
			}
		}
		else if ( is.getItem() == AEApi.instance().blocks().blockCraftingStorage1k.item() )
		{
			switch (is.getItemDamage())
			{
			case 1:
				realName += "4k";
				break;
			case 2:
				realName += "16k";
				break;
			case 3:
				realName += "64k";
				break;
			default:
			}
		}
		else if ( is.getItem() == AEApi.instance().blocks().blockCraftingUnit.item() )
		{
			switch (is.getItemDamage())
			{
			case 1:
				realName = realName.replace( "Unit", "Accelerator" );
				break;
			default:
			}
		}
		else if ( is.getItem() == AEApi.instance().blocks().blockSkyChest.item() )
		{
			switch (is.getItemDamage())
			{
			case 1:
				realName += ".Block";
				break;
			default:
			}
		}
		else if ( is.getItem() instanceof ItemMultiMaterial )
		{
			realName = realName.replace( "ItemMultiMaterial", "ItemMaterial" );
			realName += '.' + ((ItemMultiMaterial) is.getItem()).getTypeByStack( is ).name();
		}
		else if ( is.getItem() instanceof ItemMultiPart )
		{
			realName = realName.replace( "ItemMultiPart", "ItemPart" );
			realName += '.' + ((ItemMultiPart) is.getItem()).getTypeByStack( is ).name();
		}
		else if ( is.getItemDamage() > 0 )
			realName += "." + is.getItemDamage();

		return realName;

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
				if ( data.exceptions )
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

			if ( token.length() > 0 )
				tokens.add( token );

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
			IRecipeHandlerRegistry cr = AEApi.instance().registries().recipes();

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

					List<List<IIngredient>> inputs = parseLines( pre );

					if ( inputs.size() == 1 && inputs.get( 0 ).size() > 0 && post.size() == 1 )
					{
						data.groups.put( post.get( 0 ), new GroupIngredient( post.get( 0 ), inputs.get( 0 ) ) );
					}
					else
						throw new RecipeError( "Group must have exactly 1 output, and 1 or more inputs." );
				}
				else if ( operation.equals( "ore" ) )
				{
					List<String> pre = tokens.subList( 0, split - 1 );
					List<String> post = tokens.subList( split, tokens.size() );

					List<List<IIngredient>> inputs = parseLines( pre );

					if ( inputs.size() == 1 && inputs.get( 0 ).size() > 0 && post.size() == 1 )
					{
						ICraftHandler ch = new OreRegistration( inputs.get( 0 ), post.get( 0 ) );
						addCrafting( ch );
					}
					else
						throw new RecipeError( "Group must have exactly 1 output, and 1 or more inputs in a single row." );
				}
				else
				{
					List<String> pre = tokens.subList( 0, split - 1 );
					List<String> post = tokens.subList( split, tokens.size() );

					List<List<IIngredient>> inputs = parseLines( pre );
					List<List<IIngredient>> outputs = parseLines( post );

					ICraftHandler ch = cr.getCraftHandlerFor( operation );

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

				if ( operation.equals( "exceptions" ) && (tokens.get( 0 ).equals( "true" ) || tokens.get( 0 ).equals( "false" )) )
				{
					if ( tokens.size() == 1 )
					{
						data.exceptions = tokens.get( 0 ).equals( "true" );
					}
					else
						throw new RecipeError( "exceptions must be true or false explicitly." );
				}
				else if ( operation.equals( "crash" ) && (tokens.get( 0 ).equals( "true" ) || tokens.get( 0 ).equals( "false" )) )
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
						data.errorOnMissing = tokens.get( 0 ).equals( "true" );
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
			AELog.warning( "Recipe Error '" + e.getMessage() + "' near line:" + line + " in " + file + " with: " + tokens.toString() );
			if ( data.exceptions )
				AELog.error( e );
			if ( data.crash )
				throw e;
		}

		tokens.clear();
	}

	private List<List<IIngredient>> parseLines(List<String> subList) throws RecipeError
	{
		List<List<IIngredient>> out = new LinkedList<List<IIngredient>>();
		List<IIngredient> cList = new LinkedList<IIngredient>();

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
				cList = new LinkedList<IIngredient>();
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
						cList.add( findIngredient( v, qty ) );
						hasQty = false;
					}
					else
						cList.add( findIngredient( v, 1 ) );
				}
			}
		}

		if ( !cList.isEmpty() )
			out.add( cList );

		return out;
	}

	private IIngredient findIngredient(String v, int qty) throws RecipeError
	{
		GroupIngredient gi = data.groups.get( v );

		if ( gi != null )
			return gi.copy( qty );

		try
		{
			return new Ingredient( this, v, qty );
		}
		catch (MissedIngredientSet grp)
		{
			return new IngredientSet( grp.rrs );
		}
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
