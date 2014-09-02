package appeng.me.cache.helpers;

import net.minecraft.crash.CrashReportCategory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.DimensionalCoord;
import appeng.me.cache.TickManagerCache;
import appeng.parts.AEBasePart;

public class TickTracker implements Comparable<TickTracker>
{

	public final TickingRequest request;
	public final IGridTickable gt;
	public final IGridNode node;
	public final TickManagerCache host;

	public long LastFiveTicksTime = 0;

	public long lastTick;
	public int current_rate;

	public TickTracker(TickingRequest req, IGridNode node, IGridTickable gt, long currentTick, TickManagerCache tickManagerCache) {
		request = req;
		this.gt = gt;
		this.node = node;
		current_rate = (req.minTickRate + req.maxTickRate) / 2;
		lastTick = currentTick;
		host = tickManagerCache;
	}

	public long getAvgNanos()
	{
		return (LastFiveTicksTime / 5);
	}

	public void setRate(int rate)
	{
		current_rate = rate;

		if ( current_rate < request.minTickRate )
			current_rate = request.minTickRate;

		if ( current_rate > request.maxTickRate )
			current_rate = request.maxTickRate;
	}

	@Override
	public int compareTo(TickTracker t)
	{
		int nextTick = (int) ((lastTick - host.getCurrentTick()) + current_rate);
		int ts_nextTick = (int) ((t.lastTick - host.getCurrentTick()) + t.current_rate);
		return nextTick - ts_nextTick;
	}

	public void addEntityCrashInfo(CrashReportCategory crashreportcategory)
	{
		if ( gt instanceof AEBasePart )
		{
			AEBasePart part = (AEBasePart)gt;
			part.addEntityCrashInfo( crashreportcategory );
		}
		
		crashreportcategory.addCrashSection( "CurrentTickRate", current_rate );
		crashreportcategory.addCrashSection( "MinTickRate", request.minTickRate );
		crashreportcategory.addCrashSection( "MaxTickRate", request.maxTickRate );
		crashreportcategory.addCrashSection( "MachineType", gt.getClass().getName() );
		crashreportcategory.addCrashSection( "GridBlockType", node.getGridBlock().getClass().getName() );
		crashreportcategory.addCrashSection( "ConnectedSides", node.getConnectedSides() );
		
		DimensionalCoord dc = node.getGridBlock().getLocation();
		if ( dc != null )
			crashreportcategory.addCrashSection( "Location", dc );
	}
};
