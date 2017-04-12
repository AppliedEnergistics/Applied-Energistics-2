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

package appeng.entity;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;


@SideOnly( Side.CLIENT )
public class RenderTinyTNTPrimed extends Render
{

	private final RenderBlocks blockRenderer = new RenderBlocks();

	public RenderTinyTNTPrimed()
	{
		this.shadowSize = 0.5F;
		this.renderManager = RenderManager.instance;
	}

	@Override
	public void doRender( final Entity tnt, final double x, final double y, final double z, final float unused, final float life )
	{
		this.renderPrimedTNT( (EntityTinyTNTPrimed) tnt, x, y, z, life );
	}

	private void renderPrimedTNT( final EntityTinyTNTPrimed tnt, final double x, final double y, final double z, final float life )
	{
		GL11.glPushMatrix();
		GL11.glTranslatef( (float) x, (float) y - 0.25f, (float) z );
		float f2;

		if( tnt.fuse - life + 1.0F < 10.0F )
		{
			f2 = 1.0F - ( tnt.fuse - life + 1.0F ) / 10.0F;

			if( f2 < 0.0F )
			{
				f2 = 0.0F;
			}

			if( f2 > 1.0F )
			{
				f2 = 1.0F;
			}

			f2 *= f2;
			f2 *= f2;
			final float f3 = 1.0F + f2 * 0.3F;
			GL11.glScalef( f3, f3, f3 );
		}

		GL11.glScalef( 0.5f, 0.5f, 0.5f );
		f2 = ( 1.0F - ( tnt.fuse - life + 1.0F ) / 100.0F ) * 0.8F;
		this.bindEntityTexture( tnt );
		this.blockRenderer.renderBlockAsItem( Blocks.tnt, 0, tnt.getBrightness( life ) );

		if( tnt.fuse / 5 % 2 == 0 )
		{
			GL11.glDisable( GL11.GL_TEXTURE_2D );
			GL11.glDisable( GL11.GL_LIGHTING );
			GL11.glEnable( GL11.GL_BLEND );
			GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA );
			GL11.glColor4f( 1.0F, 1.0F, 1.0F, f2 );
			this.blockRenderer.renderBlockAsItem( Blocks.tnt, 0, 1.0F );
			GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
			GL11.glDisable( GL11.GL_BLEND );
			GL11.glEnable( GL11.GL_LIGHTING );
			GL11.glEnable( GL11.GL_TEXTURE_2D );
		}

		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture( final Entity entity )
	{
		return TextureMap.locationBlocksTexture;
	}
}
