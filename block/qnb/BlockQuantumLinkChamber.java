package appeng.block.qnb;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.client.EffectType;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderQNB;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.helpers.AEGlassMaterial;
import appeng.helpers.ICustomCollision;
import appeng.tile.qnb.TileQuantumBridge;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockQuantumLinkChamber extends AEBaseBlock implements ICustomCollision
{

	public BlockQuantumLinkChamber() {
		super( BlockQuantumLinkChamber.class, AEGlassMaterial.instance );
		setFeature( EnumSet.of( AEFeature.QuantumNetworkBridge ) );
		setTileEntity( TileQuantumBridge.class );
		float shave = 2.0f / 16.0f;
		setBlockBounds( shave, shave, shave, 1.0f - shave, 1.0f - shave, 1.0f - shave );
		setLightOpacity( 0 );
		isFullSize = isOpaque = false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World w, int bx, int by, int bz, Random r)
	{
		TileQuantumBridge bridge = getTileEntity( w, bx, by, bz );
		if ( bridge != null )
		{
			if ( bridge.hasQES() )
			{
				if ( CommonHelper.proxy.shouldAddParticles( r ) )
					CommonHelper.proxy.spawnEffect( EffectType.Energy, w, bx + 0.5, by + 0.5, bz + 0.5, null );
			}
		}
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, Block pointlessnumber)
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
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		if ( p.isSneaking() )
			return false;

		TileQuantumBridge tg = getTileEntity( w, x, y, z );
		if ( tg != null )
		{
			if ( Platform.isServer() )
				Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_QNB );
			return true;
		}
		return false;
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool(World w, int x, int y, int z, Entity e, boolean isVisual)
	{
		double OnePx = 2.0 / 16.0;
		return Arrays.asList( new AxisAlignedBB[] { AxisAlignedBB.getBoundingBox( OnePx, OnePx, OnePx, 1.0 - OnePx, 1.0 - OnePx, 1.0 - OnePx ) } );
	}

	@Override
	public void addCollidingBlockToList(World w, int x, int y, int z, AxisAlignedBB bb, List out, Entity e)
	{
		double OnePx = 2.0 / 16.0;
		out.add( AxisAlignedBB.getBoundingBox( OnePx, OnePx, OnePx, 1.0 - OnePx, 1.0 - OnePx, 1.0 - OnePx ) );
	}

}
