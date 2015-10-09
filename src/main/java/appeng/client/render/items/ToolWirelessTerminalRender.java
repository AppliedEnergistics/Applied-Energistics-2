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

package appeng.client.render.items;


import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;

import appeng.api.util.AEColor;
import appeng.client.texture.ExtraItemTextures;
import appeng.items.tools.powered.ToolWirelessTerminal;

public class ToolWirelessTerminalRender implements IItemRenderer
{

    @Override
    public boolean handleRenderType( ItemStack item, ItemRenderType type )
    {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper( ItemRenderType type, ItemStack item, ItemRendererHelper helper )
    {
        return false;
    }

    @Override
    public void renderItem( ItemRenderType type, ItemStack item, Object... data )
    {
        Entity itemLocation = item.getItemFrame();
        if( itemLocation==null )
        {
            itemLocation = Minecraft.getMinecraft().thePlayer;
        }
        
        boolean displayAntenna = ((ToolWirelessTerminal)item.getItem()).getIsUsable( item, itemLocation);
        boolean hasPower = (( ToolWirelessTerminal) item.getItem()).hasPower( null,0.5,item );

        IIcon border;
        if( displayAntenna )
        {
            border = ExtraItemTextures.WirelessTerminal_Border.getIcon();
        }
        else
        {
            border = ExtraItemTextures.WirelessTerminal_Border_Inactive.getIcon();
        }
        IIcon scrollBar = ExtraItemTextures.WirelessTerminal_ScrollBar.getIcon();
        IIcon screen = ExtraItemTextures.WirelessTerminal_Screen.getIcon();
        IIcon icons = ExtraItemTextures.WirelessTerminal_Icons.getIcon();

        AEColor color = ToolWirelessTerminal.getColor( item );
        if( color==null )
        {
            screen = item.getIconIndex();
        }

        final Tessellator tessellator = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );

        //translate stuff for different item render types
        if( type != ItemRenderType.INVENTORY )
        {
            if( type == ItemRenderType.EQUIPPED_FIRST_PERSON )
            {
                GL11.glTranslatef( 0.0F, 0.0F, 0.0F );
            }
            else if( type == ItemRenderType.EQUIPPED )
            {
                GL11.glTranslatef( 0.0F, 0.0F, 0.0F );
            }
            else
            {
                GL11.glTranslatef( -0.5F, -0.3F, 0.01F );
            }
        }
        else
        {
            GL11.glColor4f( 1, 1, 1, 1.0F );
            GL11.glScalef( 16F, 16F, 10F );
            GL11.glTranslatef( 0.0F, 1.0F, 0.0F );
            GL11.glRotatef( 180F, 1.0F, 0.0F, 0.0F );
            GL11.glEnable( GL11.GL_ALPHA_TEST );
        }

        final float f12 = 0.0625F;

        //Border, which is uncolored
        subRenderItem( type, tessellator, border, f12);

        if( hasPower )
        {
            RenderHelper.disableStandardItemLighting();
        }

        //If a terminal isn't colored, use the default icon which doesn't require icons or scrollbar
        if( color!=null )
        {
            //Icons, which are dark colored
            {
                final int blackColor = color.blackVariant;
                float r = ( blackColor >> 16 ) & 0xFF;
                float g = ( blackColor >> 8 ) & 0xFF;
                float b = blackColor & 0xFF;
                GL11.glColor3f( r / 256.0f, g / 256.0f, b / 256.0f );

                subRenderItem( type, tessellator, icons, f12);
            }

            //Scrollbar, which is medium colored
            {
                final int medColor = color.mediumVariant;
                float r = ( medColor >> 16 ) & 0xFF;
                float g = ( medColor >> 8 ) & 0xFF;
                float b = medColor & 0xFF;
                GL11.glColor3f( r / 256.0f, g / 256.0f, b / 256.0f );

                subRenderItem( type, tessellator, scrollBar, f12 );
            }
        }

        //Screen, which is light colored
        {
            if( color!=null )
            {
                final int whiteColor = color.whiteVariant;
                float r = ( whiteColor >> 16 ) & 0xFF;
                float g = ( whiteColor >> 8 ) & 0xFF;
                float b = whiteColor & 0xFF;
                GL11.glColor3f( r / 256.0f, g / 256.0f, b / 256.0f );
            }

            subRenderItem( type, tessellator, screen, f12);
        }

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    private void subRenderItem(ItemRenderType type, Tessellator tessellator, IIcon icon, float f12)
    {
        float f4 = icon.getMinU();
        float f5 = icon.getMaxU();
        float f6 = icon.getMinV();
        float f7 = icon.getMaxV();
        int width = icon.getIconWidth();
        int height = icon.getIconHeight();

        if( type != ItemRenderType.INVENTORY )
        {
            ItemRenderer.renderItemIn2D( tessellator, f5, f6, f4, f7, width, height, f12 );
        }
        else
        {
            tessellator.startDrawingQuads();
            tessellator.setNormal( 0.0F, 1.0F, 0.0F );
            tessellator.addVertexWithUV( 0, 0, 0, f4, f7 );
            tessellator.addVertexWithUV( 1, 0, 0, f5, f7 );
            tessellator.addVertexWithUV( 1, 1, 0, f5, f6 );
            tessellator.addVertexWithUV( 0, 1, 0, f4, f6 );
            tessellator.draw();
        }
    }
}
