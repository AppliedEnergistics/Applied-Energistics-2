package appeng.me.cluster.implementations;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.util.WorldCoord;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.MBCalculator;
import appeng.tile.qnb.TileQuantumBridge;
import appeng.util.Platform;

public class QuantumCalculator extends MBCalculator
{

	TileQuantumBridge tqb;

	public QuantumCalculator(IAEMultiBlock t) {
		super( t );
		tqb = (TileQuantumBridge) t;
	}

	@Override
	public boolean isValidTile(TileEntity te)
	{
		return te instanceof TileQuantumBridge;
	}

	@Override
	public boolean checkMultiblockScale(WorldCoord min, WorldCoord max)
	{

		if ( (max.x - min.x + 1) * (max.y - min.y + 1) * (max.z - min.z + 1) == 9 )
		{
			int ones = ((max.x - min.x) == 0 ? 1 : 0) + ((max.y - min.y) == 0 ? 1 : 0) + ((max.z - min.z) == 0 ? 1 : 0);

			int threes = ((max.x - min.x) == 2 ? 1 : 0) + ((max.y - min.y) == 2 ? 1 : 0) + ((max.z - min.z) == 2 ? 1 : 0);

			return ones == 1 && threes == 2;
		}
		return false;
	}

	@Override
	public void updateTiles(IAECluster cl, World w, WorldCoord min, WorldCoord max)
	{
		byte num = 0;
		byte ringNum = 0;
		QuantumCluster c = (QuantumCluster) cl;

		for (int x = min.x; x <= max.x; x++)
		{
			for (int y = min.y; y <= max.y; y++)
			{
				for (int z = min.z; z <= max.z; z++)
				{
					TileQuantumBridge te = (TileQuantumBridge) w.getBlockTileEntity( x, y, z );

					byte flags = 0;

					num++;
					if ( num == 5 )
					{
						flags = (byte) (num);
						c.setCenter( te );
					}
					else
					{
						if ( num == 1 || num == 3 || num == 7 || num == 9 )
							flags = (byte) (tqb.corner | num);
						else
							flags = (byte) (num);
						c.Ring[ringNum++] = te;
					}

					te.updateStatus( c, flags );
				}
			}
		}

	}

	@Override
	public IAECluster createCluster(World w, WorldCoord min, WorldCoord max)
	{
		return new QuantumCluster( min, max );
	}

	@Override
	public void disconnect()
	{
		tqb.disconnect();
	}

	@Override
	public boolean verifyInternalStructure(World w, WorldCoord min, WorldCoord max)
	{

		byte num = 0;

		for (int x = min.x; x <= max.x; x++)
		{
			for (int y = min.y; y <= max.y; y++)
			{
				for (int z = min.z; z <= max.z; z++)
				{
					IAEMultiBlock te = (IAEMultiBlock) w.getBlockTileEntity( x, y, z );

					if ( !te.isValid() )
						return false;

					num++;
					if ( num == 5 )
					{
						if ( !Platform.blockAtLocationIs( w, x, y, z, AEApi.instance().blocks().blockQuantumLink ) )
							return false;
					}
					else
					{
						if ( !Platform.blockAtLocationIs( w, x, y, z, AEApi.instance().blocks().blockQuantumRing ) )
							return false;
					}

				}
			}
		}
		return true;
	}

}
