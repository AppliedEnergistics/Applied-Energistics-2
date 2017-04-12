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

package appeng.client.render;


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
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;


@SideOnly( Side.CLIENT )
public class BusRenderer implements IItemRenderer
{

	public static final BusRenderer INSTANCE = new BusRenderer();
	private static final Map<Integer, IPart> RENDER_PART = new HashMap<Integer, IPart>();
	private final RenderBlocksWorkaround renderer = new RenderBlocksWorkaround();

	@Override
	public boolean handleRenderType( final ItemStack item, final ItemRenderType type )
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper( final ItemRenderType type, final ItemStack item, final ItemRendererHelper helper )
	{
		return true;
	}

	@Override
	public void renderItem( final ItemRenderType type, final ItemStack item, final Object... data )
	{
		if( item == null )
		{
			return;
		}

		GL11.glPushMatrix();
		GL11.glPushAttrib( GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT );
		GL11.glEnable( GL11.GL_DEPTH_TEST );
		GL11.glEnable( GL11.GL_TEXTURE_2D );
		GL11.glEnable( GL11.GL_LIGHTING );

		if( AEConfig.instance.isFeatureEnabled( AEFeature.AlphaPass ) && item.getItem() instanceof IAlphaPassItem && ( (IAlphaPassItem) item.getItem() ).useAlphaPass( item ) )
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

		if( type == ItemRenderType.EQUIPPED_FIRST_PERSON )
		{
			GL11.glTranslatef( -0.2f, -0.1f, -0.3f );
		}

		if( type == ItemRenderType.ENTITY )
		{
			GL11.glRotatef( 90.0f, 0.0f, 1.0f, 0.0f );
			GL11.glScalef( 0.8f, 0.8f, 0.8f );
			GL11.glTranslatef( -0.8f, -0.87f, -0.7f );
		}

		if( type == ItemRenderType.INVENTORY )
		{
			GL11.glTranslatef( 0.0f, -0.1f, 0.0f );
		}

		GL11.glTranslated( 0.2, 0.3, 0.1 );
		GL11.glScaled( 1.2, 1.2, 1. );

		GL11.glColor4f( 1, 1, 1, 1 );
		Tessellator.instance.setColorOpaque_F( 1, 1, 1 );
		Tessellator.instance.setBrightness( 14 << 20 | 14 << 4 );

		BusRenderHelper.INSTANCE.setBounds( 0, 0, 0, 1, 1, 1 );
		BusRenderHelper.INSTANCE.setTexture( null );
		BusRenderHelper.INSTANCE.setInvColor( 0xffffff );
		this.getRenderer().blockAccess = ClientHelper.proxy.getWorld();

		BusRenderHelper.INSTANCE.setOrientation( ForgeDirection.EAST, ForgeDirection.UP, ForgeDirection.SOUTH );

		this.getRenderer().uvRotateBottom = this.getRenderer().uvRotateEast = this.getRenderer().uvRotateNorth = this.getRenderer().uvRotateSouth = this.getRenderer().uvRotateTop = this.getRenderer().uvRotateWest = 0;
		this.getRenderer().useInventoryTint = false;
		this.getRenderer().overrideBlockTexture = null;

		if( item.getItem() instanceof IFacadeItem )
		{
			final IFacadeItem fi = (IFacadeItem) item.getItem();
			final IFacadePart fp = fi.createPartFromItemStack( item, ForgeDirection.SOUTH );

			if( type == ItemRenderType.EQUIPPED_FIRST_PERSON )
			{
				GL11.glRotatef( 160.0f, 0.0f, 1.0f, 0.0f );
				GL11.glTranslated( -0.4, 0.1, -1.6 );
			}

			if( fp != null )
			{
				fp.renderInventory( BusRenderHelper.INSTANCE, this.getRenderer() );
			}
		}
		else
		{
			final IPart ip = this.getRenderer( item, (IPartItem) item.getItem() );

			if( ip != null )
			{
				if( type == ItemRenderType.ENTITY )
				{
					final int depth = ip.cableConnectionRenderTo();
					GL11.glTranslatef( 0.0f, 0.0f, -0.04f * ( 8 - depth ) - 0.06f );
				}

				ip.renderInventory( BusRenderHelper.INSTANCE, this.getRenderer() );
			}
		}

		this.getRenderer().uvRotateBottom = this.getRenderer().uvRotateEast = this.getRenderer().uvRotateNorth = this.getRenderer().uvRotateSouth = this.getRenderer().uvRotateTop = this.getRenderer().uvRotateWest = 0;

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	@Nullable
	private IPart getRenderer( final ItemStack is, final IPartItem c )
	{
		final int id = ( Item.getIdFromItem( is.getItem() ) << Platform.DEF_OFFSET ) | is.getItemDamage();

		IPart part = RENDER_PART.get( id );

		if( part == null )
		{
			part = c.createPartFromItemStack( is );
			if( part != null )
			{
				RENDER_PART.put( id, part );
			}
		}

		return part;
	}

	public RenderBlocksWorkaround getRenderer()
	{
		return this.renderer;
	}
}
