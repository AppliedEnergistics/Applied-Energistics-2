package appeng.block.misc;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.tile.misc.TileInscriber;
import appeng.tile.misc.TileInterface;

public class BlockInscriber extends AEBaseBlock
{

	public BlockInscriber() {
		super( BlockInscriber.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.Inscriber ) );
		setTileEntiy( TileInscriber.class );
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		if ( p.isSneaking() )
			return false;

		TileInterface tg = getTileEntity( w, x, y, z );
		if ( tg != null )
		{
			// if ( Platform.isServer() )
			// Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_INSCRIBER );
			return true;
		}
		return false;
	}

}
