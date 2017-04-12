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

package appeng.items.parts;


import appeng.api.AEApi;
import appeng.api.exceptions.MissingDefinition;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHelper;
import appeng.api.parts.IPartItem;
import appeng.api.util.AEColor;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.features.ActivityState;
import appeng.core.features.ItemStackSrc;
import appeng.core.features.NameResolver;
import appeng.core.localization.GuiText;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.items.AEBaseItem;
import com.google.common.base.Preconditions;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;


public final class ItemMultiPart extends AEBaseItem implements IPartItem, IItemGroup
{
	private static final int INITIAL_REGISTERED_CAPACITY = PartType.values().length;
	private static final Comparator<Entry<Integer, PartTypeWithVariant>> REGISTERED_COMPARATOR = new RegisteredComparator();

	public static ItemMultiPart instance;
	private final NameResolver nameResolver;
	private final Map<Integer, PartTypeWithVariant> registered;

	public ItemMultiPart( final IPartHelper partHelper )
	{
		Preconditions.checkNotNull( partHelper );

		this.registered = new HashMap<Integer, PartTypeWithVariant>( INITIAL_REGISTERED_CAPACITY );

		this.nameResolver = new NameResolver( this.getClass() );
		this.setFeature( EnumSet.of( AEFeature.Core ) );
		partHelper.setItemBusRenderer( this );
		this.setHasSubtypes( true );

		instance = this;
	}

	@Nonnull
	public final ItemStackSrc createPart( final PartType mat )
	{
		Preconditions.checkNotNull( mat );

		return this.createPart( mat, 0 );
	}

	@Nonnull
	public ItemStackSrc createPart( final PartType mat, final AEColor color )
	{
		Preconditions.checkNotNull( mat );
		Preconditions.checkNotNull( color );

		final int varID = color.ordinal();

		return this.createPart( mat, varID );
	}

	@Nonnull
	private ItemStackSrc createPart( final PartType mat, final int varID )
	{
		assert mat != null;
		assert varID >= 0;

		// verify
		for( final PartTypeWithVariant p : this.registered.values() )
		{
			if( p.part == mat && p.variant == varID )
			{
				throw new IllegalStateException( "Cannot create the same material twice..." );
			}
		}

		boolean enabled = true;
		for( final AEFeature f : mat.getFeature() )
		{
			enabled = enabled && AEConfig.instance.isFeatureEnabled( f );
		}

		for( final IntegrationType integrationType : mat.getIntegrations() )
		{
			enabled &= IntegrationRegistry.INSTANCE.isEnabled( integrationType );
		}

		final int partDamage = mat.getBaseDamage() + varID;
		final ActivityState state = ActivityState.from( enabled );
		final ItemStackSrc output = new ItemStackSrc( this, partDamage, state );

		final PartTypeWithVariant pti = new PartTypeWithVariant( mat, varID );

		this.processMetaOverlap( enabled, partDamage, mat, pti );

		return output;
	}

	private void processMetaOverlap( final boolean enabled, final int partDamage, final PartType mat, final PartTypeWithVariant pti )
	{
		assert partDamage >= 0;
		assert mat != null;
		assert pti != null;

		final PartTypeWithVariant registeredPartType = this.registered.get( partDamage );
		if( registeredPartType != null )
		{
			throw new IllegalStateException( "Meta Overlap detected with type " + mat + " and damage " + partDamage + ". Found " + registeredPartType + " there already." );
		}

		if( enabled )
		{
			this.registered.put( partDamage, pti );
		}
	}

	public int getDamageByType( final PartType t )
	{
		Preconditions.checkNotNull( t );

		for( final Entry<Integer, PartTypeWithVariant> pt : this.registered.entrySet() )
		{
			if( pt.getValue().part == t )
			{
				return pt.getKey();
			}
		}
		return -1;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public int getSpriteNumber()
	{
		return 0;
	}

	@Override
	public IIcon getIconFromDamage( final int dmg )
	{
		final PartTypeWithVariant registeredType = this.registered.get( dmg );
		if( registeredType != null )
		{
			return registeredType.ico;
		}

		final String formattedRegistered = Arrays.toString( this.registered.keySet().toArray() );
		throw new MissingDefinition( "Tried to get the icon from a non-existent part with damage value " + dmg + ". There were registered: " + formattedRegistered + '.' );
	}

	@Override
	public boolean onItemUse( final ItemStack is, final EntityPlayer player, final World w, final int x, final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ )
	{
		if( this.getTypeByStack( is ) == PartType.InvalidType )
		{
			return false;
		}

		return AEApi.instance().partHelper().placeBus( is, x, y, z, side, player, w );
	}

	@Override
	public String getUnlocalizedName( final ItemStack is )
	{
		return "item.appliedenergistics2." + this.getName( is );
	}

	@Override
	public String getItemStackDisplayName( final ItemStack is )
	{
		final PartType pt = this.getTypeByStack( is );

		if( pt.isCable() )
		{
			final AEColor[] variants = AEColor.values();

			final int itemDamage = is.getItemDamage();
			final PartTypeWithVariant registeredPartType = this.registered.get( itemDamage );
			if( registeredPartType != null )
			{
				return super.getItemStackDisplayName( is ) + " - " + variants[registeredPartType.variant].toString();
			}
		}

		if( pt.getExtraName() != null )
		{
			return super.getItemStackDisplayName( is ) + " - " + pt.getExtraName().getLocal();
		}

		return super.getItemStackDisplayName( is );
	}

	@Override
	public void registerIcons( final IIconRegister iconRegister )
	{
		for( final Entry<Integer, PartTypeWithVariant> part : this.registered.entrySet() )
		{
			final String tex = "appliedenergistics2:" + this.getName( new ItemStack( this, 1, part.getKey() ) );
			part.getValue().ico = iconRegister.registerIcon( tex );
		}
	}

	@Override
	protected void getCheckedSubItems( final Item sameItem, final CreativeTabs creativeTab, final List<ItemStack> itemStacks )
	{
		final List<Entry<Integer, PartTypeWithVariant>> types = new ArrayList<Entry<Integer, PartTypeWithVariant>>( this.registered.entrySet() );
		Collections.sort( types, REGISTERED_COMPARATOR );

		for( final Entry<Integer, PartTypeWithVariant> part : types )
		{
			itemStacks.add( new ItemStack( this, 1, part.getKey() ) );
		}
	}

	private String getName( final ItemStack is )
	{
		Preconditions.checkNotNull( is );

		final PartType stackType = this.getTypeByStack( is );
		final String typeName = stackType.name();

		return this.nameResolver.getName( typeName );
	}

	@Nonnull
	public PartType getTypeByStack( final ItemStack is )
	{
		Preconditions.checkNotNull( is );

		final PartTypeWithVariant pt = this.registered.get( is.getItemDamage() );
		if( pt != null )
		{
			return pt.part;
		}

		return PartType.InvalidType;
	}

	@Nullable
	@Override
	public IPart createPartFromItemStack( final ItemStack is )
	{
		final PartType type = this.getTypeByStack( is );
		final Class<? extends IPart> part = type.getPart();
		if( part == null )
		{
			return null;
		}

		try
		{
			if( type.getConstructor() == null )
			{
				type.setConstructor( part.getConstructor( ItemStack.class ) );
			}

			return type.getConstructor().newInstance( is );
		}
		catch( final InstantiationException e )
		{
			throw new IllegalStateException( "Unable to construct IBusPart from IBusItem : " + part.getName() + " ; Possibly didn't have correct constructor( ItemStack )", e );
		}
		catch( final IllegalAccessException e )
		{
			throw new IllegalStateException( "Unable to construct IBusPart from IBusItem : " + part.getName() + " ; Possibly didn't have correct constructor( ItemStack )", e );
		}
		catch( final InvocationTargetException e )
		{
			throw new IllegalStateException( "Unable to construct IBusPart from IBusItem : " + part.getName() + " ; Possibly didn't have correct constructor( ItemStack )", e );
		}
		catch( final NoSuchMethodException e )
		{
			throw new IllegalStateException( "Unable to construct IBusPart from IBusItem : " + part.getName() + " ; Possibly didn't have correct constructor( ItemStack )", e );
		}
	}

	public int variantOf( final int itemDamage )
	{
		final PartTypeWithVariant registeredPartType = this.registered.get( itemDamage );
		if( registeredPartType != null )
		{
			return registeredPartType.variant;
		}

		return 0;
	}

	@Nullable
	@Override
	public String getUnlocalizedGroupName( final Set<ItemStack> others, final ItemStack is )
	{
		boolean importBus = false;
		boolean exportBus = false;
		boolean group = false;

		final PartType u = this.getTypeByStack( is );

		for( final ItemStack stack : others )
		{
			if( stack.getItem() == this )
			{
				final PartType pt = this.getTypeByStack( stack );
				switch( pt )
				{
					case ImportBus:
						importBus = true;
						if( u == pt )
						{
							group = true;
						}
						break;
					case ExportBus:
						exportBus = true;
						if( u == pt )
						{
							group = true;
						}
						break;
					default:
				}
			}
		}

		if( group && importBus && exportBus )
		{
			return GuiText.IOBuses.getUnlocalized();
		}

		return null;
	}

	private static final class PartTypeWithVariant
	{
		private final PartType part;
		private final int variant;

		@SideOnly( Side.CLIENT )
		private IIcon ico;

		private PartTypeWithVariant( final PartType part, final int variant )
		{
			assert part != null;
			assert variant >= 0;

			this.part = part;
			this.variant = variant;
		}

		@Override
		public String toString()
		{
			return "PartTypeWithVariant{" +
					"part=" + this.part +
					", variant=" + this.variant +
					", ico=" + this.ico +
					'}';
		}
	}


	private static final class RegisteredComparator implements Comparator<Entry<Integer, PartTypeWithVariant>>
	{
		@Override
		public int compare( final Entry<Integer, PartTypeWithVariant> o1, final Entry<Integer, PartTypeWithVariant> o2 )
		{
			return o1.getValue().part.name().compareTo( o2.getValue().part.name() );
		}
	}
}
