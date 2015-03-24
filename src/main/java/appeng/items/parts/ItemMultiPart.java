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

package appeng.items.parts;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.core.features.ItemStackSrc;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;


public class ItemMultiPart extends AEBaseItem implements IPartItem, IItemGroup
{

	public static ItemMultiPart instance;
	final HashMap<Integer, PartTypeIst> dmgToPart = new HashMap<Integer, PartTypeIst>();

	public ItemMultiPart()
	{
		super( ItemMultiPart.class );
		this.setFeature( EnumSet.of( AEFeature.Core ) );
		AEApi.instance().partHelper().setItemBusRenderer( this );
		this.setHasSubtypes( true );
		instance = this;
	}

	public ItemStackSrc createPart( PartType mat, Enum variant )
	{
		try
		{
			// I think this still works?
			ItemStack is = new ItemStack( this );
			mat.getPart().getConstructor( ItemStack.class ).newInstance( is );
		}
		catch( Throwable e )
		{
			AELog.integration( e );
			return null; // part not supported..
		}

		int varID = variant == null ? 0 : variant.ordinal();

		// verify
		for( PartTypeIst p : this.dmgToPart.values() )
		{
			if( p.part == mat && p.variant == varID )
				throw new RuntimeException( "Cannot create the same material twice..." );
		}

		boolean enabled = true;
		for( AEFeature f : mat.getFeature() )
			enabled = enabled && AEConfig.instance.isFeatureEnabled( f );

		if( enabled )
		{
			int newPartNum = mat.baseDamage + varID;
			ItemStackSrc output = new ItemStackSrc( this, newPartNum );

			PartTypeIst pti = new PartTypeIst();
			pti.part = mat;
			pti.variant = varID;

			if( this.dmgToPart.get( newPartNum ) == null )
			{
				this.dmgToPart.put( newPartNum, pti );
				return output;
			}
			else
				throw new RuntimeException( "Meta Overlap detected." );
		}

		return null;
	}

	public int getDamageByType( PartType t )
	{
		for( Entry<Integer, PartTypeIst> pt : this.dmgToPart.entrySet() )
		{
			if( pt.getValue().part == t )
				return pt.getKey();
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
	public IIcon getIconFromDamage( int dmg )
	{
		return this.dmgToPart.get( dmg ).ico;
	}

	@Override
	public boolean onItemUse( ItemStack is, EntityPlayer player, World w, int x, int y, int z, int side, float hitX, float hitY, float hitZ )
	{
		return AEApi.instance().partHelper().placeBus( is, x, y, z, side, player, w );
	}

	@Override
	public String getUnlocalizedName( ItemStack is )
	{
		return "item.appliedenergistics2." + this.getName( is );
	}

	public String getName( ItemStack is )
	{
		return AEFeatureHandler.getName( ItemMultiPart.class, this.getTypeByStack( is ).name() );
	}

	public PartType getTypeByStack( ItemStack is )
	{
		if( is == null )
			return null;

		PartTypeIst pt = this.dmgToPart.get( is.getItemDamage() );
		if( pt != null )
			return pt.part;

		return null;
	}

	@Override
	public String getItemStackDisplayName( ItemStack is )
	{
		PartType pt = this.getTypeByStack( is );
		if( pt == null )
			return "Unnamed";

		Enum[] variants = pt.getVariants();

		if( variants != null )
			return super.getItemStackDisplayName( is ) + " - " + variants[this.dmgToPart.get( is.getItemDamage() ).variant].toString();

		if( pt.getExtraName() != null )
			return super.getItemStackDisplayName( is ) + " - " + pt.getExtraName().getLocal();

		return super.getItemStackDisplayName( is );
	}

	@Override
	public void getSubItems( Item number, CreativeTabs tab, List cList )
	{
		List<Entry<Integer, PartTypeIst>> types = new ArrayList<Entry<Integer, PartTypeIst>>( this.dmgToPart.entrySet() );
		Collections.sort( types, new Comparator<Entry<Integer, PartTypeIst>>()
		{

			@Override
			public int compare( Entry<Integer, PartTypeIst> o1, Entry<Integer, PartTypeIst> o2 )
			{
				return o1.getValue().part.name().compareTo( o2.getValue().part.name() );
			}
		} );

		for( Entry<Integer, PartTypeIst> part : types )
			cList.add( new ItemStack( this, 1, part.getKey() ) );
	}

	@Override
	public void registerIcons( IIconRegister par1IconRegister )
	{
		for( Entry<Integer, PartTypeIst> part : this.dmgToPart.entrySet() )
		{
			String tex = "appliedenergistics2:" + this.getName( new ItemStack( this, 1, part.getKey() ) );
			part.getValue().ico = par1IconRegister.registerIcon( tex );
		}
	}

	@Override
	public IPart createPartFromItemStack( ItemStack is )
	{
		try
		{
			PartType t = this.getTypeByStack( is );
			if( t != null )
			{
				if( t.constructor == null )
					t.constructor = t.getPart().getConstructor( ItemStack.class );

				return t.constructor.newInstance( is );
			}
		}
		catch( Throwable e )
		{
			throw new RuntimeException( "Unable to construct IBusPart from IBusItem : " + this.getTypeByStack( is ).getPart().getName() + " ; Possibly didn't have correct constructor( ItemStack )", e );
		}
		return null;
	}

	public int variantOf( int itemDamage )
	{
		if( this.dmgToPart.containsKey( itemDamage ) )
			return this.dmgToPart.get( itemDamage ).variant;
		return 0;
	}

	@Override
	public String getUnlocalizedGroupName( Set<ItemStack> others, ItemStack is )
	{
		boolean importBus = false, exportBus = false, group = false;

		PartType u = this.getTypeByStack( is );

		for( ItemStack stack : others )
		{
			if( stack.getItem() == this )
			{
				PartType pt = this.getTypeByStack( stack );
				switch( pt )
				{
					case ImportBus:
						importBus = true;
						if( u == pt )
							group = true;
						break;
					case ExportBus:
						exportBus = true;
						if( u == pt )
							group = true;
						break;
					default:
				}
			}
		}

		if( group && importBus && exportBus )
			return GuiText.IOBuses.getUnlocalized();

		return null;
	}

	public ItemStack getStackFromTypeAndVariant( PartType mt, int variant )
	{
		return new ItemStack( this, 1, mt.baseDamage + variant );
	}


	static class PartTypeIst
	{

		PartType part;
		int variant;

		@SideOnly( Side.CLIENT )
		IIcon ico;
	}
}
