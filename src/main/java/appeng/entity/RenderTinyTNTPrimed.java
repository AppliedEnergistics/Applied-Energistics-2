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


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import appeng.client.render.ModelGenerator;


@SideOnly( Side.CLIENT )
public class RenderTinyTNTPrimed extends Render
{

	private final ModelGenerator blockRenderer = new ModelGenerator();

    public RenderTinyTNTPrimed(RenderManager p_i46134_1_)
    {
        super(p_i46134_1_);
		this.shadowSize = 0.5F;
	}

	@Override
	public void doRender( Entity tnt, double x, double y, double z, float unused, float life )
	{
		this.renderPrimedTNT( (EntityTinyTNTPrimed) tnt, x, y, z, unused, life );
	}

	public void renderPrimedTNT( EntityTinyTNTPrimed tnt, double x, double y, double z, float unused, float life  )
	{
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y + 0.5F, (float)z);
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
			float f3 = 1.0F + f2 * 0.3F;
			GL11.glScalef( f3, f3, f3 );
		}

		GL11.glScalef( 0.5f, 0.5f, 0.5f );
        f2 = (1.0F - (tnt.fuse - life + 1.0F) / 100.0F) * 0.8F;
        this.bindEntityTexture(tnt);
        GlStateManager.translate(-0.5F, -0.5F, 0.5F);
        blockrendererdispatcher.renderBlockBrightness(Blocks.tnt.getDefaultState(), tnt.getBrightness(life));
        GlStateManager.translate(0.0F, 0.0F, 1.0F);

		if( tnt.fuse / 5 % 2 == 0 )
		{
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 772);
            GlStateManager.color(1.0F, 1.0F, 1.0F, f2);
            GlStateManager.doPolygonOffset(-3.0F, -3.0F);
            GlStateManager.enablePolygonOffset();
            blockrendererdispatcher.renderBlockBrightness(Blocks.tnt.getDefaultState(), 1.0F);
            GlStateManager.doPolygonOffset(0.0F, 0.0F);
            GlStateManager.disablePolygonOffset();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
		}

        GlStateManager.popMatrix();
        super.doRender(tnt, x, y, z, unused, life );
	}

	@Override
	protected ResourceLocation getEntityTexture( Entity entity )
	{
		return TextureMap.locationBlocksTexture;
	}
}
