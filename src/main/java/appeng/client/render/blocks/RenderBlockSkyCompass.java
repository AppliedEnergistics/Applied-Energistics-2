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
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.model.ModelCompass;
import appeng.hooks.CompassManager;
import appeng.hooks.CompassResult;
import appeng.tile.AEBaseTile;
import appeng.tile.misc.TileSkyCompass;

public class RenderBlockSkyCompass extends BaseBlockRender
{

	float r = 0;
	final ModelCompass model = new ModelCompass();

	public RenderBlockSkyCompass() {
		super( true, 80 );
	}

	@Override
	public void renderInventory(AEBaseBlock blk, ItemStack is, RenderBlocks renderer, ItemRenderType type, Object[] obj)
	{
		if ( type == ItemRenderType.INVENTORY )
		{
			boolean isGood = false;

			IInventory inv = Minecraft.getMinecraft().thePlayer.inventory;
			for (int x = 0; x < inv.getSizeInventory(); x++)
				if ( inv.getStackInSlot( x ) == is )
					isGood = true;

			if ( !isGood )
				type = ItemRenderType.FIRST_PERSON_MAP;
		}

		GL11.glEnable( 32826 /* GL_RESCALE_NORMAL_EXT */);
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		ResourceLocation loc = new ResourceLocation( "appliedenergistics2", "textures/models/compass.png" );

		Minecraft.getMinecraft().getTextureManager().bindTexture( loc );

		if ( type == ItemRenderType.ENTITY )
		{
			GL11.glRotatef( -90.0f, 0.0f, 0.0f, 1.0f );
			GL11.glScalef( 1.0F, -1F, -1F );
			GL11.glScalef( 2.5f, 2.5f, 2.5f );
			GL11.glTranslatef( -0.25F, -1.65F, -0.19F );
		}
		else
		{
			if ( type == ItemRenderType.EQUIPPED_FIRST_PERSON )
				GL11.glRotatef( 15.3f, 0.0f, 0.0f, 1.0f );

			GL11.glScalef( 1.0F, -1F, -1F );
			GL11.glScalef( 2.5f, 2.5f, 2.5f );

			if ( type == ItemRenderType.EQUIPPED_FIRST_PERSON )
				GL11.glTranslatef( 0.3F, -1.65F, -0.19F );
			else
				GL11.glTranslatef( 0.2F, -1.65F, -0.19F );
		}

		long now = System.currentTimeMillis();
		if ( type == ItemRenderType.EQUIPPED_FIRST_PERSON || type == ItemRenderType.INVENTORY || type == ItemRenderType.EQUIPPED )
		{
			EntityPlayer p = Minecraft.getMinecraft().thePlayer;
			float rYaw = p.rotationYaw;

			if ( type == ItemRenderType.EQUIPPED )
			{
				p = (EntityPlayer) obj[1];
				rYaw = p.renderYawOffset;
			}

			int x = (int) p.posX;
			int y = (int) p.posY;
			int z = (int) p.posZ;
			CompassResult cr = CompassManager.instance.getCompassDirection( 0, x, y, z );

			for (int i = 0; i < 3; i++)
				for (int j = 0; j < 3; j++)
					CompassManager.instance.getCompassDirection( 0, x + i - 1, y, z + j - 1 );

			if ( cr.hasResult )
			{
				if ( cr.spin )
				{
					now = now % 100000;
					this.model.renderAll( (now / 50000.0f) * (float) Math.PI * 500.0f );
				}
				else
				{
					if ( type == ItemRenderType.EQUIPPED_FIRST_PERSON )
					{
						float offRads = rYaw / 180.0f * (float) Math.PI;
						float adjustment = (float) Math.PI * 0.74f;
						this.model.renderAll( (float) this.flipidiy( cr.rad + offRads + adjustment ) );
					}
					else
					{
						float offRads = rYaw / 180.0f * (float) Math.PI;
						float adjustment = (float) Math.PI * -0.74f;
						this.model.renderAll( (float) this.flipidiy( cr.rad + offRads + adjustment ) );
					}
				}
			}
			else
			{
				now = now % 1000000;
				this.model.renderAll( (now / 500000.0f) * (float) Math.PI * 500.0f );
			}

		}
		else
		{
			now = now % 100000;
			this.model.renderAll( (now / 50000.0f) * (float) Math.PI * 500.0f );
		}

		GL11.glDisable( 32826 /* GL_RESCALE_NORMAL_EXT */);
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
	}

	@Override
	public boolean renderInWorld(AEBaseBlock blk, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		return true;
	}

	@Override
	public void renderTile(AEBaseBlock block, AEBaseTile tile, Tessellator tess, double x, double y, double z, float partialTick, RenderBlocks renderer)
	{
		if ( !(tile instanceof TileSkyCompass) )
			return;

		TileSkyCompass skyCompass = (TileSkyCompass) tile;

		if ( !skyCompass.hasWorldObj() )
			return;

		GL11.glEnable( 32826 /* GL_RESCALE_NORMAL_EXT */);
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		ResourceLocation loc = new ResourceLocation( "appliedenergistics2", "textures/models/compass.png" );

		Minecraft.getMinecraft().getTextureManager().bindTexture( loc );

		this.applyTESRRotation( x, y, z, skyCompass.getUp(), skyCompass.getForward() );

		GL11.glScalef( 1.0F, -1F, -1F );
		GL11.glTranslatef( 0.5F, -1.5F, -0.5F );

		long now = System.currentTimeMillis();

		CompassResult cr = null;
		if ( skyCompass.getForward() == ForgeDirection.UP || skyCompass.getForward() == ForgeDirection.DOWN )
			cr = CompassManager.instance.getCompassDirection( 0, tile.xCoord, tile.yCoord, tile.zCoord );
		else
			cr = new CompassResult( false, true, 0 );

		if ( cr.hasResult )
		{
			if ( cr.spin )
			{
				now = now % 100000;
				this.model.renderAll( (now / 50000.0f) * (float) Math.PI * 500.0f );
			}
			else
				this.model.renderAll( (float) (skyCompass.getForward() == ForgeDirection.DOWN ? this.flipidiy( cr.rad ) : cr.rad) );

		}
		else
		{
			now = now % 1000000;
			this.model.renderAll( (now / 500000.0f) * (float) Math.PI * 500.0f );
		}

		GL11.glDisable( 32826 /* GL_RESCALE_NORMAL_EXT */);
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
	}

	private double flipidiy(double rad)
	{
		double x = Math.cos( rad );
		double y = Math.sin( rad );
		return Math.atan2( -y, x );
	}
}
