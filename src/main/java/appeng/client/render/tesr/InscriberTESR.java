
package appeng.client.render.tesr;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.items.IItemHandler;

import appeng.api.features.IInscriberRecipe;
import appeng.client.render.FacingToRotation;
import appeng.core.AppEng;
import appeng.tile.AEBaseTile;
import appeng.tile.misc.TileInscriber;


/**
 * Renders the dynamic parts of an inscriber (the presses, the animation and the item being smashed)
 */
public final class InscriberTESR extends TileEntityRenderer<TileInscriber>
{

	private static final float ITEM_RENDER_SCALE = 1.0f / 1.2f;

	private static final ResourceLocation TEXTURE_INSIDE = new ResourceLocation( AppEng.MOD_ID, "blocks/inscriber_inside" );

	private static TextureAtlasSprite textureInside;

	public InscriberTESR(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}

	@Override
	public void render(TileInscriber tile, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffers, int combinedLight, int combinedOverlay) {

		// render inscriber

		ms.push();
		ms.translate( 0.5F, 0.5F, 0.5F );
		FacingToRotation.get( tile.getForward(), tile.getUp() ).push(ms);
		ms.translate( -0.5F, -0.5F, -0.5F );

		// FIXME RenderSystem.color4f( 1.0F, 1.0F, 1.0F, 1.0F );
		// FIXME RenderSystem.disableLighting();
		// FIXME RenderSystem.disableRescaleNormal();

		// render sides of stamps

		Minecraft mc = Minecraft.getInstance();
//		FIXME RenderingEngine.getInstance().bindTexture( AtlasTexture.LOCATION_BLOCKS_TEXTURE );

		// << 20 | light << 4;
		// FIXME final int br = combinedLight;
		// FIXME final int var11 = br % 65536;
		// FIXME final int var12 = br / 65536;

		// FIXME OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, var11, var12 );

		long absoluteProgress = 0;

		if( tile.isSmash() )
		{
			final long currentTime = System.currentTimeMillis();
			absoluteProgress = currentTime - tile.getClientStart();
			if( absoluteProgress > 800 )
			{
				tile.setSmash( false );
			}
		}

		final float relativeProgress = absoluteProgress % 800 / 400.0f;
		float progress = relativeProgress;

		if( progress > 1.0f )
		{
			progress = 1.0f - ( progress - 1.0f );
		}
		float press = 0.2f;
		press -= progress / 5.0f;

		IVertexBuilder buffer = buffers.getBuffer(RenderType.getSolid());

//		final BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		// FIXME buffer.begin( GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX );

		float middle = 0.5f;
		middle += 0.02f;
		final float TwoPx = 2.0f / 16.0f;
		final float base = 0.4f;

		final TextureAtlasSprite tas = textureInside;
		if( tas != null )
		{
			// Bottom of Top Stamp
			buffer.pos( TwoPx, middle + press, TwoPx ).tex( tas.getInterpolatedU( 2 ), tas.getInterpolatedV( 13 ) ).endVertex();
			buffer.pos( 1.0 - TwoPx, middle + press, TwoPx ).tex( tas.getInterpolatedU( 14 ), tas.getInterpolatedV( 13 ) ).endVertex();
			buffer.pos( 1.0 - TwoPx, middle + press, 1.0 - TwoPx ).tex( tas.getInterpolatedU( 14 ), tas.getInterpolatedV( 2 ) ).endVertex();
			buffer.pos( TwoPx, middle + press, 1.0 - TwoPx ).tex( tas.getInterpolatedU( 2 ), tas.getInterpolatedV( 2 ) ).endVertex();

			// Front of Top Stamp
			buffer.pos( TwoPx, middle + base, TwoPx ).tex( tas.getInterpolatedU( 2 ), tas.getInterpolatedV( 3 - 16 * ( press - base ) ) ).endVertex();
			buffer.pos( 1.0 - TwoPx, middle + base, TwoPx ).tex( tas.getInterpolatedU( 14 ), tas.getInterpolatedV( 3 - 16 * ( press - base ) ) ).endVertex();
			buffer.pos( 1.0 - TwoPx, middle + press, TwoPx ).tex( tas.getInterpolatedU( 14 ), tas.getInterpolatedV( 3 ) ).endVertex();
			buffer.pos( TwoPx, middle + press, TwoPx ).tex( tas.getInterpolatedU( 2 ), tas.getInterpolatedV( 3 ) ).endVertex();

			// Top of Bottom Stamp
			middle -= 2.0f * 0.02f;
			buffer.pos( 1.0 - TwoPx, middle - press, TwoPx ).tex( tas.getInterpolatedU( 2 ), tas.getInterpolatedV( 13 ) ).endVertex();
			buffer.pos( TwoPx, middle - press, TwoPx ).tex( tas.getInterpolatedU( 14 ), tas.getInterpolatedV( 13 ) ).endVertex();
			buffer.pos( TwoPx, middle - press, 1.0 - TwoPx ).tex( tas.getInterpolatedU( 14 ), tas.getInterpolatedV( 2 ) ).endVertex();
			buffer.pos( 1.0 - TwoPx, middle - press, 1.0 - TwoPx ).tex( tas.getInterpolatedU( 2 ), tas.getInterpolatedV( 2 ) ).endVertex();

			// Front of Bottom Stamp
			buffer.pos( 1.0 - TwoPx, middle + -base, TwoPx ).tex( tas.getInterpolatedU( 2 ), tas.getInterpolatedV( 3 - 16 * ( press - base ) ) ).endVertex();
			buffer.pos( TwoPx, middle - base, TwoPx ).tex( tas.getInterpolatedU( 14 ), tas.getInterpolatedV( 3 - 16 * ( press - base ) ) ).endVertex();
			buffer.pos( TwoPx, middle - press, TwoPx ).tex( tas.getInterpolatedU( 14 ), tas.getInterpolatedV( 3 ) ).endVertex();
			buffer.pos( 1.0 - TwoPx, middle - press, TwoPx ).tex( tas.getInterpolatedU( 2 ), tas.getInterpolatedV( 3 ) ).endVertex();
		}

		// render items.
//		FIXME RenderSystem.color4f( 1.0F, 1.0F, 1.0F, 1.0F );

		IItemHandler tileInv = tile.getInternalInventory();

		int items = 0;
		if( !tileInv.getStackInSlot( 0 ).isEmpty() )
		{
			items++;
		}
		if( !tileInv.getStackInSlot( 1 ).isEmpty() )
		{
			items++;
		}
		if( !tileInv.getStackInSlot( 2 ).isEmpty() )
		{
			items++;
		}

		if( relativeProgress > 1.0f || items == 0 )
		{
			ItemStack is = tileInv.getStackInSlot( 3 );

			if( is.isEmpty() )
			{
				final IInscriberRecipe ir = tile.getTask();
				if( ir != null )
				{
					is = ir.getOutput().copy();
				}
			}

			this.renderItem( ms, is, 0.0f, buffers, combinedLight, combinedOverlay );
		}
		else
		{
			this.renderItem( ms, tileInv.getStackInSlot( 0 ), press, buffers, combinedLight, combinedOverlay );
			this.renderItem( ms, tileInv.getStackInSlot( 1 ), -press, buffers, combinedLight, combinedOverlay );
			this.renderItem( ms, tileInv.getStackInSlot( 2 ), 0.0f, buffers, combinedLight, combinedOverlay );
		}

		ms.pop();
		// FIXME RenderSystem.enableLighting();
		// FIXME GlStateManager.enableRescaleNormal();
	}

	private void renderItem( MatrixStack ms, final ItemStack stack, final float o, IRenderTypeBuffer buffers, int combinedLight, int combinedOverlay )
	{
		if( !stack.isEmpty() )
		{
			final ItemStack sis = stack.copy(); // FIXME WHY????

			ms.push();
			// move to center
			ms.translate( 0.5f, 0.5f + o, 0.5f );
			ms.rotate( new Quaternion(90, 0, 0, true) );
			// set scale
			ms.scale( ITEM_RENDER_SCALE, ITEM_RENDER_SCALE, ITEM_RENDER_SCALE );

			// heuristic to scale items down much further than blocks
			if( !( sis.getItem() instanceof BlockItem ) )
			{
				ms.scale( 0.5f, 0.5f, 0.5f );
			}

			Minecraft.getInstance().getItemRenderer().renderItem(sis, ItemCameraTransforms.TransformType.FIXED, combinedLight, combinedOverlay, ms, buffers);
			ms.pop();
		}
	}

	public static void registerTexture( TextureStitchEvent.Pre event )
	{
		// FIXME textureInside = event.getMap().registerSprite( TEXTURE_INSIDE );
	}
}
