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


import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.util.AEPartLocation;
import appeng.block.networking.BlockCableBus;
import appeng.client.ClientHelper;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BusRenderHelper;
import appeng.client.render.BusRenderer;
import appeng.client.render.ModelGenerator;
import appeng.facade.IFacadeItem;
import appeng.tile.AEBaseTile;
import appeng.tile.networking.TileCableBus;
import appeng.util.Platform;


public class RendererCableBus extends BaseBlockRender<BlockCableBus, TileCableBus>
{

	public RendererCableBus()
	{
		super( true, 30 );
	}

	@Override
	public void renderInventory( BlockCableBus blk, ItemStack item, ModelGenerator renderer, ItemRenderType type, Object[] obj )
	{
		renderer.setColorOpaque_F( 1, 1, 1 );
		renderer.setBrightness( 14 << 20 | 14 << 4 );

		BusRenderer.INSTANCE.renderer = renderer; // post data to this...
		BusRenderHelper.INSTANCE.setBounds( 0, 0, 0, 1, 1, 1 );
		BusRenderHelper.INSTANCE.setTexture( null );
		BusRenderHelper.INSTANCE.setInvColor( 0xffffff );
		renderer.blockAccess = ClientHelper.proxy.getWorld();

		BusRenderHelper.INSTANCE.setOrientation( EnumFacing.EAST, EnumFacing.UP, EnumFacing.SOUTH );

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
		renderer.overrideBlockTexture = null;

		if( item.getItem() instanceof IFacadeItem )
		{
			IFacadeItem fi = (IFacadeItem) item.getItem();
			IFacadePart fp = fi.createPartFromItemStack( item, AEPartLocation.SOUTH );

			if( fp != null )
			{
				fp.renderInventory( BusRenderHelper.INSTANCE, renderer );
			}
		}
		else
		{
			IPart ip = this.getRenderer( item, (IPartItem) item.getItem() );
			if( ip != null )
			{
				if( type == ItemRenderType.ENTITY )
				{
					int depth = ip.cableConnectionRenderTo();
				}

				ip.renderInventory( BusRenderHelper.INSTANCE, renderer );
			}
		}

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
	}

	public IPart getRenderer( ItemStack is, IPartItem c )
	{
		int id = ( Item.getIdFromItem( is.getItem() ) << Platform.DEF_OFFSET ) | is.getItemDamage();

		IPart part = RENDER_PART.get( id );
		if( part == null )
		{
			part = c.createPartFromItemStack( is );
			if( part != null )
			{
				RENDER_PART.put( id, part );
			}
		}

		return part;
	}

	public static final BusRenderer INSTANCE = new BusRenderer();
	private static final Map<Integer, IPart> RENDER_PART = new HashMap<Integer, IPart>();
	
	@Override
	public boolean renderInWorld( BlockCableBus block, IBlockAccess world, BlockPos pos, ModelGenerator renderer )
	{
		AEBaseTile t = block.getTileEntity( world, pos );

		if( t instanceof TileCableBus )
		{
			BusRenderer.INSTANCE.renderer = renderer; // post data to this...
			BusRenderer.INSTANCE.renderer.renderAllFaces = true;
			BusRenderer.INSTANCE.renderer.blockAccess = renderer.blockAccess;
			BusRenderer.INSTANCE.renderer.overrideBlockTexture = renderer.overrideBlockTexture;
			( (TileCableBus) t ).cb.renderStatic();
			BusRenderer.INSTANCE.renderer.renderAllFaces = false;
		}

		return BusRenderHelper.INSTANCE.getItemsRendered() > 0;
	}

	@Override
	public void renderTile( BlockCableBus block, TileCableBus t, WorldRenderer tess, double x, double y, double z, float f, ModelGenerator renderer )
	{
		if( t instanceof TileCableBus )
		{
			BusRenderer.INSTANCE.renderer.overrideBlockTexture = null;
			t.cb.renderDynamic( x, y, z );
		}
	}
}
