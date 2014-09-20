package appeng.block.misc;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.misc.TileCondenser;
import appeng.util.Platform;

public class BlockCondenser extends AEBaseBlock
{

	public BlockCondenser() {
		super( BlockCondenser.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.Core ) );
		setTileEntity( TileCondenser.class );
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if ( player.isSneaking() )
			return false;

		if ( Platform.isServer() )
		{
			TileCondenser tc = getTileEntity( w, x, y, z );
			if ( tc != null && !player.isSneaking() )
			{
				Platform.openGUI( player, tc, ForgeDirection.getOrientation(side), GuiBridge.GUI_CONDENSER );
				return true;
			}
		}

		return true;
	}

}
