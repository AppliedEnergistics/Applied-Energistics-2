package appeng.block.misc;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.misc.TileCellWorkbench;
import appeng.util.Platform;

public class BlockCellWorkbench extends AEBaseBlock
{

	public BlockCellWorkbench() {
		super( BlockCellWorkbench.class, Material.iron );
		setFeature( EnumSet.of( AEFeature.StorageCells ) );
		setTileEntity( TileCellWorkbench.class );
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		if ( p.isSneaking() )
			return false;

		TileCellWorkbench tg = getTileEntity( w, x, y, z );
		if ( tg != null )
		{
			if ( Platform.isServer() )
				Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_CELLWORKBENCH );
			return true;
		}
		return false;
	}

}
