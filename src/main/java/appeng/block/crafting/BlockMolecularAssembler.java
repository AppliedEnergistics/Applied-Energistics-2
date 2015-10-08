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

package appeng.block.crafting;


import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockAssembler;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.crafting.TileMolecularAssembler;
import appeng.util.Platform;


public class BlockMolecularAssembler extends AEBaseTileBlock
{

	public BlockMolecularAssembler()
	{
		super( Material.iron );

		this.setTileEntity( TileMolecularAssembler.class );
		this.setOpaque( false );
		this.lightOpacity = 1;
		this.setFeature( EnumSet.of( AEFeature.MolecularAssembler ) );
	}

	@Override
	public boolean canRenderInLayer( final net.minecraft.util.EnumWorldBlockLayer layer )
	{
		return layer == EnumWorldBlockLayer.CUTOUT_MIPPED;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockAssembler.class;
	}

	@Override
	public boolean onBlockActivated(
			final World w,
			final BlockPos pos,
			final IBlockState state,
			final EntityPlayer p,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		final TileMolecularAssembler tg = this.getTileEntity( w, pos );
		if( tg != null && !p.isSneaking() )
		{
			Platform.openGUI( p, tg, AEPartLocation.fromFacing( side ), GuiBridge.GUI_MAC );
			return true;
		}
		return false;
	}
}
