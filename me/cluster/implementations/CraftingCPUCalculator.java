package appeng.me.cluster.implementations;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import appeng.api.util.WorldCoord;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.MBCalculator;
import appeng.tile.crafting.TileCraftingTile;
import appeng.tile.qnb.TileQuantumBridge;

public class CraftingCPUCalculator extends MBCalculator
{

	TileCraftingTile tqb;

	public CraftingCPUCalculator(IAEMultiBlock t) {
		super( t );
		tqb = (TileCraftingTile) t;
	}

	@Override
	public boolean isValidTile(TileEntity te)
	{
		return te instanceof TileQuantumBridge;
	}

	@Override
	public boolean checkMultiblockScale(WorldCoord min, WorldCoord max)
	{
		return true;
	}

	@Override
	public void updateTiles(IAECluster cl, World w, WorldCoord min, WorldCoord max)
	{

	}

	@Override
	public IAECluster createCluster(World w, WorldCoord min, WorldCoord max)
	{
		return new CraftingCPUCluster( min, max );
	}

	@Override
	public void disconnect()
	{
		tqb.disconnect();
	}

	@Override
	public boolean verifyInternalStructure(World w, WorldCoord min, WorldCoord max)
	{

		for (int x = min.x; x <= max.x; x++)
		{
			for (int y = min.y; y <= max.y; y++)
			{
				for (int z = min.z; z <= max.z; z++)
				{
					IAEMultiBlock te = (IAEMultiBlock) w.getTileEntity( x, y, z );

					if ( !te.isValid() )
						return false;

				}
			}
		}

		return true;
	}

}
