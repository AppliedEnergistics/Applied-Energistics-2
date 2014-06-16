package appeng.crafting;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.cache.CraftingCache;

public class CraftingTreeProcess
{

	CraftingTreeNode parent;
	ICraftingPatternDetails details;
	CraftingJob job;

	int crafts = 0;
	final private int depth;

	Map<CraftingTreeNode, Long> nodes = new HashMap();
	public boolean possible = true;

	public CraftingTreeProcess(CraftingCache cc, CraftingJob job, ICraftingPatternDetails details, CraftingTreeNode craftingTreeNode, int depth) {
		parent = craftingTreeNode;
		this.details = details;
		this.job = job;
		this.depth = depth;

		for (IAEItemStack part : details.getCondencedInputs())
			nodes.put( new CraftingTreeNode( cc, job, part.copy(), this, depth + 1 ), part.getStackSize() );
	}

	public boolean notRecurive(ICraftingPatternDetails details)
	{
		return parent.notRecurive( details );
	}

	IAEItemStack getAmountCrafted(IAEItemStack what2)
	{
		for (IAEItemStack is : details.getCondencedOutputs())
		{
			if ( is.equals( what2 ) )
			{
				what2 = what2.copy();
				what2.setStackSize( is.getStackSize() );
				return what2;
			}
		}

		throw new RuntimeException( "Crafting Tree construction failed." );
	}

	public void request(MECraftingInventory inv, long i, BaseActionSource src) throws CraftBranchFailure
	{
		// request and remove inputs...
		for (Entry<CraftingTreeNode, Long> entry : nodes.entrySet())
		{
			IAEItemStack item = entry.getKey().getStack( entry.getValue() );
			entry.getKey().request( inv, item.getStackSize() * i, src );
		}

		// assume its possible.

		// add crafting results..
		for (IAEItemStack out : details.getCondencedOutputs())
		{
			IAEItemStack o = out.copy();
			o.setStackSize( o.getStackSize() * i );
			inv.injectItems( o, Actionable.MODULATE, src );
		}

		crafts += i;
	}

	public void dive(CraftingJob job)
	{
		job.addTask( getAmountCrafted( parent.getStack( 1 ) ), crafts, details, depth );
		for (CraftingTreeNode pro : nodes.keySet())
			pro.dive( job );
	}
}
