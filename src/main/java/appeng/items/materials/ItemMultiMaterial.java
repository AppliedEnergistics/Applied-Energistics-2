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

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import com.google.common.collect.ImmutableSet;

import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.client.texture.MissingIcon;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.features.IStackSrc;
import appeng.core.features.MaterialStackSrc;
import appeng.core.features.NameResolver;
import appeng.items.AEBaseItem;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;


public class ItemMultiMaterial extends AEBaseItem implements IStorageComponent, IUpgradeModule
{
	public static final int KILO = 1024;
	public static ItemMultiMaterial instance;

	private final Map<Integer, MaterialType> dmgToMaterial = new HashMap<Integer, MaterialType>();
	private final NameResolver nameResolver;

	public ItemMultiMaterial()
	{
		this.nameResolver = new NameResolver( this.getClass() );
		this.setFeature( EnumSet.of( AEFeature.Core ) );
		this.setHasSubtypes( true );
		instance = this;
	}

	@Override
	public void addCheckedInformation( ItemStack stack, EntityPlayer player, List<String> lines, boolean displayAdditionalInformation )
	{
		super.addCheckedInformation( stack, player, lines, displayAdditionalInformation );

		MaterialType mt = this.getTypeByStack( stack );
		if ( mt == null )
			return;

		if ( mt == MaterialType.NamePress )
		{
			NBTTagCompound c = Platform.openNbtData( stack );
			lines.add( c.getString( "InscribeName" ) );
		}

		Upgrades u = this.getType( stack );
		if ( u != null )
		{
			List<String> textList = new LinkedList<String>();
			for ( Entry<ItemStack, Integer> j : u.getSupported().entrySet() )
			{
				String name = null;

				int limit = j.getValue();

				if ( j.getKey().getItem() instanceof IItemGroup )
				{
					IItemGroup ig = (IItemGroup) j.getKey().getItem();
					String str = ig.getUnlocalizedGroupName( u.getSupported().keySet(), j.getKey() );
					if ( str != null )
						name = Platform.gui_localize( str ) + ( limit > 1 ? " (" + limit + ')' : "" );
				}

				if ( name == null )
					name = j.getKey().getDisplayName() + ( limit > 1 ? " (" + limit + ')' : "" );

				if ( !textList.contains( name ) )
					textList.add( name );
			}

			Pattern p = Pattern.compile( "(\\d+)[^\\d]" );
			SlightlyBetterSort s = new SlightlyBetterSort( p );
			Collections.sort( textList, s );
			lines.addAll( textList );
		}
	}

	public MaterialType getTypeByStack( ItemStack is )
	{
		if ( this.dmgToMaterial.containsKey( is.getItemDamage() ) )
			return this.dmgToMaterial.get( is.getItemDamage() );
		return MaterialType.InvalidType;
	}

	@Override
	public Upgrades getType( ItemStack itemstack )
	{
		switch ( this.getTypeByStack( itemstack ) )
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

	public IStackSrc createMaterial( MaterialType mat )
	{
		if ( !mat.isRegistered() )
		{
			boolean enabled = true;
			for ( AEFeature f : mat.getFeature() )
				enabled = enabled && AEConfig.instance.isFeatureEnabled( f );

			if ( enabled )
			{
				mat.itemInstance = this;
				int newMaterialNum = mat.damageValue;
				mat.markReady();

				mat.stackSrc = new MaterialStackSrc( mat );

				if ( this.dmgToMaterial.get( newMaterialNum ) == null )
					this.dmgToMaterial.put( newMaterialNum, mat );
				else
					throw new RuntimeException( "Meta Overlap detected." );

				return mat.stackSrc;
			}

			return mat.stackSrc;
		}
		else
			throw new RuntimeException( "Cannot create the same material twice..." );
	}

	public void makeUnique()
	{
		for ( MaterialType mt : ImmutableSet.copyOf( this.dmgToMaterial.values() ) )
		{
			if ( mt.getOreName() != null )
			{
				ItemStack replacement = null;

				String[] names = mt.getOreName().split( "," );

				for ( String name : names )
				{
					if ( replacement != null )
						break;

					List<ItemStack> options = OreDictionary.getOres( name );
					if ( options != null && options.size() > 0 )
					{
						for ( ItemStack is : options )
						{
							if ( is != null && is.getItem() != null )
							{
								replacement = is.copy();
								break;
							}
						}
					}
				}

				if ( replacement == null || AEConfig.instance.useAEVersion( mt ) )
				{
					// continue using the AE2 item.
					for ( String name : names )
						OreDictionary.registerOre( name, mt.stack( 1 ) );
				}
				else
				{
					if ( mt.itemInstance == this )
						this.dmgToMaterial.remove( mt.damageValue );

					mt.itemInstance = replacement.getItem();
					mt.damageValue = replacement.getItemDamage();
				}
			}
		}
	}

	@Override
	public IIcon getIconFromDamage( int dmg )
	{
		if ( this.dmgToMaterial.containsKey( dmg ) )
			return this.dmgToMaterial.get( dmg ).IIcon;
		return new MissingIcon( this );
	}

	@Override
	public String getUnlocalizedName( ItemStack is )
	{
		return "item.appliedenergistics2." + this.nameOf( is );
	}

	private String nameOf( ItemStack is )
	{
		if ( is == null )
			return "null";

		MaterialType mt = this.getTypeByStack( is );
		if ( mt == null )
			return "null";

		return this.nameResolver.getName( mt.name() );
	}

	@Override
	public void getSubItems( Item par1, CreativeTabs par2CreativeTabs, List cList )
	{
		List<MaterialType> types = Arrays.asList( MaterialType.values() );
		Collections.sort( types, new Comparator<MaterialType>()
		{

			@Override
			public int compare( MaterialType o1, MaterialType o2 )
			{
				return o1.name().compareTo( o2.name() );
			}
		} );

		for ( MaterialType mat : types )
		{
			if ( mat.damageValue >= 0 && mat.isRegistered() && mat.itemInstance == this )
				cList.add( new ItemStack( this, 1, mat.damageValue ) );
		}
	}

	@Override
	public void registerIcons( IIconRegister icoRegister )
	{
		for ( MaterialType mat : MaterialType.values() )
		{
			if ( mat.damageValue != -1 )
			{
				ItemStack what = new ItemStack( this, 1, mat.damageValue );
				if ( this.getTypeByStack( what ) != MaterialType.InvalidType )
				{
					String tex = "appliedenergistics2:" + this.nameOf( what );
					mat.IIcon = icoRegister.registerIcon( tex );
				}
			}
		}
	}

	@Override
	public boolean onItemUseFirst( ItemStack is, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ )
	{
		if ( player.isSneaking() )
		{
			TileEntity te = world.getTileEntity( x, y, z );
			IInventory upgrades = null;

			if ( te instanceof IPartHost )
			{
				SelectedPart sp = ( (IPartHost) te ).selectPart( Vec3.createVectorHelper( hitX, hitY, hitZ ) );
				if ( sp.part instanceof IUpgradeableHost )
					upgrades = ( (ISegmentedInventory) sp.part ).getInventoryByName( "upgrades" );
			}
			else if ( te instanceof IUpgradeableHost )
				upgrades = ( (ISegmentedInventory) te ).getInventoryByName( "upgrades" );

			if ( upgrades != null && is != null && is.getItem() instanceof IUpgradeModule )
			{
				IUpgradeModule um = (IUpgradeModule) is.getItem();
				Upgrades u = um.getType( is );

				if ( u != null )
				{
					InventoryAdaptor ad = InventoryAdaptor.getAdaptor( upgrades, ForgeDirection.UNKNOWN );
					if ( ad != null )
					{
						if ( player.worldObj.isRemote )
							return false;

						player.inventory.setInventorySlotContents( player.inventory.currentItem, ad.addItems( is ) );
						return true;
					}
				}
			}
		}

		return super.onItemUseFirst( is, player, world, x, y, z, side, hitX, hitY, hitZ );
	}

	@Override
	public boolean hasCustomEntity( ItemStack is )
	{
		return this.getTypeByStack( is ).hasCustomEntity();
	}

	@Override
	public Entity createEntity( World w, Entity location, ItemStack itemstack )
	{
		Class<? extends Entity> droppedEntity = this.getTypeByStack( itemstack ).getCustomEntityClass();
		Entity eqi;

		try
		{
			eqi = droppedEntity.getConstructor( World.class, double.class, double.class, double.class, ItemStack.class ).newInstance( w, location.posX, location.posY, location.posZ, itemstack );
		}
		catch ( Throwable t )
		{
			throw new RuntimeException( t );
		}

		eqi.motionX = location.motionX;
		eqi.motionY = location.motionY;
		eqi.motionZ = location.motionZ;

		if ( location instanceof EntityItem && eqi instanceof EntityItem )
			( (EntityItem) eqi ).delayBeforeCanPickup = ( (EntityItem) location ).delayBeforeCanPickup;

		return eqi;
	}

	@Override
	public int getBytes( ItemStack is )
	{
		switch ( this.getTypeByStack( is ) )
		{
			case Cell1kPart:
				return KILO;
			case Cell4kPart:
				return KILO * 4;
			case Cell16kPart:
				return KILO * 16;
			case Cell64kPart:
				return KILO * 64;
			default:
		}
		return 0;
	}

	@Override
	public boolean isStorageComponent( ItemStack is )
	{
		switch ( this.getTypeByStack( is ) )
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

		public SlightlyBetterSort( Pattern pattern )
		{
			this.pattern = pattern;
		}

		@Override
		public int compare( String o1, String o2 )
		{
			try
			{
				Matcher a = this.pattern.matcher( o1 );
				Matcher b = this.pattern.matcher( o2 );
				if ( a.find() && b.find() )
				{
					int ia = Integer.parseInt( a.group( 1 ) );
					int ib = Integer.parseInt( b.group( 1 ) );
					return Integer.compare( ia, ib );
				}
			}
			catch ( Throwable t )
			{
				// ek!
			}
			return o1.compareTo( o2 );
		}
	}
}
