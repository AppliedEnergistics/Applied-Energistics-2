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


import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;

import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.tile.AEBaseTile;
import appeng.tile.storage.TileSkyChest;


public class RenderBlockSkyChest extends BaseBlockRender
{

	final ModelChest model = new ModelChest();

	public RenderBlockSkyChest()
	{
		super( true, 80 );
	}

	@Override
	public void renderInventory( AEBaseBlock blk, ItemStack is, RenderBlocks renderer, ItemRenderType type, Object[] obj )
	{
		GL11.glEnable( 32826 /* GL_RESCALE_NORMAL_EXT */ );
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		ResourceLocation loc;
		if( is.getItemDamage() == 1 )
			loc = new ResourceLocation( "appliedenergistics2", "textures/models/skyblockchest.png" );
		else
			loc = new ResourceLocation( "appliedenergistics2", "textures/models/skychest.png" );

		Minecraft.getMinecraft().getTextureManager().bindTexture( loc );

		float lidAngle = 0.0f;

		GL11.glScalef( 1.0F, -1F, -1F );
		GL11.glTranslatef( -0.0F, -1.0F, -1.0F );

		this.model.chestLid.offsetY = -( 0.9f / 16.0f );
		this.model.chestLid.rotateAngleX = -( ( lidAngle * 3.141593F ) / 2.0F );
		this.model.renderAll();

		GL11.glDisable( 32826 /* GL_RESCALE_NORMAL_EXT */ );
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
	}

	@Override
	public boolean renderInWorld( AEBaseBlock blk, IBlockAccess world, int x, int y, int z, RenderBlocks renderer )
	{
		return true;
	}

	@Override
	public void renderTile( AEBaseBlock block, AEBaseTile tile, Tessellator tess, double x, double y, double z, float partialTick, RenderBlocks renderer )
	{
		if( !( tile instanceof TileSkyChest ) )
			return;

		TileSkyChest skyChest = (TileSkyChest) tile;

		if( !skyChest.hasWorldObj() )
			return;

		GL11.glEnable( 32826 /* GL_RESCALE_NORMAL_EXT */ );
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		ResourceLocation loc;

		if( tile.getWorldObj().getBlockMetadata( tile.xCoord, tile.yCoord, tile.zCoord ) == 1 )
			loc = new ResourceLocation( "appliedenergistics2", "textures/models/skyblockchest.png" );
		else
			loc = new ResourceLocation( "appliedenergistics2", "textures/models/skychest.png" );

		Minecraft.getMinecraft().getTextureManager().bindTexture( loc );

		this.applyTESRRotation( x, y, z, skyChest.getForward(), skyChest.getUp() );

		GL11.glScalef( 1.0F, -1F, -1F );
		GL11.glTranslatef( -0.0F, -1.0F, -1.0F );

		long now = System.currentTimeMillis();
		long distance = now - skyChest.lastEvent;

		if( skyChest.playerOpen > 0 )
			skyChest.lidAngle += distance * 0.0001;
		else
			skyChest.lidAngle -= distance * 0.0001;

		if( skyChest.lidAngle > 0.5f )
			skyChest.lidAngle = 0.5f;

		if( skyChest.lidAngle < 0.0f )
			skyChest.lidAngle = 0.0f;

		float lidAngle = skyChest.lidAngle;
		lidAngle = 1.0F - lidAngle;
		lidAngle = 1.0F - lidAngle * lidAngle * lidAngle;

		this.model.chestLid.offsetY = -( 1.01f / 16.0f );
		this.model.chestLid.rotateAngleX = -( ( lidAngle * 3.141593F ) / 2.0F );
		this.model.renderAll();

		GL11.glDisable( 32826 /* GL_RESCALE_NORMAL_EXT */ );
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
	}
}
