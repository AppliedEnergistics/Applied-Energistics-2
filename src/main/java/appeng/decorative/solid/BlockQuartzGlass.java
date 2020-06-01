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

import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelProperty;

import appeng.block.AEBaseBlock;

public class BlockQuartzGlass extends AEBaseBlock
{

	public BlockQuartzGlass()
	{
		super( Properties.create(Material.GLASS) );
		// FIXME this.setLightOpacity( 0 );
		this.setOpaque( false );
	}

//	@Override
//	protected BlockStateContainer createBlockState()
//	{
//		IProperty[] listedProperties = new IProperty[0];
//		IUnlistedProperty[] unlistedProperties = new IUnlistedProperty[] { GLASS_STATE };
//		return new ExtendedBlockState( this, listedProperties, unlistedProperties );
//	}

// FIXME	@Override
// FIXME	public BlockRenderLayer getBlockLayer()
// FIXME	{
// FIXME		return BlockRenderLayer.CUTOUT;
// FIXME	}

// FIXME	@Override
// FIXME	public boolean shouldSideBeRendered( final BlockState state, final IBlockReader w, final BlockPos pos, final Direction side )
// FIXME	{
// FIXME		BlockPos adjacentPos = pos.offset( side );
// FIXME
// FIXME		final Material mat = w.getBlockState( adjacentPos ).getMaterial();
// FIXME
// FIXME		if( mat == Material.GLASS || mat == AEGlassMaterial.INSTANCE )
// FIXME		{
// FIXME			if( w.getBlockState( adjacentPos ).getRenderType() == this.getRenderType( state ) )
// FIXME			{
// FIXME				return false;
// FIXME			}
// FIXME		}
// FIXME
// FIXME		return super.shouldSideBeRendered( state, w, pos, side );
// FIXME	}

	// BEGIN: Copied from AbstractGlassBlock
	@OnlyIn(Dist.CLIENT)
	public float getAmbientOcclusionLightValue(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return 1.0F;
	}

	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
		return true;
	}

	public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return false;
	}

	public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return false;
	}

	public boolean canEntitySpawn(BlockState state, IBlockReader worldIn, BlockPos pos, EntityType<?> type) {
		return false;
	}
	// END: Copied from AbstractGlassBlock

}
