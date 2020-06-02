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

package appeng.items.materials;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.features.IStackSrc;
import appeng.core.features.MaterialStackSrc;
import appeng.items.AEBaseItem;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;


public final class ItemMaterial extends AEBaseItem implements IStorageComponent, IUpgradeModule
{
	public static ItemMaterial instance;

	private static final int KILO_SCALAR = 1024;

	private final Map<Integer, MaterialType> dmgToMaterial = new HashMap<>();

	public ItemMaterial()
	{
		this.setHasSubtypes( true );
		instance = this;
	}

	@OnlyIn( Dist.CLIENT )
	@Override
	public void addInformation( final ItemStack stack, final World world, final List<ITextComponent> lines, final ITooltipFlag advancedTooltips )
	{
		super.addInformation( stack, world, lines, advancedTooltips );

		final MaterialType mt = this.getTypeByStack( stack );
		if( mt == null )
		{
			return;
		}

		if( mt == MaterialType.NAME_PRESS )
		{
			final CompoundNBT c = stack.getOrCreateTag();
			lines.add( c.getString( "InscribeName" ) );
		}

		final Upgrades u = this.getType( stack );
		if( u != null )
		{
			final List<String> textList = new ArrayList<>();
			for( final Entry<ItemStack, Integer> j : u.getSupported().entrySet() )
			{
				String name = null;

				final int limit = j.getValue();

				if( j.getKey().getItem() instanceof IItemGroup )
				{
					final IItemGroup ig = (IItemGroup) j.getKey().getItem();
					final String str = ig.getUnlocalizedGroupName( u.getSupported().keySet(), j.getKey() );
					if( str != null )
					{
						name = Platform.gui_localize( str ) + ( limit > 1 ? " (" + limit + ')' : "" );
					}
				}

				if( name == null )
				{
					name = j.getKey().getDisplayName() + ( limit > 1 ? " (" + limit + ')' : "" );
				}

				if( !textList.contains( name ) )
				{
					textList.add( name );
				}
			}

			final Pattern p = Pattern.compile( "(\\d+)[^\\d]" );
			final SlightlyBetterSort s = new SlightlyBetterSort( p );
			Collections.sort( textList, s );
			lines.addAll( textList );
		}
	}

	public MaterialType getTypeByStack( final ItemStack is )
	{
		MaterialType type = this.dmgToMaterial.get( is.getDamage() );
		return ( type != null ) ? type : MaterialType.INVALID_TYPE;
	}

	@Override
	public Upgrades getType( final ItemStack itemstack )
	{
		switch( this.getTypeByStack( itemstack ) )
		{
			case CARD_CAPACITY:
				return Upgrades.CAPACITY;
			case CARD_FUZZY:
				return Upgrades.FUZZY;
			case CARD_REDSTONE:
				return Upgrades.REDSTONE;
			case CARD_SPEED:
				return Upgrades.SPEED;
			case CARD_INVERTER:
				return Upgrades.INVERTER;
			case CARD_CRAFTING:
				return Upgrades.CRAFTING;
			default:
				return null;
		}
	}

	public IStackSrc createMaterial( final MaterialType mat )
	{
		Preconditions.checkState( !mat.isRegistered(), "Cannot create the same material twice." );

		boolean enabled = true;

		for( final AEFeature f : mat.getFeature() )
		{
			enabled = enabled && AEConfig.instance().isFeatureEnabled( f );
		}

		mat.setStackSrc( new MaterialStackSrc( mat, enabled ) );

		if( enabled )
		{
			mat.setItemInstance( this );
			mat.markReady();
			final int newMaterialNum = mat.getDamageValue();

			if( this.dmgToMaterial.get( newMaterialNum ) == null )
			{
				this.dmgToMaterial.put( newMaterialNum, mat );
			}
			else

			{
				throw new IllegalStateException( "Meta Overlap detected." );
			}
		}

		return mat.getStackSrc();
	}

	@Override
	public String getTranslationKey( final ItemStack is )
	{
		return "item.appliedenergistics2.material." + this.nameOf( is ).toLowerCase();
	}

	@Override
	protected void getCheckedSubItems( final CreativeTabs creativeTab, final NonNullList<ItemStack> itemStacks )
	{
		final List<MaterialType> types = Arrays.asList( MaterialType.values() );
		Collections.sort( types, ( o1, o2 ) -> o1.name().compareTo( o2.name() ) );

		for( final MaterialType mat : types )
		{
			if( mat.getDamageValue() >= 0 && mat.isRegistered() && mat.getItemInstance() == this )
			{
				itemStacks.add( new ItemStack( this, 1, mat.getDamageValue() ) );
			}
		}
	}

	@Override
	public ActionResultType onItemUseFirst( ItemStack stack, ItemUseContext context )
	{
		PlayerEntity player = context.getPlayer();
		Hand hand = context.getHand();
		if( player.isCrouching() )
		{
			final TileEntity te = context.getWorld().getTileEntity( context.getPos() );
			IItemHandler upgrades = null;

			if( te instanceof IPartHost )
			{
				final SelectedPart sp = ( (IPartHost) te ).selectPart( context.getHitVec() );
				if( sp.part instanceof IUpgradeableHost )
				{
					upgrades = ( (ISegmentedInventory) sp.part ).getInventoryByName( "upgrades" );
				}
			}
			else if( te instanceof IUpgradeableHost )
			{
				upgrades = ( (ISegmentedInventory) te ).getInventoryByName( "upgrades" );
			}

			if( upgrades != null && !player.getHeldItem( hand ).isEmpty() && player.getHeldItem( hand ).getItem() instanceof IUpgradeModule )
			{
				final IUpgradeModule um = (IUpgradeModule) player.getHeldItem( hand ).getItem();
				final Upgrades u = um.getType( player.getHeldItem( hand ) );

				if( u != null )
				{
					if( player.world.isRemote )
					{
						return ActionResultType.PASS;
					}

					final InventoryAdaptor ad = new AdaptorItemHandler( upgrades );
					player.setHeldItem( hand, ad.addItems( player.getHeldItem( hand ) ) );
					return ActionResultType.SUCCESS;
				}
			}
		}

		return super.onItemUseFirst( stack, context );
	}

	@Override
	public boolean hasCustomEntity( final ItemStack is )
	{
		return this.getTypeByStack( is ).hasCustomEntity();
	}

	@Override
	public Entity createEntity( final World w, final Entity location, final ItemStack itemstack )
	{
		final Class<? extends Entity> droppedEntity = this.getTypeByStack( itemstack ).getCustomEntityClass();
		final Entity eqi;

		try
		{
			eqi = droppedEntity.getConstructor( World.class, double.class, double.class, double.class, ItemStack.class ).newInstance( w, location.getPosX(), location.getPosY(), location.getPosZ(), itemstack );
		}
		catch( final Throwable t )
		{
			throw new IllegalStateException( t );
		}

		eqi.setMotion( location.getMotion() );

		if( location instanceof ItemEntity && eqi instanceof ItemEntity )
		{
			( (ItemEntity) eqi ).setDefaultPickupDelay();
		}

		return eqi;
	}

	private String nameOf( final ItemStack is )
	{
		if( is.isEmpty() )
		{
			return "null";
		}

		final MaterialType mt = this.getTypeByStack( is );
		if( mt == null )
		{
			return "null";
		}

		return mt.name();
	}

	@Override
	public int getBytes( final ItemStack is )
	{
		switch( this.getTypeByStack( is ) )
		{
			case CELL1K_PART:
				return KILO_SCALAR;
			case CELL4K_PART:
				return KILO_SCALAR * 4;
			case CELL16K_PART:
				return KILO_SCALAR * 16;
			case CELL64K_PART:
				return KILO_SCALAR * 64;
			default:
		}
		return 0;
	}

	@Override
	public boolean isStorageComponent( final ItemStack is )
	{
		switch( this.getTypeByStack( is ) )
		{
			case CELL1K_PART:
			case CELL4K_PART:
			case CELL16K_PART:
			case CELL64K_PART:
				return true;
			default:
		}
		return false;
	}

	private static class SlightlyBetterSort implements Comparator<String>
	{
		private final Pattern pattern;

		public SlightlyBetterSort( final Pattern pattern )
		{
			this.pattern = pattern;
		}

		@Override
		public int compare( final String o1, final String o2 )
		{
			try
			{
				final Matcher a = this.pattern.matcher( o1 );
				final Matcher b = this.pattern.matcher( o2 );
				if( a.find() && b.find() )
				{
					final int ia = Integer.parseInt( a.group( 1 ) );
					final int ib = Integer.parseInt( b.group( 1 ) );
					return Integer.compare( ia, ib );
				}
			}
			catch( final Throwable t )
			{
				// ek!
			}
			return o1.compareTo( o2 );
		}
	}
}
