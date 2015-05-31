/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


@SideOnly( Side.CLIENT )
public final class RenderFloatingItem extends RenderItem
{

	public static DoubleBuffer buffer = ByteBuffer.allocateDirect( 8 * 4 ).asDoubleBuffer();

	public RenderFloatingItem()
	{
		this.shadowOpaque = 0.0F;
		this.renderManager = RenderManager.instance;
	}

	@Override
	public final void doRender( EntityItem entityItem, double x, double y, double z, float yaw, float partialTick )
	{
		if( entityItem instanceof EntityFloatingItem )
		{
			EntityFloatingItem efi = (EntityFloatingItem) entityItem;
			if( efi.progress > 0.0 )
			{
				GL11.glPushMatrix();

				if( !( efi.getEntityItem().getItem() instanceof ItemBlock ) )
				{
					GL11.glTranslatef( 0, -0.15f, 0 );
				}

				super.doRender( efi, x, y, z, yaw, partialTick );
				GL11.glPopMatrix();
			}
		}
	}

	@Override
	public final boolean shouldBob()
	{
		return false;
	}
}
