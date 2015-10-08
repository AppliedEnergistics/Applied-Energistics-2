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

package appeng.client.render;


import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import appeng.block.AEBaseBlock;
import appeng.core.AELog;


@SideOnly( Side.CLIENT )
public final class WorldRender implements ISimpleBlockRenderingHandler
{

	private static final WorldRender INSTANCE = new WorldRender();
	private final HashMap<AEBaseBlock, BaseBlockRender> blockRenders = new HashMap<AEBaseBlock, BaseBlockRender>();
	private final ModelGenerator renderer = new ModelGenerator();
	private boolean hasError = false;

	private WorldRender()
	{
	}

	void setRender( final AEBaseBlock in, final BaseBlockRender r )
	{
		this.blockRenders.put( in, r );
	}

	@Override
	public void renderInventoryBlock( final Block block, final int metadata, final int modelID, final ModelGenerator renderer )
	{
		// wtf is this for?
	}

	@Override
	public boolean renderWorldBlock( final IBlockAccess world, final BlockPos pos, final Block block, final int modelId, final ModelGenerator renderer )
	{
		final AEBaseBlock blk = (AEBaseBlock) block;
		renderer.setRenderBoundsFromBlock( block );
		return this.getRender( blk ).renderInWorld( blk, world, pos, renderer );
	}

	private BaseBlockRender getRender( final AEBaseBlock block )
	{
		return block.getRendererInstance().getRendererInstance();
	}

	void renderItemBlock( final ItemStack item, final ItemRenderType type, final Object[] data )
	{
		final Block blk = Block.getBlockFromItem( item.getItem() );
		if( blk instanceof AEBaseBlock )
		{
			final AEBaseBlock block = (AEBaseBlock) blk;
			this.renderer.setRenderBoundsFromBlock( block );

			this.renderer.setUvRotateBottom( this.renderer.setUvRotateEast( this.renderer.setUvRotateNorth( this.renderer.setUvRotateSouth( this.renderer.setUvRotateTop( this.renderer.setUvRotateWest( 0 ) ) ) ) ) );
			this.getRender( block ).renderInventory( block, item, this.renderer, type, data );
			this.renderer.setUvRotateBottom( this.renderer.setUvRotateEast( this.renderer.setUvRotateNorth( this.renderer.setUvRotateSouth( this.renderer.setUvRotateTop( this.renderer.setUvRotateWest( 0 ) ) ) ) ) );
		}
		else
		{
			if( !this.hasError )
			{
				this.hasError = true;
				AELog.severe( "Invalid render - item/block mismatch" );
				AELog.severe( "		item: " + item.getUnlocalizedName() );
				AELog.severe( "		block: " + blk.getUnlocalizedName() );
			}
		}
	}
}
