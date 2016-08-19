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

package appeng.decorative.solid;


import java.util.EnumSet;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.AEApi;
import appeng.api.exceptions.MissingDefinition;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;


public class BlockQuartzOre extends AEBaseBlock
{
	private int boostBrightnessLow;
	private int boostBrightnessHigh;
	private boolean enhanceBrightness;

	public BlockQuartzOre()
	{
		super( Material.ROCK );
		this.setHardness( 3.0F );
		this.setResistance( 5.0F );
		this.boostBrightnessLow = 0;
		this.boostBrightnessHigh = 1;
		this.enhanceBrightness = false;
		this.setFeature( EnumSet.of( AEFeature.Core ) );
	}

	@Override
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public void postInit()
	{
		OreDictionary.registerOre( "oreCertusQuartz", new ItemStack( this ) );
	}

	@Override
	public int getLightValue( final IBlockState state, final IBlockAccess worldIn, final BlockPos pos )
	{
		int j1 = super.getLightValue( state, worldIn, pos );
		if( this.enhanceBrightness )
		{
			j1 = Math.max( j1 >> 20, j1 >> 4 );

			if( j1 > 4 )
			{
				j1 += this.boostBrightnessHigh;
			}
			else
			{
				j1 += this.boostBrightnessLow;
			}

			if( j1 > 15 )
			{
				j1 = 15;
			}
			return j1 << 20 | j1 << 4;
		}
		return j1;
	}

	@Override
	public int quantityDropped( final Random rand )
	{
		return 1 + rand.nextInt( 2 );
	}

	@Override
	public Item getItemDropped( final IBlockState state, /* is null */
			final Random rand, final int fortune )
	{
		for( final Item crystalItem : AEApi.instance().definitions().materials().certusQuartzCrystal().maybeItem().asSet() )
		{
			return crystalItem;
		}

		throw new MissingDefinition( "Tried to access certus quartz crystal, even though they are disabled" );
	}

	@Override
	public void dropBlockAsItemWithChance( final World w, final BlockPos pos, final IBlockState state, final float chance, final int fortune )
	{
		super.dropBlockAsItemWithChance( w, pos, state, chance, fortune );

		if( this.getItemDropped( state, w.rand, fortune ) != Item.getItemFromBlock( this ) )
		{
			final int xp = MathHelper.getRandomIntegerInRange( w.rand, 2, 5 );

			this.dropXpOnBlockBreak( w, pos, xp );
		}
	}

	@Override
	public int damageDropped( final IBlockState state )
	{
		for( final ItemStack crystalStack : AEApi.instance().definitions().materials().certusQuartzCrystal().maybeStack( 1 ).asSet() )
		{
			return crystalStack.getItemDamage();
		}

		throw new MissingDefinition( "Tried to access certus quartz crystal, even though they are disabled" );
	}

	@Override
	public int quantityDroppedWithBonus( final int fortune, final Random rand )
	{
		if( fortune > 0 && Item.getItemFromBlock( this ) != this.getItemDropped( null, rand, fortune ) )
		{
			int j = rand.nextInt( fortune + 2 ) - 1;

			if( j < 0 )
			{
				j = 0;
			}

			return this.quantityDropped( rand ) * ( j + 1 );
		}
		else
		{
			return this.quantityDropped( rand );
		}
	}

	void setBoostBrightnessLow( final int boostBrightnessLow )
	{
		this.boostBrightnessLow = boostBrightnessLow;
	}

	void setBoostBrightnessHigh( final int boostBrightnessHigh )
	{
		this.boostBrightnessHigh = boostBrightnessHigh;
	}

	public void setEnhanceBrightness( final boolean enhanceBrightness )
	{
		this.enhanceBrightness = enhanceBrightness;
	}
}
