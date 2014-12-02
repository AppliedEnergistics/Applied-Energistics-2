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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import appeng.api.storage.data.IAEItemStack;
import appeng.block.AEBaseBlock;
import appeng.client.ClientHelper;
import appeng.core.AELog;
import appeng.tile.AEBaseTile;
import appeng.tile.crafting.TileCraftingMonitorTile;
import appeng.util.Platform;

public class RenderBlockCraftingCPUMonitor extends RenderBlockCraftingCPU
{

	public RenderBlockCraftingCPUMonitor() {
		super( true, 20 );
	}

	@Override
	public void renderTile(AEBaseBlock block, AEBaseTile tile, Tessellator tess, double x, double y, double z, float f, RenderBlocks renderer)
	{
		if ( Platform.isDrawing( tess ) )
			return;

		if ( tile instanceof TileCraftingMonitorTile )
		{
			TileCraftingMonitorTile cmt = (TileCraftingMonitorTile) tile;
			IAEItemStack ais = cmt.getJobProgress();

			if ( cmt.dspList == null )
			{
				cmt.updateList = true;
				cmt.dspList = GLAllocation.generateDisplayLists( 1 );
			}

			if ( ais != null )
			{
				GL11.glPushMatrix();
				GL11.glTranslated( x + 0.5, y + 0.5, z + 0.5 );

				if ( cmt.updateList )
				{
					cmt.updateList = false;
					GL11.glNewList( cmt.dspList, GL11.GL_COMPILE_AND_EXECUTE );
					tesrRenderScreen( tess, cmt, ais );
					GL11.glEndList();
				}
				else
					GL11.glCallList( cmt.dspList );

				GL11.glPopMatrix();
			}
		}
	}

	private void tesrRenderScreen(Tessellator tess, TileCraftingMonitorTile cmt, IAEItemStack ais)
	{
		ForgeDirection side = cmt.getForward();

		ForgeDirection walrus = side.offsetY != 0 ? ForgeDirection.SOUTH : ForgeDirection.UP;
		int spin = 0;

		int max = 5;
		while (walrus != cmt.getUp() && max-- > 0)
		{
			spin++;
			walrus = Platform.rotateAround( walrus, side );
		}

		GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
		GL11.glTranslated( side.offsetX * 0.69, side.offsetY * 0.69, side.offsetZ * 0.69 );

		float scale = 0.7f;
		GL11.glScalef( scale, scale, scale );

		if ( side == ForgeDirection.UP )
		{
			GL11.glScalef( 1.0f, -1.0f, 1.0f );
			GL11.glRotatef( 90.0f, 1.0f, 0.0f, 0.0f );
			GL11.glRotatef( spin * 90.0F, 0, 0, 1 );
		}

		if ( side == ForgeDirection.DOWN )
		{
			GL11.glScalef( 1.0f, -1.0f, 1.0f );
			GL11.glRotatef( -90.0f, 1.0f, 0.0f, 0.0f );
			GL11.glRotatef( spin * -90.0F, 0, 0, 1 );
		}

		if ( side == ForgeDirection.EAST )
		{
			GL11.glScalef( -1.0f, -1.0f, -1.0f );
			GL11.glRotatef( -90.0f, 0.0f, 1.0f, 0.0f );
		}

		if ( side == ForgeDirection.WEST )
		{
			GL11.glScalef( -1.0f, -1.0f, -1.0f );
			GL11.glRotatef( 90.0f, 0.0f, 1.0f, 0.0f );
		}

		if ( side == ForgeDirection.NORTH )
		{
			GL11.glScalef( -1.0f, -1.0f, -1.0f );
		}

		if ( side == ForgeDirection.SOUTH )
		{
			GL11.glScalef( -1.0f, -1.0f, -1.0f );
			GL11.glRotatef( 180.0f, 0.0f, 1.0f, 0.0f );
		}

		GL11.glPushMatrix();
		try
		{
			ItemStack sis = ais.getItemStack();
			sis.stackSize = 1;

			int br = 16 << 20 | 16 << 4;
			int var11 = br % 65536;
			int var12 = br / 65536;
			OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, var11 * 0.8F, var12 * 0.8F );

			GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

			GL11.glDisable( GL11.GL_LIGHTING );
			GL11.glDisable( GL12.GL_RESCALE_NORMAL );
			// RenderHelper.enableGUIStandardItemLighting();
			tess.setColorOpaque_F( 1.0f, 1.0f, 1.0f );

			ClientHelper.proxy.doRenderItem( sis, cmt.getWorldObj() );

		}
		catch (Exception e)
		{
			AELog.error( e );
		}

		GL11.glPopMatrix();

		GL11.glTranslatef( 0.0f, 0.14f, -0.24f );
		GL11.glScalef( 1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f );

		long qty = ais.getStackSize();
		if ( qty > 999999999999L )
			qty = 999999999999L;

		String msg = Long.toString( qty );
		if ( qty > 1000000000 )
			msg = Long.toString( qty / 1000000000 ) + "B";
		else if ( qty > 1000000 )
			msg = Long.toString( qty / 1000000 ) + "M";
		else if ( qty > 9999 )
			msg = Long.toString( qty / 1000 ) + "K";

		FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
		int width = fr.getStringWidth( msg );
		GL11.glTranslatef( -0.5f * width, 0.0f, -1.0f );
		fr.drawString( msg, 0, 0, 0 );

		GL11.glPopAttrib();
	}

}
