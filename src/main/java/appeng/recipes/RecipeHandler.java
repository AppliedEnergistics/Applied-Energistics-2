/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.LoaderState;

import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItems;
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
import appeng.items.materials.ItemMultiItem;
import appeng.items.misc.ItemCrystalSeed;
import appeng.items.parts.ItemMultiPart;
import appeng.recipes.handlers.IWebsiteSerializer;
import appeng.recipes.handlers.OreRegistration;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv3 - 10.08.2015
 * @since rv0
 */
public class RecipeHandler implements IRecipeHandler
{
	private final RecipeData data;
	private final List<String> tokens = new LinkedList<String>();

	public RecipeHandler()
	{
		this.data = new RecipeData();
	}

	RecipeHandler( final RecipeHandler parent )
	{
		Preconditions.checkNotNull( parent );
		this.data = parent.data;
	}

	private void addCrafting( final ICraftHandler ch )
	{
		this.data.handlers.add( ch );
	}

	public String getName( @Nonnull final IIngredient i )
	{
		try
		{
			for( final ItemStack is : i.getItemStackSet() )
			{
				return this.getName( is );
			}
		}
		catch( final RecipeError ignored )
		{
		}
		catch( final Throwable t )
		{
			t.printStackTrace();
			// :P
		}

		return i.getNameSpace() + ':' + i.getItemName();
	}

	private String getName( final ItemStack is ) throws RecipeError
	{
		Preconditions.checkNotNull( is );

		final ResourceLocation id = Item.REGISTRY.getNameForObject( is.getItem() );
		String realName = id.toString();

		if( !id.getResourceDomain().equals( AppEng.MOD_ID ) && !id.getResourceDomain().equals( "minecraft" ) )
		{
			throw new RecipeError( "Not applicable for website" );
		}

		final IDefinitions definitions = AEApi.instance().definitions();
		final IItems items = definitions.items();
		final IBlocks blocks = definitions.blocks();

		final Optional<Item> maybeCrystalSeedItem = items.crystalSeed().maybeItem();
		final Optional<Item> maybeSkyStoneItem = blocks.skyStone().maybeItem();
		final Optional<Item> maybeCStorageItem = blocks.craftingStorage1k().maybeItem();
		final Optional<Item> maybeCUnitItem = blocks.craftingUnit().maybeItem();
		final Optional<Item> maybeSkyChestItem = blocks.skyChest().maybeItem();

		if( maybeCrystalSeedItem.isPresent() && is.getItem() == maybeCrystalSeedItem.get() )
		{
			final int dmg = is.getItemDamage();
			if( dmg < ItemCrystalSeed.NETHER )
			{
				realName += ".Certus";
			}
			else if( dmg < ItemCrystalSeed.FLUIX )
			{
				realName += ".Nether";
			}
			else if( dmg < ItemCrystalSeed.FINAL_STAGE )
			{
				realName += ".Fluix";
			}
		}
		else if( maybeSkyStoneItem.isPresent() && is.getItem() == maybeSkyStoneItem.get() )
		{
			switch( is.getItemDamage() )
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
		else if( maybeCStorageItem.isPresent() && is.getItem() == maybeCStorageItem.get() )
		{
			switch( is.getItemDamage() )
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
		else if( maybeCUnitItem.isPresent() && is.getItem() == maybeCUnitItem.get() )
		{
			switch( is.getItemDamage() )
			{
				case 1:
					realName = realName.replace( "Unit", "Accelerator" );
					break;
				default:
			}
		}
		else if( maybeSkyChestItem.isPresent() && is.getItem() == maybeSkyChestItem.get() )
		{
			switch( is.getItemDamage() )
			{
				case 1:
					realName += ".Block";
					break;
				default:
			}
		}
		else if( is.getItem() instanceof ItemMultiItem )
		{
			realName = realName.replace( "ItemMultiMaterial", "ItemMaterial" );
			realName += '.' + ( (ItemMultiItem) is.getItem() ).getTypeByStack( is ).name();
		}
		else if( is.getItem() instanceof ItemMultiPart )
		{
			realName = realName.replace( "ItemMultiPart", "ItemPart" );
			realName += '.' + ( (ItemMultiPart) is.getItem() ).getTypeByStack( is ).name();
		}
		else if( is.getItemDamage() > 0 )
		{
			realName += "." + is.getItemDamage();
		}

		return realName;
	}

	String alias( final String in )
	{
		Preconditions.checkNotNull( in );

		final String out = this.data.aliases.get( in );

		if( out != null )
		{
			return out;
		}

		return in;
	}

	@Override
	public void parseRecipes( final IRecipeLoader loader, final String path )
	{
		Preconditions.checkNotNull( loader );
		Preconditions.checkNotNull( path );

		try
		{
			BufferedReader reader = null;
			try
			{
				reader = loader.getFile( path );
			}
			catch( final Exception err )
			{
				AELog.warn( "Error Loading Recipe File:" + path );
				if( this.data.exceptions )
				{
					AELog.debug( err );
				}
				return;
			}

			boolean inQuote = false;
			boolean inComment = false;

			String token = "";
			int line = 0;

			int val = -1;
			while( ( val = reader.read() ) != -1 )
			{
				final char c = (char) val;

				if( c == '\n' )
				{
					line++;
				}

				if( inComment )
				{
					if( c == '\n' || c == '\r' )
					{
						inComment = false;
					}
				}
				else if( inQuote )
				{
					switch( c )
					{
						case '"':
							inQuote = !inQuote;
							break;
						default:
							token += c;
					}
				}
				else
				{
					switch( c )
					{
						case '"':
							inQuote = !inQuote;
							break;
						case ',':

							if( token.length() > 0 )
							{
								this.tokens.add( token );
								this.tokens.add( "," );
							}
							token = "";
							break;

						case '=':

							this.processTokens( loader, path, line );

							if( token.length() > 0 )
							{
								this.tokens.add( token );
							}
							token = "";

							break;

						case '#':
							inComment = true;
							// then add a token if you can...

						case '\n':
						case '\t':
						case '\r':
						case ' ':

							if( token.length() > 0 )
							{
								this.tokens.add( token );
							}
							token = "";

							break;
						default:
							token += c;
					}
				}
			}

			if( token.length() > 0 )
			{
				this.tokens.add( token );
			}

			reader.close();
			this.processTokens( loader, path, line );
		}
		catch( final Throwable e )
		{
			AELog.debug( e );
			if( this.data.crash )
			{
				throw new IllegalStateException( e );
			}
		}
	}

	@Override
	public void injectRecipes()
	{
		if( net.minecraftforge.fml.common.Loader.instance().hasReachedState( LoaderState.POSTINITIALIZATION ) )
		{
			throw new IllegalStateException( "Recipes must now be loaded in Init." );
		}

		final Map<Class, Integer> processed = new HashMap<Class, Integer>();
		try
		{
			for( final ICraftHandler ch : this.data.handlers )
			{
				try
				{
					ch.register();

					final Class clz = ch.getClass();
					final Integer i = processed.get( clz );
					if( i == null )
					{
						processed.put( clz, 1 );
					}
					else
					{
						processed.put( clz, i + 1 );
					}
				}
				catch( final RegistrationError e )
				{
					AELog.warn( "Unable to register a recipe: " + e.getMessage() );
					if( this.data.exceptions )
					{
						AELog.debug( e );
					}
					if( this.data.crash )
					{
						throw e;
					}
				}
				catch( final MissingIngredientError e )
				{
					if( this.data.errorOnMissing )
					{
						AELog.warn( "Unable to register a recipe:" + e.getMessage() );
						if( this.data.exceptions )
						{
							AELog.debug( e );
						}
						if( this.data.crash )
						{
							throw e;
						}
					}
				}
			}
		}
		catch( final Throwable e )
		{
			if( this.data.exceptions )
			{
				AELog.debug( e );
			}
			if( this.data.crash )
			{
				throw new IllegalStateException( e );
			}
		}

		for( final Entry<Class, Integer> e : processed.entrySet() )
		{
			AELog.info( "Recipes Loading: " + e.getKey().getSimpleName() + ": " + e.getValue() + " loaded." );
		}

		if( AEConfig.instance.isFeatureEnabled( AEFeature.WebsiteRecipes ) )
		{
			try
			{
				final ZipOutputStream out = new ZipOutputStream( new FileOutputStream( "recipes.zip" ) );

				final HashMultimap<String, IWebsiteSerializer> combined = HashMultimap.create();

				for( final String s : this.data.knownItem )
				{
					try
					{

						final IIngredient i = new Ingredient( this, s, 1 );

						for( final ItemStack is : i.getItemStackSet() )
						{
							final String realName = this.getName( is );
							final List<IWebsiteSerializer> recipes = this.findRecipe( is );
							if( !recipes.isEmpty() )
							{
								combined.putAll( realName, recipes );
							}
						}
					}
					catch( final RecipeError ignored )
					{

					}
					catch( final MissedIngredientSet ignored )
					{

					}
					catch( final RegistrationError ignored )
					{

					}
					catch( final MissingIngredientError ignored )
					{

					}
				}

				for( final String realName : combined.keySet() )
				{
					int offset = 0;

					for( final IWebsiteSerializer ws : combined.get( realName ) )
					{
						final String rew = ws.getPattern( this );
						if( rew != null && rew.length() > 0 )
						{
							out.putNextEntry( new ZipEntry( realName + '_' + offset + ".txt" ) );
							offset++;
							out.write( rew.getBytes() );
						}
					}
				}

				out.close();
			}
			catch( final FileNotFoundException e1 )
			{
				AELog.debug( e1 );
			}
			catch( final IOException e1 )
			{
				AELog.debug( e1 );
			}
		}
	}

	private List<IWebsiteSerializer> findRecipe( final ItemStack output )
	{
		final List<IWebsiteSerializer> out = new LinkedList<IWebsiteSerializer>();

		for( final ICraftHandler ch : this.data.handlers )
		{
			try
			{
				if( ch instanceof IWebsiteSerializer && ( (IWebsiteSerializer) ch ).canCraft( output ) )
				{
					out.add( (IWebsiteSerializer) ch );
				}
			}
			catch( final Throwable t )
			{
				AELog.debug( t );
			}
		}

		return out;
	}

	RecipeData getData()
	{
		return this.data;
	}

	private void processTokens( final IRecipeLoader loader, final String file, final int line ) throws RecipeError
	{
		try
		{
			final IRecipeHandlerRegistry cr = AEApi.instance().registries().recipes();

			if( this.tokens.isEmpty() )
			{
				return;
			}

			final int split = this.tokens.indexOf( "->" );
			if( split != -1 )
			{
				final String operation = this.tokens.remove( 0 ).toLowerCase( Locale.ENGLISH );

				if( operation.equals( "alias" ) )
				{
					if( this.tokens.size() == 3 && this.tokens.indexOf( "->" ) == 1 )
					{
						this.data.aliases.put( this.tokens.get( 0 ), this.tokens.get( 2 ) );
					}
					else
					{
						throw new RecipeError( "Alias must have exactly 1 input and 1 output." );
					}
				}
				else if( operation.equals( "group" ) )
				{
					final List<String> pre = this.tokens.subList( 0, split - 1 );
					final List<String> post = this.tokens.subList( split, this.tokens.size() );

					final List<List<IIngredient>> inputs = this.parseLines( pre );

					if( inputs.size() == 1 && inputs.get( 0 ).size() > 0 && post.size() == 1 )
					{
						this.data.groups.put( post.get( 0 ), new GroupIngredient( post.get( 0 ), inputs.get( 0 ), 1 ) );
					}
					else
					{
						throw new RecipeError( "Group must have exactly 1 output, and 1 or more inputs." );
					}
				}
				else if( operation.equals( "ore" ) )
				{
					final List<String> pre = this.tokens.subList( 0, split - 1 );
					final List<String> post = this.tokens.subList( split, this.tokens.size() );

					final List<List<IIngredient>> inputs = this.parseLines( pre );

					if( inputs.size() == 1 && inputs.get( 0 ).size() > 0 && post.size() == 1 )
					{
						final ICraftHandler ch = new OreRegistration( inputs.get( 0 ), post.get( 0 ) );
						this.addCrafting( ch );
					}
					else
					{
						throw new RecipeError( "Group must have exactly 1 output, and 1 or more inputs in a single row." );
					}
				}
				else
				{
					final List<String> pre = this.tokens.subList( 0, split - 1 );
					final List<String> post = this.tokens.subList( split, this.tokens.size() );

					final List<List<IIngredient>> inputs = this.parseLines( pre );
					final List<List<IIngredient>> outputs = this.parseLines( post );

					final ICraftHandler ch = cr.getCraftHandlerFor( operation );

					if( ch != null )
					{
						ch.setup( inputs, outputs );
						this.addCrafting( ch );
					}
					else
					{
						throw new RecipeError( "Invalid crafting type: " + operation );
					}
				}
			}
			else
			{
				final String operation = this.tokens.remove( 0 ).toLowerCase();

				if( operation.equals( "exceptions" ) && ( this.tokens.get( 0 ).equals( "true" ) || this.tokens.get( 0 ).equals( "false" ) ) )
				{
					if( this.tokens.size() == 1 )
					{
						this.data.exceptions = this.tokens.get( 0 ).equals( "true" );
					}
					else
					{
						throw new RecipeError( "exceptions must be true or false explicitly." );
					}
				}
				else if( operation.equals( "crash" ) && ( this.tokens.get( 0 ).equals( "true" ) || this.tokens.get( 0 ).equals( "false" ) ) )
				{
					if( this.tokens.size() == 1 )
					{
						this.data.crash = this.tokens.get( 0 ).equals( "true" );
					}
					else
					{
						throw new RecipeError( "crash must be true or false explicitly." );
					}
				}
				else if( operation.equals( "erroronmissing" ) )
				{
					if( this.tokens.size() == 1 && ( this.tokens.get( 0 ).equals( "true" ) || this.tokens.get( 0 ).equals( "false" ) ) )
					{
						this.data.errorOnMissing = this.tokens.get( 0 ).equals( "true" );
					}
					else
					{
						throw new RecipeError( "erroronmissing must be true or false explicitly." );
					}
				}
				else if( operation.equals( "import" ) )
				{
					if( this.tokens.size() == 1 )
					{
						( new RecipeHandler( this ) ).parseRecipes( loader, this.tokens.get( 0 ) );
					}
					else
					{
						throw new RecipeError( "Import must have exactly 1 input." );
					}
				}
				else
				{
					throw new RecipeError( operation + ": " + this.tokens.toString() + "; recipe without an output." );
				}
			}
		}
		catch( final RecipeError e )
		{
			AELog.warn( "Recipe Error '" + e.getMessage() + "' near line:" + line + " in " + file + " with: " + this.tokens.toString() );
			if( this.data.exceptions )
			{
				AELog.debug( e );
			}
			if( this.data.crash )
			{
				throw e;
			}
		}

		this.tokens.clear();
	}

	private List<List<IIngredient>> parseLines( final Iterable<String> subList ) throws RecipeError
	{
		final List<List<IIngredient>> out = new LinkedList<List<IIngredient>>();
		List<IIngredient> cList = new LinkedList<IIngredient>();

		boolean hasQty = false;
		int qty = 1;

		for( final String v : subList )
		{
			if( v.equals( "," ) )
			{
				if( hasQty )
				{
					throw new RecipeError( "Qty found with no item." );
				}
				if( !cList.isEmpty() )
				{
					out.add( cList );
				}
				cList = new LinkedList<IIngredient>();
			}
			else
			{
				if( this.isNumber( v ) )
				{
					if( hasQty )
					{
						throw new RecipeError( "Qty found with no item." );
					}
					hasQty = true;
					qty = Integer.parseInt( v );
				}
				else
				{
					if( hasQty )
					{
						cList.add( this.findIngredient( v, qty ) );
						hasQty = false;
					}
					else
					{
						cList.add( this.findIngredient( v, 1 ) );
					}
				}
			}
		}

		if( !cList.isEmpty() )
		{
			out.add( cList );
		}

		return out;
	}

	private IIngredient findIngredient( final String v, final int qty ) throws RecipeError
	{
		final GroupIngredient gi = this.data.groups.get( v );

		if( gi != null )
		{
			return gi.copy( qty );
		}

		try
		{
			return new Ingredient( this, v, qty );
		}
		catch( final MissedIngredientSet grp )
		{
			return new IngredientSet( grp.getResolverResultSet(), qty );
		}
	}

	private boolean isNumber( final CharSequence v )
	{
		if( v.length() <= 0 )
		{
			return false;
		}

		final int l = v.length();
		for( int x = 0; x < l; x++ )
		{
			if( !Character.isDigit( v.charAt( x ) ) )
			{
				return false;
			}
		}

		return true;
	}
}
