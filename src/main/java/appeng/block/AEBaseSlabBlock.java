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

package appeng.block;


import appeng.core.features.AEFeature;
import appeng.core.features.IAEFeature;
import appeng.core.features.IFeatureHandler;
import appeng.core.features.SlabBlockFeatureHandler;
import net.minecraft.block.BlockSlab;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;


public class AEBaseSlabBlock extends BlockSlab implements IAEFeature
{
	private final IFeatureHandler features;
	private final AEBaseBlock block;
	private final int meta;
	private AEBaseSlabBlock slabs;
	private AEBaseSlabBlock doubleSlabs;
	private final String name;

	public AEBaseSlabBlock( final AEBaseBlock block, final int meta, final EnumSet<AEFeature> features, final boolean isDoubleSlab, final String name )
	{
		super( isDoubleSlab, block.getMaterial() );
		this.block = block;
		this.meta = meta;
		this.name = name;
		this.setBlockName( "appliedenergistics2." + name );
		this.setHardness( block.getBlockHardness( null, 0, 0, 0 ) );
		this.setResistance( block.getExplosionResistance( null ) * 5.0F / 3.0F );
		this.setStepSound( block.stepSound );
		this.useNeighborBrightness = true;
		if( !this.field_150004_a )
		{
			this.doubleSlabs = new AEBaseSlabBlock( block, meta, features, true, name + ".double" ).setSlabs( this );
		}
		this.features = !this.field_150004_a ? new SlabBlockFeatureHandler( features, this ) : null;
	}

	private AEBaseSlabBlock setSlabs( final AEBaseSlabBlock slabs )
	{
		this.slabs = slabs;
		return this;
	}

	public AEBaseSlabBlock slabs()
	{
		return this.slabs;
	}

	public AEBaseSlabBlock doubleSlabs()
	{
		return this.doubleSlabs;
	}

	@Override
	public IFeatureHandler handler()
	{
		return this.features;
	}

	@Override
	public void postInit()
	{
		// Override to do stuff
	}

	@Override
	public IIcon getIcon( final int dir, final int meta )
	{
		return this.block.getIcon( dir, this.meta );
	}

	@Override
	public String func_150002_b( final int p_150002_1_ )
	{
		return this.getUnlocalizedName();
	}

	@Override
	public void registerBlockIcons( final IIconRegister reg )
	{
	}

	@Override
	public Item getItemDropped( final int meta, final Random rand, final int fortune )
	{
		return this.field_150004_a ? Item.getItemFromBlock( this.slabs ) : Item.getItemFromBlock( this );
	}

	@Override
	public ItemStack getPickBlock( final MovingObjectPosition target, final World world, final int x, final int y, final int z )
	{
		AEBaseSlabBlock block = (AEBaseSlabBlock) world.getBlock( x, y, z );

		if( block == null )
		{
			return null;
		}
		if( block.field_150004_a )
		{
			block = this.slabs;
		}

		final int meta = world.getBlockMetadata( x, y, z ) & 7;
		return new ItemStack( block, 1, meta );
	}

	public String name()
	{
		return this.name;
	}
}
