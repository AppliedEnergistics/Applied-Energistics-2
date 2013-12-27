package appeng.block.misc;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.tile.misc.TileInscriber;
import appeng.util.Platform;

public class BlockInscriber extends AEBaseBlock
{

	public BlockInscriber() {
		super( BlockInscriber.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.Inscriber ) );
		setTileEntiy( TileInscriber.class );
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if ( player.isSneaking() )
			return false;

		if ( Platform.isServer() )
		{
			TileInscriber tc = getTileEntity( w, x, y, z );
			if ( tc != null )
			{
				tc.activate( player );
			}
		}

		return true;
	}

}
