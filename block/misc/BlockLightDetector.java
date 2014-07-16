package appeng.block.misc;

import java.util.EnumSet;
import java.util.Random;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import appeng.core.features.AEFeature;
import appeng.tile.misc.TileLightDetector;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockLightDetector extends BlockQuartzTorch
{

	public BlockLightDetector() {
		super( BlockLightDetector.class );
		setfeature( EnumSet.of( AEFeature.LightDetector ) );
		setTileEntiy( TileLightDetector.class );
	}

	@Override
	public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ)
	{
		super.onNeighborChange( world, x, y, z, tileX, tileY, tileZ );

		TileLightDetector tld = getTileEntity( world, x, y, z );
		if ( tld != null )
			tld.updateLight();
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess w, int x, int y, int z, int side)
	{
		if ( w instanceof World && ((TileLightDetector) getTileEntity( w, x, y, z )).isReady() )
			return (int) ((World) w).getBlockLightValue( x, y, z ) - 6;

		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World w, int x, int y, int z, Random r)
	{
		// cancel out lightning
	}

}
