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


import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import appeng.block.storage.BlockSkyChest;
import appeng.block.storage.BlockSkyChest.SkyChestType;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.IRenderHelper;
import appeng.tile.storage.TileSkyChest;


public class RenderBlockSkyChest extends BaseBlockRender<BlockSkyChest, TileSkyChest>
{

	private final static ResourceLocation SKY_STONE_CHEST = new ResourceLocation( "appliedenergistics2", "textures/models/skychest.png" );
	private final static ResourceLocation SKY_BLOCK_CHEST = new ResourceLocation( "appliedenergistics2", "textures/models/skyblockchest.png" );
	private final static ResourceLocation METADATA_TO_TEXTURE[] = new ResourceLocation[] { SKY_STONE_CHEST, SKY_BLOCK_CHEST };

	private final ModelChest model = new ModelChest();

	public RenderBlockSkyChest()
	{
		super( true, 80 );
	}

	@Override
	public void renderInventory( BlockSkyChest blk, ItemStack is, IRenderHelper renderer, ItemRenderType type, Object[] obj )
	{
		//GL11.glEnable( GL12.GL_RESCALE_NORMAL );
		//GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		//final int metaData = is.getItemDamage();
		//final ResourceLocation loc = METADATA_TO_TEXTURE[metaData];

		//Minecraft.getMinecraft().getTextureManager().bindTexture( loc );

		//final float lidAngle = 0.0f;

		//GL11.glScalef( 1.0F, -1F, -1F );
		//GL11.glTranslatef( -0.0F, -1.0F, -1.0F );

		//this.model.chestLid.offsetY = -( 0.9f / 16.0f );
		//this.model.chestLid.rotateAngleX = -( ( lidAngle * 3.141593F ) / 2.0F );
		//this.model.renderAll();

		//GL11.glDisable( GL12.GL_RESCALE_NORMAL );
		//GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
	}

	@Override
	public boolean renderInWorld( BlockSkyChest blk, IBlockAccess world, BlockPos pos, IRenderHelper renderer )
	{
		return true;
	}

	@Override
	public void renderTile( BlockSkyChest block, TileSkyChest tile, WorldRenderer tess, double x, double y, double z, float partialTick, IRenderHelper renderer )
	{
		if( !( tile instanceof TileSkyChest ) )
		{
			return;
		}

		final TileSkyChest skyChest = tile;

		if( !skyChest.hasWorldObj() )
		{
			return;
		}

		GL11.glEnable( GL12.GL_RESCALE_NORMAL );
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		final IBlockState metaData = tile.getWorld().getBlockState( tile.getPos() );
		final ResourceLocation loc = METADATA_TO_TEXTURE[ ((BlockSkyChest)metaData.getBlock()).type == SkyChestType.BLOCK ? 1 : 0 ];

		Minecraft.getMinecraft().getTextureManager().bindTexture( loc );

		this.applyTESRRotation( x, y, z, skyChest.getForward(), skyChest.getUp() );

		GL11.glScalef( 1.0F, -1F, -1F );
		GL11.glTranslatef( -0.0F, -1.0F, -1.0F );

		final long now = System.currentTimeMillis();
		final long distance = now - skyChest.lastEvent;

		if( skyChest.playerOpen > 0 )
		{
			skyChest.lidAngle += distance * 0.0001;
		}
		else
		{
			skyChest.lidAngle -= distance * 0.0001;
		}

		if( skyChest.lidAngle > 0.5f )
		{
			skyChest.lidAngle = 0.5f;
		}

		if( skyChest.lidAngle < 0.0f )
		{
			skyChest.lidAngle = 0.0f;
		}

		float lidAngle = skyChest.lidAngle;
		lidAngle = 1.0F - lidAngle;
		lidAngle = 1.0F - lidAngle * lidAngle * lidAngle;

		this.model.chestLid.offsetY = -( 1.01f / 16.0f );
		this.model.chestLid.rotateAngleX = -( ( lidAngle * 3.141593F ) / 2.0F );

		// The vanilla chests wants culling reversed...
		GL11.glCullFace( GL11.GL_FRONT );
		this.model.renderAll();
		GL11.glCullFace( GL11.GL_BACK );

		GL11.glDisable( GL12.GL_RESCALE_NORMAL );
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
	}
}
