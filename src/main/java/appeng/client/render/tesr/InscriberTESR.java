
package appeng.client.render.tesr;


import appeng.recipes.handlers.InscriberRecipe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.MatrixApplyingVertexBuilder;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.pipeline.TransformerConsumer;
import net.minecraftforge.items.IItemHandler;

import appeng.client.render.FacingToRotation;
import appeng.core.AppEng;
import appeng.tile.misc.TileInscriber;


/**
 * Renders the dynamic parts of an inscriber (the presses, the animation and the item being smashed)
 */
public final class InscriberTESR extends TileEntityRenderer<TileInscriber>
{

	private static final float ITEM_RENDER_SCALE = 1.0f / 1.2f;

	private static final Material TEXTURE_INSIDE = new Material(PlayerContainer.LOCATION_BLOCKS_TEXTURE, new ResourceLocation( AppEng.MOD_ID, "block/inscriber_inside" ));

	public InscriberTESR(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}

	// See https://easings.net/#easeOutBack
	private static float ease(float x) {
		float c1 = 1.70158f;
		float c3 = c1 + 1;

		return (float) (1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2));
	}

	@Override
	public void render(TileInscriber tile, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffers, int combinedLight, int combinedOverlay) {

		// render inscriber

		ms.push();
		ms.translate( 0.5F, 0.5F, 0.5F );
		FacingToRotation.get( tile.getForward(), tile.getUp() ).push(ms);
		ms.translate( -0.5F, -0.5F, -0.5F );

		// render sides of stamps

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
		} else {
			// Only apply the easing function on the way down
			progress = ease(progress);
		}

		float press = 0.2f;
		press -= progress / 5.0f;

		float middle = 0.5f;
		middle += 0.02f;
		final float TwoPx = 2.0f / 16.0f;
		final float base = 0.4f;

		final TextureAtlasSprite tas = TEXTURE_INSIDE.getSprite();

		IVertexBuilder buffer = buffers.getBuffer(RenderType.getSolid());

		// Bottom of Top Stamp
		addVertex( buffer, ms, TwoPx, middle + press, TwoPx, 1, 1, 1, 1, tas.getInterpolatedU( 2 ), tas.getInterpolatedV( 13 ), combinedOverlay, combinedLight, 0, -1, 0);
		addVertex( buffer, ms, 1.0f - TwoPx, middle + press, TwoPx, 1, 1, 1, 1, tas.getInterpolatedU( 14 ), tas.getInterpolatedV( 13 ), combinedOverlay, combinedLight, 0, -1, 0);
		addVertex( buffer, ms, 1.0f - TwoPx, middle + press, 1.0f - TwoPx, 1, 1, 1, 1, tas.getInterpolatedU( 14 ), tas.getInterpolatedV( 2 ), combinedOverlay, combinedLight, 0, -1, 0);
		addVertex( buffer, ms, TwoPx, middle + press, 1.0f - TwoPx, 1, 1, 1, 1, tas.getInterpolatedU( 2 ), tas.getInterpolatedV( 2 ), combinedOverlay, combinedLight, 0, -1, 0);

		// Front of Top Stamp
		addVertex( buffer, ms, TwoPx, middle + base, TwoPx, 1, 1, 1, 1, tas.getInterpolatedU( 2 ), tas.getInterpolatedV( 3 - 16 * ( press - base ) ), combinedOverlay, combinedLight, 0, 0, -1);
		addVertex( buffer, ms, 1.0f - TwoPx, middle + base, TwoPx, 1, 1, 1, 1, tas.getInterpolatedU( 14 ), tas.getInterpolatedV( 3 - 16 * ( press - base ) ), combinedOverlay, combinedLight, 0, 0, -1);
		addVertex( buffer, ms, 1.0f - TwoPx, middle + press, TwoPx, 1, 1, 1, 1, tas.getInterpolatedU( 14 ), tas.getInterpolatedV( 3 ), combinedOverlay, combinedLight, 0, 0, -1);
		addVertex( buffer, ms, TwoPx, middle + press, TwoPx, 1, 1, 1, 1, tas.getInterpolatedU( 2 ), tas.getInterpolatedV( 3 ), combinedOverlay, combinedLight, 0, 0, -1);

		// Top of Bottom Stamp
		middle -= 2.0f * 0.02f;
		addVertex( buffer, ms, 1.0f - TwoPx, middle - press, TwoPx, 1, 1, 1, 1, tas.getInterpolatedU( 2 ), tas.getInterpolatedV( 13 ), combinedOverlay, combinedLight, 0, 1, 0);
		addVertex( buffer, ms, TwoPx, middle - press, TwoPx, 1, 1, 1, 1, tas.getInterpolatedU( 14 ), tas.getInterpolatedV( 13 ), combinedOverlay, combinedLight, 0, 1, 0);
		addVertex( buffer, ms, TwoPx, middle - press, 1.0f - TwoPx, 1, 1, 1, 1, tas.getInterpolatedU( 14 ), tas.getInterpolatedV( 2 ), combinedOverlay, combinedLight, 0, 1, 0);
		addVertex( buffer, ms, 1.0f - TwoPx, middle - press, 1.0f - TwoPx, 1, 1, 1, 1, tas.getInterpolatedU( 2 ), tas.getInterpolatedV( 2 ), combinedOverlay, combinedLight, 0, 1, 0);

		// Front of Bottom Stamp
		addVertex( buffer, ms, 1.0f - TwoPx, middle + -base, TwoPx, 1, 1, 1, 1, tas.getInterpolatedU( 2 ), tas.getInterpolatedV( 3 - 16 * ( press - base ) ), combinedOverlay, combinedLight, 0, 0, -1);
		addVertex( buffer, ms, TwoPx, middle - base, TwoPx, 1, 1, 1, 1, tas.getInterpolatedU( 14 ), tas.getInterpolatedV( 3 - 16 * ( press - base ) ), combinedOverlay, combinedLight, 0, 0, -1);
		addVertex( buffer, ms, TwoPx, middle - press, TwoPx, 1, 1, 1, 1, tas.getInterpolatedU( 14 ), tas.getInterpolatedV( 3 ), combinedOverlay, combinedLight, 0, 0, -1);
		addVertex( buffer, ms, 1.0f - TwoPx, middle - press, TwoPx, 1, 1, 1, 1, tas.getInterpolatedU( 2 ), tas.getInterpolatedV( 3 ), combinedOverlay, combinedLight, 0, 0, -1);

		// render items.

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
				final InscriberRecipe ir = tile.getTask();
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
	}

	private static void addVertex(IVertexBuilder vb, MatrixStack ms, float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ) {
		vb.pos(ms.getLast().getMatrix(), x, y, z);
		vb.color(red, green, blue, alpha);
		vb.tex(texU, texV);
		vb.overlay(overlayUV);
		vb.lightmap(lightmapUV);
		vb.normal(ms.getLast().getNormal(), normalX, normalY, normalZ);
		vb.endVertex();
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

	public static void registerTexture( TextureStitchEvent.Pre evt )
	{
		if (evt.getMap().getTextureLocation().equals(TEXTURE_INSIDE.getAtlasLocation())) {
			evt.addSprite(TEXTURE_INSIDE.getTextureLocation());
		}
	}
}
