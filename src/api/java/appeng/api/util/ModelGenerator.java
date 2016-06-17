package appeng.api.util;


import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;


/**
 * This is a helper to render stuff in 1.8.+
 *
 * This is required by all blocks which require a better renderer than the in house default renderer
 *
 * @author thatsIch
 * @version rv3 - 16.02.2016
 * @since rv3 - 16.02.2016
 */
public interface ModelGenerator
{
	int setUvRotateBottom( int i );

	int setUvRotateTop( int i );

	int setUvRotateEast( int i );

	int setUvRotateWest( int i );

	int setUvRotateNorth( int i );

	int setUvRotateSouth( int i );

	IAESprite[] getIcon( IBlockState iBlockState );

	IAESprite getOverrideBlockTexture();

	IBlockAccess getBlockAccess();

	void setBlockAccess( IBlockAccess theWorld );

	void setRenderAllFaces( boolean b );

	void setOverrideBlockTexture( IAESprite o );

	void setFlipTexture( boolean b );

	void setRenderBoundsFromBlock( Block block );

	void setRenderBounds( double v, double v1, double v2, double v3, double v4, double v5 );

	boolean renderStandardBlock( Block block, BlockPos pos );

	void setRenderFaces( EnumSet<EnumFacing> faces );

	void setNormal( float v, float v1, float v2 );

	void setColorOpaque_I( int color );

	void setRenderMinX( double min );

	void setRenderMinY( double min );

	void setRenderMinZ( double min );

	void setRenderMaxX( double max );

	void setRenderMaxY( double max );

	void setRenderMaxZ( double max );

	void addVertexWithUV( EnumFacing face, double v, double v1, double v2, double interpolatedU, double interpolatedV );

	void renderFaceZNeg( Block block, BlockPos pos, IAESprite ico );

	void renderFaceZPos( Block block, BlockPos pos, IAESprite ico );

	void renderFaceXPos( Block block, BlockPos pos, IAESprite ico );

	void renderFaceXNeg( Block block, BlockPos pos, IAESprite ico );

	void renderFaceYPos( Block block, BlockPos pos, IAESprite ico );

	void renderFaceYNeg( Block block, BlockPos pos, IAESprite ico );

	void setTranslation( int i, int i1, int i2 );

	IAESprite getIcon( ItemStack itemStack );

	void setBrightness( int i );

	void setColorOpaque_F( float v, float v1, float v2 );

	boolean isAlphaPass();

	void setColorRGBA_F( int i, int i1, int i2, float v );

	double getRenderMinX();

	double getRenderMaxX();

	double getRenderMinY();

	double getRenderMaxY();

	double getRenderMinZ();

	double getRenderMaxZ();

	void finalizeModel( boolean b );

	IBakedModel getOutput();
}
