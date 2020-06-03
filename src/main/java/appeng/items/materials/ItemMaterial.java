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


import appeng.api.config.Upgrades;
import appeng.api.definitions.IItemDefinition;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.bootstrap.FeatureFactory;
import appeng.items.AEBaseItem;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class ItemMaterial extends AEBaseItem implements IStorageComponent, IUpgradeModule
{
	public static ItemMaterial instance;

	private static final int KILO_SCALAR = 1024;

	@Nonnull
	private final MaterialType mt;

	public ItemMaterial( Properties properties, @Nonnull MaterialType mat )
	{
		super(properties);
		this.mt = mat;
	}

	public static IItemDefinition item( FeatureFactory registry, Properties properties, MaterialType type )
	{
		type.setItem( new ItemMaterial( properties, type ) );
		return registry.item( type.getId(), type::getItem )
				.build();
	}

	@OnlyIn( Dist.CLIENT )
	@Override
	public void addInformation( @Nonnull final ItemStack stack, final World world, @Nonnull final List<ITextComponent> lines, @Nonnull final ITooltipFlag advancedTooltips )
	{
		super.addInformation( stack, world, lines, advancedTooltips );

		if( mt == MaterialType.NAME_PRESS )
		{
			final CompoundNBT c = stack.getOrCreateTag();
			lines.add( new StringTextComponent( c.getString( "InscribeName" ) ) );
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
			final SlightlyBetterSort sort = new SlightlyBetterSort( p );
			textList.sort( sort );
			for( String s : textList )
			{
				lines.add( new StringTextComponent( s ) );
			}
		}
	}

	@Override
	public Upgrades getType( final ItemStack itemstack )
	{
		switch( mt )
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

	@Override
	public String getTranslationKey( @Nonnull final ItemStack is )
	{
		return "item.appliedenergistics2.material." + mt.name().toLowerCase();
	}

	@Override
	public ActionResultType onItemUseFirst( ItemStack stack, ItemUseContext context )
	{
		PlayerEntity player = context.getPlayer();
		Hand hand = context.getHand();
		if( player != null && player.isCrouching() )
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
		return mt.hasCustomEntity();
	}

	@Override
	public Entity createEntity( final World w, final Entity location, final ItemStack itemstack )
	{
		final Class<? extends Entity> droppedEntity = mt.getCustomEntityClass();
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

	@Override
	public int getBytes( final ItemStack is )
	{
		switch( mt )
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
		switch( mt )
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
