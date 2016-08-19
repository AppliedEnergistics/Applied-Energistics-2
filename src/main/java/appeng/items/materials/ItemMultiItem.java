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


import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

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


public final class ItemMultiItem extends AEBaseItem implements IStorageComponent, IUpgradeModule
{
	public static ItemMultiItem instance;

	private static final int KILO_SCALAR = 1024;

	private final Map<Integer, MaterialType> dmgToMaterial = new HashMap<Integer, MaterialType>();

	public ItemMultiItem()
	{
		this.setFeature( EnumSet.of( AEFeature.Core ) );
		this.setHasSubtypes( true );
		instance = this;
	}

	@Override
	public void addCheckedInformation( final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo )
	{
		super.addCheckedInformation( stack, player, lines, displayMoreInfo );

		final MaterialType mt = this.getTypeByStack( stack );
		if( mt == null )
		{
			return;
		}

		if( mt == MaterialType.NamePress )
		{
			final NBTTagCompound c = Platform.openNbtData( stack );
			lines.add( c.getString( "InscribeName" ) );
		}

		final Upgrades u = this.getType( stack );
		if( u != null )
		{
			final List<String> textList = new LinkedList<String>();
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
		MaterialType type = this.dmgToMaterial.get( is.getItemDamage() );
		return (type != null) ? type : MaterialType.InvalidType;
	}

	@Override
	public Upgrades getType( final ItemStack itemstack )
	{
		switch( this.getTypeByStack( itemstack ) )
		{
			case CardCapacity:
				return Upgrades.CAPACITY;
			case CardFuzzy:
				return Upgrades.FUZZY;
			case CardRedstone:
				return Upgrades.REDSTONE;
			case CardSpeed:
				return Upgrades.SPEED;
			case CardInverter:
				return Upgrades.INVERTER;
			case CardCrafting:
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
			enabled = enabled && AEConfig.instance.isFeatureEnabled( f );
		}

		mat.setStackSrc( new MaterialStackSrc( mat ) );

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

	public void makeUnique()
	{
		for( final MaterialType mt : ImmutableSet.copyOf( this.dmgToMaterial.values() ) )
		{
			if( mt.getOreName() != null )
			{
				ItemStack replacement = null;

				final String[] names = mt.getOreName().split( "," );

				for( final String name : names )
				{
					if( replacement != null )
					{
						break;
					}

					final List<ItemStack> options = OreDictionary.getOres( name );
					if( options != null && options.size() > 0 )
					{
						for( final ItemStack is : options )
						{
							if( is != null && is.getItem() != null )
							{
								replacement = is.copy();
								break;
							}
						}
					}
				}

				if( replacement == null || AEConfig.instance.useAEVersion( mt ) )
				{
					// continue using the AE2 item.
					for( final String name : names )
					{
						OreDictionary.registerOre( name, mt.stack( 1 ) );
					}
				}
				else
				{
					if( mt.getItemInstance() == this )
					{
						this.dmgToMaterial.remove( mt.getDamageValue() );
					}

					mt.setItemInstance( replacement.getItem() );
					mt.setDamageValue( replacement.getItemDamage() );
				}
			}
		}
	}

	@Override
	public String getUnlocalizedName( final ItemStack is )
	{
		return "item.appliedenergistics2." + this.nameOf( is );
	}

	@Override
	protected void getCheckedSubItems( final Item sameItem, final CreativeTabs creativeTab, final List<ItemStack> itemStacks )
	{
		final List<MaterialType> types = Arrays.asList( MaterialType.values() );
		Collections.sort( types, new Comparator<MaterialType>(){

			@Override
			public int compare( final MaterialType o1, final MaterialType o2 )
			{
				return o1.name().compareTo( o2.name() );
			}
		} );

		for( final MaterialType mat : types )
		{
			if( mat.getDamageValue() >= 0 && mat.isRegistered() && mat.getItemInstance() == this )
			{
				itemStacks.add( new ItemStack( this, 1, mat.getDamageValue() ) );
			}
		}
	}

	@Override
	public EnumActionResult onItemUseFirst( final ItemStack is, final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand )
	{
		if( player.isSneaking() )
		{
			final TileEntity te = world.getTileEntity( pos );
			IInventory upgrades = null;

			if( te instanceof IPartHost )
			{
				final SelectedPart sp = ( (IPartHost) te ).selectPart( new Vec3d( hitX, hitY, hitZ ) );
				if( sp.part instanceof IUpgradeableHost )
				{
					upgrades = ( (ISegmentedInventory) sp.part ).getInventoryByName( "upgrades" );
				}
			}
			else if( te instanceof IUpgradeableHost )
			{
				upgrades = ( (ISegmentedInventory) te ).getInventoryByName( "upgrades" );
			}

			if( upgrades != null && is != null && is.getItem() instanceof IUpgradeModule )
			{
				final IUpgradeModule um = (IUpgradeModule) is.getItem();
				final Upgrades u = um.getType( is );

				if( u != null )
				{
					final InventoryAdaptor ad = InventoryAdaptor.getAdaptor( upgrades, EnumFacing.UP );
					if( ad != null )
					{
						if( player.worldObj.isRemote )
						{
							return EnumActionResult.PASS;
						}

						player.inventory.setInventorySlotContents( player.inventory.currentItem, ad.addItems( is ) );
						return EnumActionResult.SUCCESS;
					}
				}
			}
		}

		return super.onItemUseFirst( is, player, world, pos, side, hitX, hitY, hitZ, hand );
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
			eqi = droppedEntity.getConstructor( World.class, double.class, double.class, double.class, ItemStack.class ).newInstance( w, location.posX, location.posY, location.posZ, itemstack );
		}
		catch( final Throwable t )
		{
			throw new IllegalStateException( t );
		}

		eqi.motionX = location.motionX;
		eqi.motionY = location.motionY;
		eqi.motionZ = location.motionZ;

		if( location instanceof EntityItem && eqi instanceof EntityItem )
		{
			// TODO: Entity Pick up time?
			// needs fixing?
			// ( (EntityItem) eqi ).setPickupDelay( ( (EntityItem) location ).pick;
		}

		return eqi;
	}

	private String nameOf( final ItemStack is )
	{
		if( is == null )
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
			case Cell1kPart:
				return KILO_SCALAR;
			case Cell4kPart:
				return KILO_SCALAR * 4;
			case Cell16kPart:
				return KILO_SCALAR * 16;
			case Cell64kPart:
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
			case Cell1kPart:
			case Cell4kPart:
			case Cell16kPart:
			case Cell64kPart:
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

	@Override
	@SideOnly( Side.CLIENT )
	public List<ResourceLocation> getItemVariants()
	{
		// Register a resource location for every material type
		return Arrays.stream( MaterialType.values() )
				.map( MaterialType::getModel )
				.collect( Collectors.toList() );

	}

	@Override
	@SideOnly( Side.CLIENT )
	public ItemMeshDefinition getItemMeshDefinition()
	{
		return is -> getTypeByStack( is ).getModel();
	}

}
