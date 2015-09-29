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


import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import appeng.decorative.solid.QuartzOreBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.ModelGenerator;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.AEBaseTile;


public class RenderQuartzOre extends BaseBlockRender<QuartzOreBlock, AEBaseTile>
{

	public RenderQuartzOre()
	{
		super( false, 20 );
	}

	@Override
	public void renderInventory( QuartzOreBlock blk, ItemStack is, ModelGenerator renderer, ItemRenderType type, Object[] obj )
	{
		super.renderInventory( blk, is, renderer, type, obj );
		blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.OreQuartzStone.getIcon() );
		super.renderInventory( blk, is, renderer, type, obj );
		blk.getRendererInstance().setTemporaryRenderIcon( null );
	}

	@Override
	public boolean renderInWorld( QuartzOreBlock block, IBlockAccess world, BlockPos pos, ModelGenerator renderer )
	{
		QuartzOreBlock blk = block;
		blk.setEnhanceBrightness( true );
		super.renderInWorld( block, world, pos, renderer );
		blk.setEnhanceBrightness( false );

		blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.OreQuartzStone.getIcon() );
		boolean out = super.renderInWorld( block, world, pos, renderer );
		blk.getRendererInstance().setTemporaryRenderIcon( null );

		return out;
	}
}
