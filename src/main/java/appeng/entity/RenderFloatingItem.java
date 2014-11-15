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

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFloatingItem extends RenderItem
{

	public static DoubleBuffer buffer = ByteBuffer.allocateDirect( 8 * 4 ).asDoubleBuffer();

	public RenderFloatingItem() {
		this.shadowOpaque = 0.0F;
		this.renderManager = RenderManager.instance;
	}

	@Override
	public boolean shouldBob()
	{
		return false;
	}

	@Override
	public void doRender(EntityItem p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
	{
		if ( p_76986_1_ instanceof EntityFloatingItem )
		{
			EntityFloatingItem efi = (EntityFloatingItem) p_76986_1_;
			if ( efi.progress > 0.0 )
			{
				GL11.glPushMatrix();

				if ( !(efi.getEntityItem().getItem() instanceof ItemBlock) )
					GL11.glTranslatef( 0, -0.15f, 0 );

				super.doRender( efi, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_ );
				GL11.glPopMatrix();
			}
		}
	}

}
