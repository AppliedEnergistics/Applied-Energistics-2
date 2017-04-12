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


import appeng.block.misc.BlockSkyCompass;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.model.ModelCompass;
import appeng.hooks.CompassManager;
import appeng.hooks.CompassResult;
import appeng.tile.misc.TileSkyCompass;
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
import org.lwjgl.opengl.GL12;


public class RenderBlockSkyCompass extends BaseBlockRender<BlockSkyCompass, TileSkyCompass>
{
	private static final ResourceLocation TEXTURE_SKY_COMPASS = new ResourceLocation( "appliedenergistics2", "textures/models/compass.png" );

	private final ModelCompass model = new ModelCompass();

	public RenderBlockSkyCompass()
	{
		super( true, 80 );
	}

	@Override
	public void renderInventory( final BlockSkyCompass blk, final ItemStack is, final RenderBlocks renderer, ItemRenderType type, final Object[] obj )
	{
		if( type == ItemRenderType.INVENTORY )
		{
			boolean isGood = false;
			final IInventory inv = Minecraft.getMinecraft().thePlayer.inventory;

			for( int x = 0; x < inv.getSizeInventory(); x++ )
			{
				if( inv.getStackInSlot( x ) == is )
				{
					isGood = true;
				}
			}

			if( !isGood )
			{
				type = ItemRenderType.FIRST_PERSON_MAP;
			}
		}

		GL11.glEnable( GL12.GL_RESCALE_NORMAL );
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		Minecraft.getMinecraft().getTextureManager().bindTexture( TEXTURE_SKY_COMPASS );

		if( type == ItemRenderType.ENTITY )
		{
			GL11.glRotatef( -90.0f, 0.0f, 0.0f, 1.0f );
			GL11.glScalef( 1.0F, -1F, -1F );
			GL11.glScalef( 2.5f, 2.5f, 2.5f );
			GL11.glTranslatef( -0.25F, -1.65F, -0.19F );
		}
		else
		{
			if( type == ItemRenderType.EQUIPPED_FIRST_PERSON )
			{
				GL11.glRotatef( 15.3f, 0.0f, 0.0f, 1.0f );
			}

			GL11.glScalef( 1.0F, -1F, -1F );
			GL11.glScalef( 2.5f, 2.5f, 2.5f );

			if( type == ItemRenderType.EQUIPPED_FIRST_PERSON )
			{
				GL11.glTranslatef( 0.3F, -1.65F, -0.19F );
			}
			else
			{
				GL11.glTranslatef( 0.2F, -1.65F, -0.19F );
			}
		}

		long now = System.currentTimeMillis();

		if( type == ItemRenderType.EQUIPPED_FIRST_PERSON || type == ItemRenderType.INVENTORY || type == ItemRenderType.EQUIPPED )
		{
			EntityPlayer p = Minecraft.getMinecraft().thePlayer;
			float rYaw = p.rotationYaw;

			if( type == ItemRenderType.EQUIPPED )
			{
				p = (EntityPlayer) obj[1];
				rYaw = p.renderYawOffset;
			}

			final int x = (int) p.posX;
			final int y = (int) p.posY;
			final int z = (int) p.posZ;
			final CompassResult cr = CompassManager.INSTANCE.getCompassDirection( 0, x, y, z );

			for( int i = 0; i < 3; i++ )
			{
				for( int j = 0; j < 3; j++ )
				{
					CompassManager.INSTANCE.getCompassDirection( 0, x + i - 1, y, z + j - 1 );
				}
			}

			if( cr.isValidResult() )
			{
				if( cr.isSpin() )
				{
					now %= 100000;
					this.model.renderAll( ( now / 50000.0f ) * (float) Math.PI * 500.0f );
				}
				else
				{
					if( type == ItemRenderType.EQUIPPED_FIRST_PERSON )
					{
						final float offRads = rYaw / 180.0f * (float) Math.PI;
						final float adjustment = (float) Math.PI * 0.74f;

						this.model.renderAll( (float) this.flipidiy( cr.getRad() + offRads + adjustment ) );
					}
					else
					{
						final float offRads = rYaw / 180.0f * (float) Math.PI;
						final float adjustment = (float) Math.PI * -0.74f;

						this.model.renderAll( (float) this.flipidiy( cr.getRad() + offRads + adjustment ) );
					}
				}
			}
			else
			{
				now %= 1000000;
				this.model.renderAll( ( now / 500000.0f ) * (float) Math.PI * 500.0f );
			}
		}
		else
		{
			now %= 100000;
			this.model.renderAll( ( now / 50000.0f ) * (float) Math.PI * 500.0f );
		}

		GL11.glDisable( GL12.GL_RESCALE_NORMAL );
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
	}

	@Override
	public boolean renderInWorld( final BlockSkyCompass blk, final IBlockAccess world, final int x, final int y, final int z, final RenderBlocks renderer )
	{
		return true;
	}

	@Override
	public void renderTile( final BlockSkyCompass block, final TileSkyCompass skyCompass, final Tessellator tess, final double x, final double y, final double z, final float partialTick, final RenderBlocks renderer )
	{
		if( skyCompass == null )
		{
			return;
		}

		if( !skyCompass.hasWorldObj() )
		{
			return;
		}

		GL11.glEnable( GL12.GL_RESCALE_NORMAL );
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
		GL11.glCullFace( GL11.GL_FRONT );

		Minecraft.getMinecraft().getTextureManager().bindTexture( TEXTURE_SKY_COMPASS );

		this.applyTESRRotation( x, y, z, skyCompass.getUp(), skyCompass.getForward() );

		GL11.glScalef( 1.0F, -1F, -1F );
		GL11.glTranslatef( 0.5F, -1.5F, -0.5F );

		long now = System.currentTimeMillis();
		CompassResult cr = null;

		if( skyCompass.getForward() == ForgeDirection.UP || skyCompass.getForward() == ForgeDirection.DOWN )
		{
			cr = CompassManager.INSTANCE.getCompassDirection( 0, skyCompass.xCoord, skyCompass.yCoord, skyCompass.zCoord );
		}
		else
		{
			cr = new CompassResult( false, true, 0 );
		}

		if( cr.isValidResult() )
		{
			if( cr.isSpin() )
			{
				now %= 100000;
				this.model.renderAll( ( now / 50000.0f ) * (float) Math.PI * 500.0f );
			}
			else
			{
				this.model.renderAll( (float) ( skyCompass.getForward() == ForgeDirection.DOWN ? this.flipidiy( cr.getRad() ) : cr.getRad() ) );
			}
		}
		else
		{
			now %= 1000000;
			this.model.renderAll( ( now / 500000.0f ) * (float) Math.PI * 500.0f );
		}

		GL11.glCullFace( GL11.GL_BACK );
		GL11.glDisable( GL12.GL_RESCALE_NORMAL );
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
	}

	private double flipidiy( final double rad )
	{
		final double x = Math.cos( rad );
		final double y = Math.sin( rad );
		return Math.atan2( -y, x );
	}
}
