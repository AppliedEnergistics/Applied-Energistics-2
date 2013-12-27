package appeng.client.render.entity;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import appeng.entity.EntityTinyTNTPrimed;

public class RenderTinyTNTPrimed extends Render
{

	private RenderBlocks blockRenderer = new RenderBlocks();

	public RenderTinyTNTPrimed() {
		this.shadowSize = 0.5F;
		this.renderManager = RenderManager.instance;
	}

	public void renderPrimedTNT(EntityTinyTNTPrimed tnt, double x, double y, double z, float var1, float life)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef( (float) x, (float) y - 0.25f, (float) z );
		float f2;

		if ( (float) tnt.fuse - life + 1.0F < 10.0F )
		{
			f2 = 1.0F - ((float) tnt.fuse - life + 1.0F) / 10.0F;

			if ( f2 < 0.0F )
			{
				f2 = 0.0F;
			}

			if ( f2 > 1.0F )
			{
				f2 = 1.0F;
			}

			f2 *= f2;
			f2 *= f2;
			float f3 = 1.0F + f2 * 0.3F;
			GL11.glScalef( f3, f3, f3 );
		}

		GL11.glScalef( 0.5f, 0.5f, 0.5f );
		f2 = (1.0F - ((float) tnt.fuse - life + 1.0F) / 100.0F) * 0.8F;
		this.bindEntityTexture( tnt );
		this.blockRenderer.renderBlockAsItem( Block.tnt, 0, tnt.getBrightness( life ) );

		if ( tnt.fuse / 5 % 2 == 0 )
		{
			GL11.glDisable( GL11.GL_TEXTURE_2D );
			GL11.glDisable( GL11.GL_LIGHTING );
			GL11.glEnable( GL11.GL_BLEND );
			GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA );
			GL11.glColor4f( 1.0F, 1.0F, 1.0F, f2 );
			this.blockRenderer.renderBlockAsItem( Block.tnt, 0, 1.0F );
			GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
			GL11.glDisable( GL11.GL_BLEND );
			GL11.glEnable( GL11.GL_LIGHTING );
			GL11.glEnable( GL11.GL_TEXTURE_2D );
		}

		GL11.glPopMatrix();
	}

	@Override
	public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
	{
		this.renderPrimedTNT( (EntityTinyTNTPrimed) par1Entity, par2, par4, par6, par8, par9 );
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity)
	{
		return TextureMap.locationBlocksTexture;
	}

}
