package appeng.block.spatial;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.spatial.TileSpatialIOPort;
import appeng.util.Platform;

public class BlockSpatialIOPort extends AEBaseBlock
{

	public BlockSpatialIOPort() {
		super( BlockSpatialIOPort.class, Material.iron );
		setFeature( EnumSet.of( AEFeature.SpatialIO ) );
		setTileEntity( TileSpatialIOPort.class );
	}

	@Override
	public final void onNeighborBlockChange(World w, int x, int y, int z, Block junk)
	{
		TileSpatialIOPort te = getTileEntity( w, x, y, z );
		if ( te != null )
			te.updateRedstoneState();
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		if ( p.isSneaking() )
			return false;

		TileSpatialIOPort tg = getTileEntity( w, x, y, z );
		if ( tg != null )
		{
			if ( Platform.isServer() )
				Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_SPATIALIOPORT );
			return true;
		}
		return false;
	}

}
