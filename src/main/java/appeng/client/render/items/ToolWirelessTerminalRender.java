package appeng.client.render.items;

import org.lwjgl.opengl.GL11;

import appeng.api.util.AEColor;
import appeng.client.texture.ExtraItemTextures;
import appeng.items.tools.powered.ToolWirelessTerminal;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;

/**
 * Created by Tom on 9/30/2015.
 */
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
        // Get icon index for the texture
        IIcon icon = ExtraItemTextures.WirelessTerminal_Inactive.getIcon();
        if( ToolWirelessTerminal.isLinked( item ) )
        {
            icon = item.getIconIndex();
        }

        AEColor color = ToolWirelessTerminal.getColor( item );
        float r = 0;
        float g = 0;
        float b = 0;
        if( color!=null ) {
            int medColor = color.mediumVariant;
            r = ( medColor >> 16 ) & 0xFF;
            g = ( medColor >> 8 ) & 0xFF;
            b = medColor & 0xFF;
        }

        float f4 = icon.getMinU();
        float f5 = icon.getMaxU();
        float f6 = icon.getMinV();
        float f7 = icon.getMaxV();

        final Tessellator tessellator = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );

        if( type == ItemRenderType.INVENTORY )
        {
            GL11.glColor4f( 1, 1, 1, 1.0F );
            GL11.glScalef( 16F, 16F, 10F );
            GL11.glTranslatef( 0.0F, 1.0F, 0.0F );
            GL11.glRotatef( 180F, 1.0F, 0.0F, 0.0F );
            GL11.glEnable( GL11.GL_ALPHA_TEST );

            tessellator.startDrawingQuads();
            tessellator.setNormal( 0.0F, 1.0F, 0.0F );
            tessellator.addVertexWithUV( 0, 0, 0, f4, f7 );
            tessellator.addVertexWithUV( 1, 0, 0, f5, f7 );
            tessellator.addVertexWithUV( 1, 1, 0, f5, f6 );
            tessellator.addVertexWithUV( 0, 1, 0, f4, f6 );
            tessellator.draw();

            icon = ExtraItemTextures.WirelessTerminal_Border.getIcon();

            f4 = icon.getMinU();
            f5 = icon.getMaxU();
            f6 = icon.getMinV();
            f7 = icon.getMaxV();

            if( color!=null )
            {
                GL11.glColor3f( r/256.0f, g/256.0f, b/256.0f );
            }

            tessellator.startDrawingQuads();
            tessellator.setNormal( 0.0F, 1.0F, 0.0F );
            tessellator.addVertexWithUV( 0, 0, 0, f4, f7 );
            tessellator.addVertexWithUV( 1, 0, 0, f5, f7 );
            tessellator.addVertexWithUV( 1, 1, 0, f5, f6 );
            tessellator.addVertexWithUV( 0, 1, 0, f4, f6 );
            tessellator.draw();
        }
        else
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
            final float f12 = 0.0625F;
            ItemRenderer.renderItemIn2D( tessellator, f5, f6, f4, f7, icon.getIconWidth(), icon.getIconHeight(), f12 );

            icon = ExtraItemTextures.WirelessTerminal_Border.getIcon();

            f4 = icon.getMinU();
            f5 = icon.getMaxU();
            f6 = icon.getMinV();
            f7 = icon.getMaxV();

            if( color!=null )
            {
                GL11.glColor3f( r/256.0f, g/256.0f, b/256.0f );
            }

            ItemRenderer.renderItemIn2D( tessellator, f5, f6, f4, f7, icon.getIconWidth(), icon.getIconHeight(), f12 );
        }

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }
}
