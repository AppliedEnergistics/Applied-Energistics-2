
package appeng.client.render.tesr;


import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.block.storage.BlockSkyChest;
import appeng.block.storage.BlockSkyChest.SkyChestType;
import appeng.client.render.FacingToRotation;
import appeng.core.AppEng;
import appeng.tile.storage.TileSkyChest;

@SideOnly( Side.CLIENT )
public class SkyChestTESR extends TileEntitySpecialRenderer<TileSkyChest>
{

	private static final ResourceLocation TEXTURE_STONE = new ResourceLocation( AppEng.MOD_ID, "textures/models/skychest.png" );
	private static final ResourceLocation TEXTURE_BLOCK = new ResourceLocation( AppEng.MOD_ID, "textures/models/skyblockchest.png" );

	private final ModelChest simpleChest = new ModelChest();

	public SkyChestTESR()
	{

	}

	@Override
	public void renderTileEntityAt( TileSkyChest te, double x, double y, double z, float partialTicks, int destroyStage )
	{
		GlStateManager.enableDepth();
		GlStateManager.depthFunc( 515 );
		GlStateManager.depthMask( true );

		ModelChest modelchest;

		modelchest = this.simpleChest;

		if( destroyStage >= 0 )
		{
			this.bindTexture( DESTROY_STAGES[destroyStage] );
			GlStateManager.matrixMode( 5890 );
			GlStateManager.pushMatrix();
			GlStateManager.scale( 4.0F, 4.0F, 1.0F );
			GlStateManager.translate( 0.0625F, 0.0625F, 0.0625F );
			GlStateManager.matrixMode( 5888 );
		}
		else
		{
			// TODO 1.10.2-R - So this is weirdly half working. Either fix it or deal with it.
			if( te != null )
			{
				this.bindTexture( ( (BlockSkyChest) te.getBlockType() ).type == SkyChestType.STONE ? TEXTURE_STONE : TEXTURE_BLOCK );
			}
			else
			{
				this.bindTexture( TEXTURE_BLOCK );
			}
		}

		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();

		if( destroyStage < 0 )
		{
			GlStateManager.color( 1.0F, 1.0F, 1.0F, 1.0F );
		}

		GlStateManager.translate( (float) x, (float) y + 1.0F, (float) z + 1.0F );
		GlStateManager.scale( 1.0F, -1.0F, -1.0F );
		GlStateManager.translate( 0.5F, 0.5F, 0.5F );

		if( te != null )
		{
			FacingToRotation.get( te.getForward(), te.getUp() ).glRotateCurrentMat();
		}
		GlStateManager.translate( -0.5F, -0.5F, -0.5F );
		float f = te != null ? te.getPrevLidAngle() + ( te.getLidAngle() - te.getPrevLidAngle() ) * partialTicks : 0;

		f = 1.0F - f;
		f = 1.0F - f * f * f;
		modelchest.chestLid.rotateAngleX = -( f * ( (float) Math.PI / 2F ) );
		modelchest.renderAll();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		GlStateManager.color( 1.0F, 1.0F, 1.0F, 1.0F );

		if( destroyStage >= 0 )
		{
			GlStateManager.matrixMode( 5890 );
			GlStateManager.popMatrix();
			GlStateManager.matrixMode( 5888 );
		}
	}

}
