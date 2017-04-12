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
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import org.lwjgl.opengl.GL11;


@SideOnly( Side.CLIENT )
public class RenderFloatingItem extends RenderItem
{

	public RenderFloatingItem()
	{
		this.shadowOpaque = 0.0F;
		this.renderManager = RenderManager.instance;
	}

	@Override
	public void doRender( final EntityItem entityItem, final double x, final double y, final double z, final float yaw, final float partialTick )
	{
		if( entityItem instanceof EntityFloatingItem )
		{
			final EntityFloatingItem efi = (EntityFloatingItem) entityItem;
			if( efi.getProgress() > 0.0 )
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
	public boolean shouldBob()
	{
		return false;
	}
}
