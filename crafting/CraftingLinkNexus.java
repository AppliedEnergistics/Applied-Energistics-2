package appeng.crafting;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.me.cache.CraftingGridCache;

public class CraftingLinkNexus
{

	public CraftingLinkNexus(String craftID) {
		this.CraftID = craftID;
	}

	public final String CraftID;

	boolean canceled = false;
	boolean done = false;

	int tickOfDeath = 0;

	CraftingLink req;
	CraftingLink cpu;

	public boolean isDead(IGrid g, CraftingGridCache craftingGridCache)
	{
		if ( isCanceled() || isDone() )
			return true;

		if ( req == null || cpu == null )
			tickOfDeath++;
		else
		{
			boolean hasCpu = craftingGridCache.hasCpu( cpu.cpu );
			boolean hasMachine = req.req.getActionableNode().getGrid() == g;

			if ( hasCpu && hasMachine )
				tickOfDeath = 0;
			else
				tickOfDeath += 60;
		}

		if ( tickOfDeath > 60 )
		{
			cancel();
			return true;
		}

		return false;
	}

	public void remove(CraftingLink craftingLink)
	{
		if ( req == craftingLink )
			req = null;
		else if ( cpu == craftingLink )
			cpu = null;
	}

	public void add(CraftingLink craftingLink)
	{
		if ( craftingLink.cpu != null )
			cpu = craftingLink;
		else if ( craftingLink.req != null )
			req = craftingLink;
	}

	public boolean isCanceled()
	{
		return canceled;
	}

	public boolean isDone()
	{
		return done;
	}

	public void markDone()
	{
		done = true;

		if ( req != null )
		{
			req.done = true;
			if ( req.req != null )
				req.req.jobStateChange( req );
		}

		if ( cpu != null )
			cpu.done = true;
	}

	public void cancel()
	{
		canceled = true;

		if ( req != null )
		{
			req.canceled = true;
			if ( req.req != null )
				req.req.jobStateChange( req );
		}

		if ( cpu != null )
			cpu.canceled = true;
	}

	public boolean isMachine(IGridHost machine)
	{
		return req == machine;
	}

	public void removeNode()
	{
		if ( req != null )
			req.setNextus( null );

		req = null;
		tickOfDeath = 0;
	}

}
