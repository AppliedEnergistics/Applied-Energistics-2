package appeng.block.grindstone;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.grindstone.TileGrinder;
import appeng.util.Platform;

public class BlockGrinder extends AEBaseBlock
{

	public BlockGrinder() {
		super( BlockGrinder.class, Material.rock );
		setfeature( EnumSet.of( AEFeature.GrindStone ) );
		setTileEntity( TileGrinder.class );
		setHardness( 3.2F );
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		TileGrinder tg = getTileEntity( w, x, y, z );
		if ( tg != null && !p.isSneaking() )
		{
			Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_GRINDER );
			return true;
		}
		return false;
	}

}
