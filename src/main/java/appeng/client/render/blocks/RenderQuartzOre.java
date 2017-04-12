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


import appeng.block.solids.OreQuartz;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.AEBaseTile;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;


public class RenderQuartzOre extends BaseBlockRender<OreQuartz, AEBaseTile>
{

	public RenderQuartzOre()
	{
		super( false, 20 );
	}

	@Override
	public void renderInventory( final OreQuartz blk, final ItemStack is, final RenderBlocks renderer, final ItemRenderType type, final Object[] obj )
	{
		super.renderInventory( blk, is, renderer, type, obj );
		blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.OreQuartzStone.getIcon() );
		super.renderInventory( blk, is, renderer, type, obj );
		blk.getRendererInstance().setTemporaryRenderIcon( null );
	}

	@Override
	public boolean renderInWorld( final OreQuartz quartz, final IBlockAccess world, final int x, final int y, final int z, final RenderBlocks renderer )
	{
		quartz.setEnhanceBrightness( true );
		super.renderInWorld( quartz, world, x, y, z, renderer );
		quartz.setEnhanceBrightness( false );

		quartz.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.OreQuartzStone.getIcon() );
		final boolean out = super.renderInWorld( quartz, world, x, y, z, renderer );
		quartz.getRendererInstance().setTemporaryRenderIcon( null );

		return out;
	}
}
