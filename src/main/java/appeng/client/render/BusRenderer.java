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

package appeng.client.render;

import java.util.HashMap;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import appeng.api.parts.IAlphaPassItem;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.client.ClientHelper;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.facade.IFacadeItem;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BusRenderer implements IItemRenderer
{

	public static final BusRenderer instance = new BusRenderer();

	public final RenderBlocksWorkaround renderer = new RenderBlocksWorkaround();
	public static final HashMap<Integer, IPart> renderPart = new HashMap<Integer, IPart>();

	public IPart getRenderer(ItemStack is, IPartItem c)
	{
		int id = (Item.getIdFromItem( is.getItem() ) << Platform.DEF_OFFSET) | is.getItemDamage();

		IPart part = renderPart.get( id );
		if ( part == null )
		{
			part = c.createPartFromItemStack( is );
			if ( part != null )
				renderPart.put( id, part );
		}

		return part;
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		if ( item == null )
			return;

		GL11.glPushMatrix();
		GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
		GL11.glEnable( GL11.GL_DEPTH_TEST );
		GL11.glEnable( GL11.GL_TEXTURE_2D );
		GL11.glEnable( GL11.GL_LIGHTING );

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.AlphaPass ) && item.getItem() instanceof IAlphaPassItem
				&& ((IAlphaPassItem) item.getItem()).useAlphaPass( item ) )
		{
			GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
			GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
			GL11.glDisable( GL11.GL_ALPHA_TEST );
			GL11.glEnable( GL11.GL_BLEND );
		}
		else
		{
			GL11.glAlphaFunc( GL11.GL_GREATER, 0.4f );
			GL11.glEnable( GL11.GL_ALPHA_TEST );
			GL11.glDisable( GL11.GL_BLEND );
		}

		if ( type == ItemRenderType.EQUIPPED_FIRST_PERSON )
		{
			GL11.glTranslatef( -0.2f, -0.1f, -0.3f );
		}

		if ( type == ItemRenderType.ENTITY )
		{
			GL11.glRotatef( 90.0f, 0.0f, 1.0f, 0.0f );
			GL11.glScalef( 0.8f, 0.8f, 0.8f );
			GL11.glTranslatef( -0.8f, -0.87f, -0.7f );
		}

		if ( type == ItemRenderType.INVENTORY )
			GL11.glTranslatef( 0.0f, -0.1f, 0.0f );

		GL11.glTranslated( 0.2, 0.3, 0.1 );
		GL11.glScaled( 1.2, 1.2, 1. );

		GL11.glColor4f( 1, 1, 1, 1 );
		Tessellator.instance.setColorOpaque_F( 1, 1, 1 );
		Tessellator.instance.setBrightness( 14 << 20 | 14 << 4 );

		BusRenderHelper.instance.setBounds( 0, 0, 0, 1, 1, 1 );
		BusRenderHelper.instance.setTexture( null );
		BusRenderHelper.instance.setInvColor( 0xffffff );
		renderer.blockAccess = ClientHelper.proxy.getWorld();

		BusRenderHelper.instance.setOrientation( ForgeDirection.EAST, ForgeDirection.UP, ForgeDirection.SOUTH );

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
		renderer.useInventoryTint = false;
		renderer.overrideBlockTexture = null;

		if ( item.getItem() instanceof IFacadeItem )
		{
			IFacadeItem fi = (IFacadeItem) item.getItem();
			IFacadePart fp = fi.createPartFromItemStack( item, ForgeDirection.SOUTH );

			if ( type == ItemRenderType.EQUIPPED_FIRST_PERSON )
			{
				GL11.glRotatef( 160.0f, 0.0f, 1.0f, 0.0f );
				GL11.glTranslated( -0.4, 0.1, -1.6 );
			}

			if ( fp != null )
				fp.renderInventory( BusRenderHelper.instance, renderer );
		}
		else
		{
			IPart ip = getRenderer( item, (IPartItem) item.getItem() );
			if ( ip != null )
			{
				if ( type == ItemRenderType.ENTITY )
				{
					int depth = ip.cableConnectionRenderTo();
					GL11.glTranslatef( 0.0f, 0.0f, -0.04f * (8 - depth) - 0.06f );
				}

				ip.renderInventory( BusRenderHelper.instance, renderer );
			}
		}

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}
}
