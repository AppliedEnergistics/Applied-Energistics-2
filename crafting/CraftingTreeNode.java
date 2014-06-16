package appeng.crafting;

import java.util.ArrayList;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.cache.CraftingCache;

public class CraftingTreeNode
{

	// parent node.
	private CraftingTreeProcess parent;

	// what item is this?
	private IAEItemStack what;

	// what are the crafting patterns for this?
	private ArrayList<CraftingTreeProcess> nodes = new ArrayList();

	boolean cannotUse = false;
	int missing = 0;

	public CraftingTreeNode(CraftingCache cc, CraftingJob job, IAEItemStack wat, CraftingTreeProcess par, int depth) {
		what = wat;
		parent = par;

		for (ICraftingPatternDetails details : cc.getCraftingFor( what ))// in order.
		{
			if ( notRecurive( details ) )
				nodes.add( new CraftingTreeProcess( cc, job, details, this, depth + 1 ) );
		}
	}

	public IAEItemStack getStack(long size)
	{
		IAEItemStack is = what.copy();
		is.setStackSize( size );
		return is;
	}

	boolean notRecurive(ICraftingPatternDetails details)
	{
		IAEItemStack[] o = details.getOutputs();
		for (IAEItemStack i : o)
			if ( i.equals( what ) )
				return true;

		if ( parent == null )
			return false;

		return parent.notRecurive( details );
	}

	private long getTimes(long remaining, long stackSize)
	{
		return (remaining / stackSize) + (remaining % stackSize != 0 ? 1 : 0);
	}

	public void request(MECraftingInventory inv, long l, BaseActionSource src) throws CraftBranchFailure
	{
		what.setStackSize( l );
		IAEItemStack available = inv.extractItems( what, Actionable.MODULATE, src );

		if ( available != null )
			l -= available.getStackSize();

		if ( l == 0 )
			return;

		if ( nodes.size() == 1 )
		{
			CraftingTreeProcess pro = nodes.get( 0 );

			while (pro.possible && l > 0)
			{
				pro.request( inv, getTimes( l, pro.getAmountCrafted( what ).getStackSize() ), src );

				what.setStackSize( l );
				available = inv.extractItems( what, Actionable.MODULATE, src );

				if ( available != null )
				{
					l -= available.getStackSize();

					if ( l <= 0 )
						return;
				}
				else
					pro.possible = false; // ;P
			}
		}
		else if ( nodes.size() > 1 )
		{
			for (CraftingTreeProcess pro : nodes)
			{
				try
				{
					while (pro.possible && l > 0)
					{
						MECraftingInventory subInv = new MECraftingInventory( inv );
						pro.request( subInv, 1, src );
						subInv.commit( src );

						what.setStackSize( l );
						available = inv.extractItems( what, Actionable.MODULATE, src );

						if ( available != null )
						{
							l -= available.getStackSize();

							if ( l <= 0 )
								return;
						}
						else
							pro.possible = false; // ;P
					}
				}
				catch (CraftBranchFailure fail)
				{
					pro.possible = true;
				}
			}
		}

		missing += l;
		// throw new CraftBranchFailure( what, l );
	}

	public void dive(CraftingJob job)
	{
		if ( missing > 0 )
			job.addMissing( getStack( missing ) );
		missing = 0;

		for (CraftingTreeProcess pro : nodes)
			pro.dive( job );
	}
}
