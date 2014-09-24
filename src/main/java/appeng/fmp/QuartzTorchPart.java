package appeng.fmp;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.IRandomDisplayTick;
import codechicken.multipart.minecraft.McBlockPart;
import codechicken.multipart.minecraft.McSidedMetaPart;

public class QuartzTorchPart extends McSidedMetaPart implements IRandomDisplayTick
{

	public QuartzTorchPart() {
		this( ForgeDirection.DOWN.ordinal() );
	}

	public QuartzTorchPart(int meta) {
		super( meta );
	}

	@Override
	public boolean doesTick()
	{
		return false;
	}

	@Override
	public Block getBlock()
	{
		return AEApi.instance().blocks().blockQuartzTorch.block();
	}

	@Override
	public String getType()
	{
		return PartRegistry.QuartzTorchPart.getName();
	}

	@Override
	public Cuboid6 getBounds()
	{
		return getBounds( meta );
	}

	public Cuboid6 getBounds(int meta)
	{
		ForgeDirection up = ForgeDirection.getOrientation( meta );
		double xOff = -0.3 * up.offsetX;
		double yOff = -0.3 * up.offsetY;
		double zOff = -0.3 * up.offsetZ;
		return new Cuboid6( xOff + 0.3, yOff + 0.3, zOff + 0.3, xOff + 0.7, yOff + 0.7, zOff + 0.7 );
	}

	@Override
	public int sideForMeta(int meta)
	{
		return ForgeDirection.getOrientation( meta ).getOpposite().ordinal();
	}

	public static McBlockPart placement(World world, BlockCoord pos, int side)
	{
		pos = pos.copy().offset( side );
		if ( !world.isSideSolid( pos.x, pos.y, pos.z, ForgeDirection.getOrientation( side ) ) )
		{
			return null;
		}

		return new QuartzTorchPart( side );
	}

	@Override
	public void randomDisplayTick(Random r)
	{
		getBlock().randomDisplayTick( world(), x(), y(), z(), r );
	}
}