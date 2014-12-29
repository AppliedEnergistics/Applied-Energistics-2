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

package appeng.core.features;


import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.common.registry.GameRegistry;

import appeng.api.util.AEItemDefinition;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseItemBlock;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import appeng.core.CreativeTab;
import appeng.core.CreativeTabFacade;
import appeng.items.parts.ItemFacade;
import appeng.util.Platform;


public class AEFeatureHandler implements AEItemDefinition
{

	private final EnumSet<AEFeature> features;

	private final String subName;
	private final IAEFeature feature;

	private Item ItemData;
	private Block BlockData;
	private BlockStairs stairData;

	public AEFeatureHandler( EnumSet<AEFeature> features, IAEFeature feature, String subName )
	{
		this.features = features;
		this.feature = feature;
		this.subName = subName;
	}

	public void register()
	{
		if ( this.isFeatureAvailable() )
		{
			if ( this.feature instanceof Item )
			{
				this.initItem( ( Item ) this.feature );
			}
			else if ( this.feature instanceof BlockStairs )
			{
				this.initStairBlock( ( BlockStairs ) this.feature );
			}
			else if ( this.feature instanceof Block )
			{
				this.initBlock( ( Block ) this.feature );
			}
		}
	}

	public boolean isFeatureAvailable()
	{
		boolean enabled = true;

		for ( AEFeature f : this.features )
			enabled = enabled && AEConfig.instance.isFeatureEnabled( f );

		return enabled;
	}

	private void initItem( Item i )
	{
		this.ItemData = i;

		String name = getName( i.getClass(), this.subName );
		i.setTextureName( "appliedenergistics2:" + name );
		i.setUnlocalizedName( /* "item." */"appliedenergistics2." + name );

		if ( i instanceof ItemFacade )
			i.setCreativeTab( CreativeTabFacade.instance );
		else
			i.setCreativeTab( CreativeTab.instance );

		if ( name.equals( "ItemMaterial" ) )
			name = "ItemMultiMaterial";
		else if ( name.equals( "ItemPart" ) )
			name = "ItemMultiPart";

		GameRegistry.registerItem( i, "item." + name );
	}

	private void initStairBlock( BlockStairs stair )
	{
		this.stairData = stair;

		String name = getName( stair.getClass(), this.subName );
		stair.setCreativeTab( CreativeTab.instance );
		stair.setBlockName( /* "tile." */"appliedenergistics2." + name );
		stair.setBlockTextureName( "appliedenergistics2:" + name );

		GameRegistry.registerBlock( stair, "tile." + name );
	}

	private void initBlock( Block b )
	{
		this.BlockData = b;

		String name = getName( b.getClass(), this.subName );
		b.setCreativeTab( CreativeTab.instance );
		b.setBlockName( /* "tile." */"appliedenergistics2." + name );
		b.setBlockTextureName( "appliedenergistics2:" + name );

		if ( Platform.isClient() && this.BlockData instanceof AEBaseBlock )
		{
			AEBaseBlock bb = ( AEBaseBlock ) b;
			CommonHelper.proxy.bindTileEntitySpecialRenderer( bb.getTileEntityClass(), bb );
		}

		Class<? extends AEBaseItemBlock> itemBlock = AEBaseItemBlock.class;
		if ( b instanceof AEBaseBlock )
			itemBlock = ( ( AEBaseBlock ) b ).getItemBlockClass();

		GameRegistry.registerBlock( b, itemBlock, "tile." + name );
	}

	public static String getName( Class o, String subName )
	{
		String name = o.getSimpleName();

		if ( name.startsWith( "ItemMultiPart" ) )
			name = name.replace( "ItemMultiPart", "ItemPart" );
		else if ( name.startsWith( "ItemMultiMaterial" ) )
			name = name.replace( "ItemMultiMaterial", "ItemMaterial" );

		if ( subName != null )
		{
			// simple hack to allow me to do get nice names for these without
			// mode code outside of AEBaseItem
			if ( subName.startsWith( "P2PTunnel" ) )
				return "ItemPart.P2PTunnel";

			if ( subName.equals( "CertusQuartzTools" ) )
				return name.replace( "Quartz", "CertusQuartz" );
			if ( subName.equals( "NetherQuartzTools" ) )
				return name.replace( "Quartz", "NetherQuartz" );

			name += '.' + subName;
		}

		return name;
	}

	public EnumSet<AEFeature> getFeatures()
	{
		return this.features.clone();
	}

	@Override
	public Block block()
	{
		return this.BlockData;
	}

	@Override
	public Item item()
	{
		if ( this.ItemData != null )
		{
			return this.ItemData;
		}
		else if ( this.BlockData != null )
		{
			return Item.getItemFromBlock( this.BlockData );
		}
		else if ( this.stairData != null )
		{
			return Item.getItemFromBlock( this.stairData );
		}

		return null;
	}

	@Override
	public Class<? extends TileEntity> entity()
	{
		if ( this.BlockData instanceof AEBaseBlock )
		{
			AEBaseBlock bb = ( AEBaseBlock ) this.BlockData;
			return bb.getTileEntityClass();
		}

		return null;
	}

	@Override
	public ItemStack stack( int stackSize )
	{
		if ( this.isFeatureAvailable() )
		{
			ItemStack rv;

			if ( this.ItemData != null )
				rv = new ItemStack( this.ItemData );
			else
				rv = new ItemStack( this.BlockData );

			rv.stackSize = stackSize;
			return rv;
		}

		return null;
	}

	@Override
	public boolean sameAsStack( ItemStack is )
	{
		return this.isFeatureAvailable() && Platform.isSameItemType( is, this.stack( 1 ) );
	}

	/**
	 * block at xyz in world is same if:
	 *
	 * - the feature is available
	 * - the stored block data is not null
	 * - and the stored block data is equal
	 *
	 * @param world world of block
	 * @param x x pos of block
	 * @param y y pos of block
	 * @param z z pos of block
	 *
	 * @return true if feature is available and the blocks are equal
	 */
	@Override
	public boolean sameAsBlock( IBlockAccess world, int x, int y, int z )
	{
		return this.isFeatureAvailable() && this.BlockData != null && world.getBlock( x, y, z ) == this.BlockData;
	}

}
