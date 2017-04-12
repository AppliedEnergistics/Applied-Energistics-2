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


import appeng.block.storage.BlockSkyChest;
import appeng.client.render.BaseBlockRender;
import appeng.tile.storage.TileSkyChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;


public class RenderBlockSkyChest extends BaseBlockRender<BlockSkyChest, TileSkyChest>
{

	private static final ResourceLocation SKY_STONE_CHEST = new ResourceLocation( "appliedenergistics2", "textures/models/skychest.png" );
	private static final ResourceLocation SKY_BLOCK_CHEST = new ResourceLocation( "appliedenergistics2", "textures/models/skyblockchest.png" );
	private static final ResourceLocation[] METADATA_TO_TEXTURE = {
			SKY_STONE_CHEST,
			SKY_BLOCK_CHEST
	};

	private final ModelChest model = new ModelChest();

	public RenderBlockSkyChest()
	{
		super( true, 80 );
	}

	@Override
	public void renderInventory( final BlockSkyChest blk, final ItemStack is, final RenderBlocks renderer, final ItemRenderType type, final Object[] obj )
	{
		GL11.glEnable( GL12.GL_RESCALE_NORMAL );
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		final int metaData = is.getItemDamage();
		final ResourceLocation loc = METADATA_TO_TEXTURE[metaData];

		Minecraft.getMinecraft().getTextureManager().bindTexture( loc );

		GL11.glScalef( 1.0F, -1F, -1F );
		GL11.glTranslatef( -0.0F, -1.0F, -1.0F );

		this.model.chestLid.offsetY = -( 0.9f / 16.0f );
		final float lidAngle = 0.0f;
		this.model.chestLid.rotateAngleX = -( ( lidAngle * 3.141593F ) / 2.0F );
		this.model.renderAll();

		GL11.glDisable( GL12.GL_RESCALE_NORMAL );
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
	}

	@Override
	public boolean renderInWorld( final BlockSkyChest blk, final IBlockAccess world, final int x, final int y, final int z, final RenderBlocks renderer )
	{
		return true;
	}

	@Override
	public void renderTile( final BlockSkyChest block, final TileSkyChest skyChest, final Tessellator tess, final double x, final double y, final double z, final float partialTick, final RenderBlocks renderer )
	{
		if( skyChest == null || !skyChest.hasWorldObj() )
		{
			return;
		}

		GL11.glEnable( GL12.GL_RESCALE_NORMAL );
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		final int metaData = skyChest.getWorldObj().getBlockMetadata( skyChest.xCoord, skyChest.yCoord, skyChest.zCoord );
		final ResourceLocation loc = METADATA_TO_TEXTURE[metaData];

		Minecraft.getMinecraft().getTextureManager().bindTexture( loc );

		this.applyTESRRotation( x, y, z, skyChest.getForward(), skyChest.getUp() );

		GL11.glScalef( 1.0F, -1F, -1F );
		GL11.glTranslatef( -0.0F, -1.0F, -1.0F );

		final long now = System.currentTimeMillis();
		final long distance = now - skyChest.getLastEvent();

		if( skyChest.getPlayerOpen() > 0 )
		{
			skyChest.setLidAngle( skyChest.getLidAngle() + distance * 0.0001f );
		}
		else
		{
			skyChest.setLidAngle( skyChest.getLidAngle() - distance * 0.0001f );
		}

		if( skyChest.getLidAngle() > 0.5f )
		{
			skyChest.setLidAngle( 0.5f );
		}

		if( skyChest.getLidAngle() < 0.0f )
		{
			skyChest.setLidAngle( 0.0f );
		}

		float lidAngle = skyChest.getLidAngle();
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
