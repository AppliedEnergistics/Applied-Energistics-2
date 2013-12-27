package appeng.debug;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.core.features.AEFeature;
import appeng.helpers.TickHandler;
import appeng.items.AEBaseItem;
import appeng.me.Grid;
import appeng.me.GridCacheWrapper;
import appeng.me.GridNode;
import appeng.me.cache.TickManagerCache;
import appeng.util.Platform;

public class ToolDebugCard extends AEBaseItem
{

	public ToolDebugCard() {
		super( ToolDebugCard.class );
		setfeature( EnumSet.of( AEFeature.Creative ) );
	}

	public String timeMeasurement(long nanos)
	{
		int ms = (int) (nanos / 1000000);
		if ( ms <= 0 )
			return nanos + "ns";
		return ms + "ms";
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if ( Platform.isClient() )
			return false;

		if ( player.isSneaking() )
		{
			int grids = 0;
			int totalNodes = 0;
			long totalOver5Ticks = 0;

			for (Grid g : TickHandler.instance.getGridList())
			{
				TickManagerCache tmc = (TickManagerCache) g.getCache( ITickManager.class );

				for (GridCacheWrapper w : g.getCacheWrappers())
				{
					totalOver5Ticks += w.LastFiveTicksTime;
				}

				grids++;
				for (IGridNode n : g.getNodes())
				{
					totalOver5Ticks += tmc.getAvgNanoTime( n );
					totalNodes++;
				}
			}
			outputMsg( player, "Grids: " + grids );
			outputMsg( player, "Total Nodes: " + totalNodes );
			outputMsg( player, "Average Time Used: " + " - " + timeMeasurement( totalOver5Ticks / 5 ) );
		}
		else
		{
			TileEntity te = world.getBlockTileEntity( x, y, z );

			if ( te instanceof IGridHost )
			{
				GridNode node = (GridNode) ((IGridHost) te).getGridNode( ForgeDirection.getOrientation( side ) );
				if ( node != null )
				{
					Grid g = node.getInternalGrid();
					IGridNode center = g.getPivot();
					outputMsg( player, "This Node: " + node.toString() );
					outputMsg( player, "Center Node: " + center.toString() );

					for (GridCacheWrapper w : g.getCacheWrappers())
					{
						outputMsg( player, w.getName() + "Tick: " + timeMeasurement( w.LastFiveTicksTime / 5 ) + ", Add: "
								+ timeMeasurement( w.LastFiveAddNode / 5 ) + ", Rmv: " + timeMeasurement( w.LastFiveRemoveTime / 5 ) );
					}

					TickManagerCache tmc = (TickManagerCache) g.getCache( ITickManager.class );
					for (Class c : g.getMachineClasses())
					{
						int o = 0;
						long nanos = 0;
						for (IGridNode oj : g.getMachines( c ))
						{
							o++;
							nanos += tmc.getAvgNanoTime( oj );
						}

						if ( nanos < 0 )
						{
							outputMsg( player, c.getSimpleName() + " - " + o );
						}
						else
						{
							outputMsg( player, c.getSimpleName() + " - " + o + "; " + timeMeasurement( nanos ) );
						}
					}
				}
				else
					outputMsg( player, "No Node Available." );
			}
			else
				outputMsg( player, "Not Networked Block" );

			if ( te instanceof IPartHost )
			{
				IPart center = ((IPartHost) te).getPart( ForgeDirection.UNKNOWN );
				((IPartHost) te).markForUpdate();
				if ( center != null )
				{
					GridNode n = (GridNode) center.getGridNode();
					outputMsg( player, "Node Channels: " + n.usedChannels() );
					for (IGridConnection gc : n.getConnections())
					{
						ForgeDirection fd = gc.getDirection( n );
						if ( fd != ForgeDirection.UNKNOWN )
							outputMsg( player, fd.toString() + ": " + gc.getUsedChannels() );
					}
				}
			}

			if ( te instanceof IAEPowerStorage )
			{
				IAEPowerStorage ps = (IAEPowerStorage) te;
				outputMsg( player, "Energy: " + ps.getAECurrentPower() + " / " + ps.getAEMaxPower() );

				if ( te instanceof IGridHost )
				{
					IGridNode node = (IGridNode) ((IGridHost) te).getGridNode( ForgeDirection.getOrientation( side ) );
					if ( node != null && node.getGrid() != null )
					{
						IEnergyGrid eg = node.getGrid().getCache( IEnergyGrid.class );
						outputMsg( player, "GridEnerg: " + eg.getStoredPower() + " : " + eg.getEnergyDemand( Double.MAX_VALUE ) );
					}
				}
			}
		}
		return true;
	}

	private void outputMsg(EntityPlayer player, String string)
	{
		player.sendChatToPlayer( ChatMessageComponent.createFromText( string ) );
	}

}
