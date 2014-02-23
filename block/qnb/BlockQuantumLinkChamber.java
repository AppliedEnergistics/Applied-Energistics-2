package appeng.block.qnb;

import java.util.EnumSet;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.client.EffectType;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderQNB;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.qnb.TileQuantumBridge;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockQuantumLinkChamber extends AEBaseBlock
{

	public BlockQuantumLinkChamber() {
		super( BlockQuantumLinkChamber.class, Material.glass );
		setfeature( EnumSet.of( AEFeature.QuantumNetworkBridge ) );
		setTileEntiy( TileQuantumBridge.class );
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
					CommonHelper.proxy.spawnEffect( EffectType.Energy, w, bx + 0.5, by + 0.5, bz + 0.5 );
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

}
