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

	public static final WorldRender INSTANCE = new WorldRender();
	public final HashMap<AEBaseBlock, BaseBlockRender> blockRenders = new HashMap<AEBaseBlock, BaseBlockRender>();
	private final IRenderHelper renderer = new IRenderHelper();
	boolean hasError = false;

	private WorldRender()
	{
	}

	void setRender( AEBaseBlock in, BaseBlockRender r )
	{
		this.blockRenders.put( in, r );
	}

	@Override
	public void renderInventoryBlock( Block block, int metadata, int modelID, IRenderHelper renderer )
	{
		// wtf is this for?
	}

	@Override
	public boolean renderWorldBlock( IBlockAccess world, BlockPos pos, Block block, int modelId, IRenderHelper renderer )
	{
		AEBaseBlock blk = (AEBaseBlock) block;
		renderer.setRenderBoundsFromBlock( block );
		return this.getRender( blk ).renderInWorld( blk, world, pos, renderer );
	}

	private BaseBlockRender getRender( AEBaseBlock block )
	{
		return block.getRendererInstance().rendererInstance;
	}

	public void renderItemBlock( ItemStack item, ItemRenderType type, Object[] data )
	{
		Block blk = Block.getBlockFromItem( item.getItem() );
		if( blk instanceof AEBaseBlock )
		{
			AEBaseBlock block = (AEBaseBlock) blk;
			this.renderer.setRenderBoundsFromBlock( block );

			this.renderer.uvRotateBottom = this.renderer.uvRotateEast = this.renderer.uvRotateNorth = this.renderer.uvRotateSouth = this.renderer.uvRotateTop = this.renderer.uvRotateWest = 0;
			this.getRender( block ).renderInventory( block, item, this.renderer, type, data );
			this.renderer.uvRotateBottom = this.renderer.uvRotateEast = this.renderer.uvRotateNorth = this.renderer.uvRotateSouth = this.renderer.uvRotateTop = this.renderer.uvRotateWest = 0;
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
