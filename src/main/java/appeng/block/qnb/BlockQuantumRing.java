package appeng.block.qnb;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderQNB;
import appeng.core.features.AEFeature;
import appeng.helpers.ICustomCollision;
import appeng.tile.qnb.TileQuantumBridge;

public class BlockQuantumRing extends AEBaseBlock implements ICustomCollision
{

	public BlockQuantumRing() {
		super( BlockQuantumRing.class, Material.iron );
		setFeature( EnumSet.of( AEFeature.QuantumNetworkBridge ) );
		setTileEntity( TileQuantumBridge.class );
		float shave = 2.0f / 16.0f;
		setBlockBounds( shave, shave, shave, 1.0f - shave, 1.0f - shave, 1.0f - shave );
		setLightOpacity( 1 );
		isFullSize = isOpaque = false;
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, Block pointlessNumber)
	{
		TileQuantumBridge bridge = getTileEntity( w, x, y, z );
		if ( bridge != null )
			bridge.neighborUpdate();
	}

	@Override
	public void breakBlock(World w, int x, int y, int z, Block a, int b)
	{
		TileQuantumBridge bridge = getTileEntity( w, x, y, z );
		if ( bridge != null )
			bridge.breakCluster();

		super.breakBlock( w, x, y, z, a, b );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderQNB.class;
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool(World w, int x, int y, int z, Entity e, boolean isVisual)
	{
		double OnePx = 2.0 / 16.0;
		TileQuantumBridge bridge = getTileEntity( w, x, y, z );
		if ( bridge != null && bridge.isCorner() )
		{
			OnePx = 4.0 / 16.0;
		}
		else if ( bridge != null && bridge.isFormed() )
		{
			OnePx = 1.0 / 16.0;
		}
		return Arrays.asList( new AxisAlignedBB[] { AxisAlignedBB.getBoundingBox( OnePx, OnePx, OnePx, 1.0 - OnePx, 1.0 - OnePx, 1.0 - OnePx ) } );
	}

	@Override
	public void addCollidingBlockToList(World w, int x, int y, int z, AxisAlignedBB bb, List out, Entity e)
	{
		double OnePx = 2.0 / 16.0;
		TileQuantumBridge bridge = getTileEntity( w, x, y, z );
		if ( bridge != null && bridge.isCorner() )
		{
			OnePx = 4.0 / 16.0;
		}
		else if ( bridge != null && bridge.isFormed() )
		{
			OnePx = 1.0 / 16.0;
		}
		out.add( AxisAlignedBB.getBoundingBox( OnePx, OnePx, OnePx, 1.0 - OnePx, 1.0 - OnePx, 1.0 - OnePx ) );
	}

}
