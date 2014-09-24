package appeng.client.render.items;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import appeng.api.util.AEColor;
import appeng.client.texture.ExtraItemTextures;
import appeng.items.tools.powered.ToolColorApplicator;

public class ToolColorApplicatorRender implements IItemRenderer
{

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		IIcon par2Icon = item.getIconIndex();

		float f4 = ((IIcon) par2Icon).getMinU();
		float f5 = ((IIcon) par2Icon).getMaxU();
		float f6 = ((IIcon) par2Icon).getMinV();
		float f7 = ((IIcon) par2Icon).getMaxV();
		float f12 = 0.0625F;

		Tessellator tessellator = Tessellator.instance;
		GL11.glPushMatrix();
		GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );

		if ( type == ItemRenderType.INVENTORY )
		{
			GL11.glColor4f( 1, 1, 1, 1.0F );
			GL11.glScalef( 16F, 16F, 10F );
			GL11.glTranslatef( 0.0F, 1.0F, 0.0F );
			GL11.glRotatef( 180F, 1.0F, 0.0F, 0.0F );
			GL11.glEnable( GL11.GL_ALPHA_TEST );

			tessellator.startDrawingQuads();
			tessellator.setNormal( 0.0F, 1.0F, 0.0F );
			tessellator.addVertexWithUV( 0, 0, 0, (double) f4, (double) f7 );
			tessellator.addVertexWithUV( 1, 0, 0, (double) f5, (double) f7 );
			tessellator.addVertexWithUV( 1, 1, 0, (double) f5, (double) f6 );
			tessellator.addVertexWithUV( 0, 1, 0, (double) f4, (double) f6 );
			tessellator.draw();
		}
		else
		{
			if ( type == ItemRenderType.EQUIPPED_FIRST_PERSON )
				GL11.glTranslatef( 0.0F, 0.0F, 0.0F );
			else if ( type == ItemRenderType.EQUIPPED )
				GL11.glTranslatef( 0.0F, 0.0F, 0.0F );
			else
				GL11.glTranslatef( -0.5F, -0.3F, 0.01F );
			ItemRenderer.renderItemIn2D( tessellator, f5, f6, f4, f7, ((IIcon) par2Icon).getIconWidth(), ((IIcon) par2Icon).getIconHeight(), f12 );

			GL11.glDisable( GL11.GL_CULL_FACE );
			GL11.glColor4f( 1, 1, 1, 1.0F );
			GL11.glScalef( -1F, -1F, 1F );
			GL11.glTranslatef( -1.125F, 0.0f, f12 / -2.0f );
			GL11.glRotatef( 180F, 1.0F, 0.0F, 0.0F );
		}

		IIcon dark = ExtraItemTextures.ToolColorApplicatorTip_Dark.getIcon();
		IIcon med = ExtraItemTextures.ToolColorApplicatorTip_Medium.getIcon();
		IIcon light = ExtraItemTextures.ToolColorApplicatorTip_Light.getIcon();

		GL11.glScalef( 1F / 16F, 1F / 16F, 1F );
		if ( type != ItemRenderType.INVENTORY )
			GL11.glTranslatef( 2, 0, 0 );
		GL11.glDisable( GL11.GL_LIGHTING );

		AEColor col = null;

		col = ((ToolColorApplicator) item.getItem()).getActiveColor( item );

		if ( col != null )
		{
			tessellator.startDrawingQuads();

			f4 = ((IIcon) dark).getMinU();
			f5 = ((IIcon) dark).getMaxU();
			f6 = ((IIcon) dark).getMinV();
			f7 = ((IIcon) dark).getMaxV();

			tessellator.setColorOpaque_I( col.blackVariant );
			tessellator.addVertexWithUV( 0, 0, 0, (double) f4, (double) f7 );
			tessellator.addVertexWithUV( 16, 0, 0, (double) f5, (double) f7 );
			tessellator.addVertexWithUV( 16, 16, 0, (double) f5, (double) f6 );
			tessellator.addVertexWithUV( 0, 16, 0, (double) f4, (double) f6 );

			f4 = ((IIcon) light).getMinU();
			f5 = ((IIcon) light).getMaxU();
			f6 = ((IIcon) light).getMinV();
			f7 = ((IIcon) light).getMaxV();

			tessellator.setColorOpaque_I( col.whiteVariant );
			tessellator.addVertexWithUV( 0, 0, 0, (double) f4, (double) f7 );
			tessellator.addVertexWithUV( 16, 0, 0, (double) f5, (double) f7 );
			tessellator.addVertexWithUV( 16, 16, 0, (double) f5, (double) f6 );
			tessellator.addVertexWithUV( 0, 16, 0, (double) f4, (double) f6 );

			f4 = ((IIcon) med).getMinU();
			f5 = ((IIcon) med).getMaxU();
			f6 = ((IIcon) med).getMinV();
			f7 = ((IIcon) med).getMaxV();

			tessellator.setColorOpaque_I( col.mediumVariant );
			tessellator.addVertexWithUV( 0, 0, 0, (double) f4, (double) f7 );
			tessellator.addVertexWithUV( 16, 0, 0, (double) f5, (double) f7 );
			tessellator.addVertexWithUV( 16, 16, 0, (double) f5, (double) f6 );
			tessellator.addVertexWithUV( 0, 16, 0, (double) f4, (double) f6 );

			tessellator.draw();
		}

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}
}
