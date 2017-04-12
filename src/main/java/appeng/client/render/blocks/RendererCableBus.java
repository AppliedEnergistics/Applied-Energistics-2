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

package appeng.client.render.blocks;


import appeng.block.networking.BlockCableBus;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BusRenderHelper;
import appeng.client.render.BusRenderer;
import appeng.tile.AEBaseTile;
import appeng.tile.networking.TileCableBus;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;


public class RendererCableBus extends BaseBlockRender<BlockCableBus, TileCableBus>
{

	public RendererCableBus()
	{
		super( true, 30 );
	}

	@Override
	public void renderInventory( final BlockCableBus blk, final ItemStack is, final RenderBlocks renderer, final ItemRenderType type, final Object[] obj )
	{
		// nothing.
	}

	@Override
	public boolean renderInWorld( final BlockCableBus block, final IBlockAccess world, final int x, final int y, final int z, final RenderBlocks renderer )
	{
		final AEBaseTile t = block.getTileEntity( world, x, y, z );

		if( t instanceof TileCableBus )
		{
			BusRenderer.INSTANCE.getRenderer().renderAllFaces = true;
			BusRenderer.INSTANCE.getRenderer().blockAccess = renderer.blockAccess;
			BusRenderer.INSTANCE.getRenderer().overrideBlockTexture = renderer.overrideBlockTexture;
			( (TileCableBus) t ).getCableBus().renderStatic( x, y, z );
			BusRenderer.INSTANCE.getRenderer().renderAllFaces = false;
		}

		return BusRenderHelper.INSTANCE.getItemsRendered() > 0;
	}

	@Override
	public void renderTile( final BlockCableBus block, final TileCableBus cableBus, final Tessellator tess, final double x, final double y, final double z, final float f, final RenderBlocks renderer )
	{
		if( cableBus != null )
		{
			BusRenderer.INSTANCE.getRenderer().overrideBlockTexture = null;
			cableBus.getCableBus().renderDynamic( x, y, z );
		}
	}
}
