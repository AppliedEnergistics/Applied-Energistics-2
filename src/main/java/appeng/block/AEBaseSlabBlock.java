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


import java.util.EnumSet;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import appeng.core.features.AEFeature;
import appeng.core.features.IAEFeature;
import appeng.core.features.IFeatureHandler;
import appeng.core.features.SlabBlockFeatureHandler;


public class AEBaseSlabBlock extends BlockSlab implements IAEFeature
{
	private final IFeatureHandler features;
	public AEBaseBlock block;
	public int meta;
	public boolean isDoubleSlab;
	public AEBaseSlabBlock slabs;
	public AEBaseSlabBlock dSlabs;
	public final String name;

	public AEBaseSlabBlock( AEBaseBlock block, int meta, EnumSet<AEFeature> features, boolean isDoubleSlab, String name )
	{
        super( isDoubleSlab, block.getMaterial() );
		this.block = block;
		this.meta = meta;
		this.name = name;
		this.isDoubleSlab = isDoubleSlab;
		this.setBlockName( "appliedenergistics2." + name );
        this.setHardness( block.getBlockHardness( null, 0, 0, 0 ) );
        this.setResistance( block.getExplosionResistance( null ) * 5.0F / 3.0F );
        this.setStepSound( block.stepSound );
        this.useNeighborBrightness = true;
		if (!isDoubleSlab)
		{
			this.dSlabs = new AEBaseSlabBlock( block, meta, features, true, name + ".double" ).setSlabs(this);
			this.slabs = this;
		} else {
			this.dSlabs = this;
		}
		this.features = isDoubleSlab ? new SlabBlockFeatureHandler( features, this ) : new SlabBlockFeatureHandler( features, this );
	}

	public AEBaseSlabBlock setSlabs(AEBaseSlabBlock slabs)
	{
		this.slabs = slabs;
		return this;
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
	public IIcon getIcon(int dir, int meta)
	{
		return block.getIcon( dir, this.meta );
	}

	@Override
	public String func_150002_b(int p_150002_1_)
	{
		return this.getUnlocalizedName();
	}

	@Override
	public void registerBlockIcons(IIconRegister reg) { }

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune)
	{
	    return Item.getItemFromBlock(this.slabs);
	}

	@Override
	public int quantityDropped(int meta, int fortune, Random random)
	{
	    return this.field_150004_a ? 2 : 1;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
	    Block block = world.getBlock(x, y, z);

	    if (block == null) return null;
	    if (block == this.dSlabs) block = this.slabs;

	    int meta = world.getBlockMetadata(x, y, z) & 7;
	    return new ItemStack(block, 1, meta);
	}
}
