package appeng.block.storage;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.storage.TileIOPort;
import appeng.util.Platform;

public class BlockIOPort extends AEBaseBlock
{

	public BlockIOPort() {
		super( BlockIOPort.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.StorageCells, AEFeature.IOPort ) );
		setTileEntiy( TileIOPort.class );
	}

	@Override
	public final void onNeighborBlockChange(World w, int x, int y, int z, int junk)
	{
		TileIOPort te = getTileEntity( w, x, y, z );
		if ( te != null )
			te.updateRedstoneState();
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		if ( p.isSneaking() )
			return false;

		TileIOPort tg = getTileEntity( w, x, y, z );
		if ( tg != null )
		{
			if ( Platform.isServer() )
				Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_IOPORT );
			return true;
		}
		return false;
	}
}
