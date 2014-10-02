package appeng.crafting;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.me.cluster.implementations.CraftingCPUCluster;

public class CraftingTreeNode
{

	// parent node.
	private final CraftingTreeProcess parent;
	private final World world;

	// what slot!
	final int slot;
	int bytes = 0;

	// what item is this?
	private final IAEItemStack what;

	// what are the crafting patterns for this?
	private final ArrayList<CraftingTreeProcess> nodes = new ArrayList<CraftingTreeProcess>();

	boolean canEmit = false;
	boolean cannotUse = false;

	long missing = 0;
	long howManyEmitted = 0;

	final CraftingJob job;
	final IItemList<IAEItemStack> used = AEApi.instance().storage().createItemList();
	boolean exhausted = false;

	boolean sim;

	public CraftingTreeNode(ICraftingGrid cc, CraftingJob job, IAEItemStack wat, CraftingTreeProcess par, int slot, int depth)
	{
		what = wat;
		parent = par;
		this.slot = slot;
		this.world = job.getWorld();
		this.job = job;
		sim = false;

		canEmit = cc.canEmitFor( what );
		if ( canEmit )
			return; // if you can emit for something, you can't make it with patterns.

		for (ICraftingPatternDetails details : cc.getCraftingFor( what, parent == null ? null : parent.details, slot, world ))// in
																																// order.
		{
			if ( parent == null || parent.notRecursive( details ) )
				nodes.add( new CraftingTreeProcess( cc, job, details, this, depth + 1, world ) );
		}

	}

	public IAEItemStack getStack(long size)
	{
		IAEItemStack is = what.copy();
		is.setStackSize( size );
		return is;
	}

	boolean notRecursive(ICraftingPatternDetails details)
	{
		IAEItemStack[] o = details.getCondensedOutputs();
		for (IAEItemStack i : o)
			if ( i.equals( what ) )
				return false;

		o = details.getCondensedInputs();
		for (IAEItemStack i : o)
			if ( i.equals( what ) )
				return false;

		if ( parent == null )
			return true;

		return parent.notRecursive( details );
	}

	public IAEItemStack request(MECraftingInventory inv, long l, BaseActionSource src) throws CraftBranchFailure, InterruptedException
	{
		job.handlePausing();

		List<IAEItemStack> thingsUsed = new LinkedList<IAEItemStack>();

		what.setStackSize( l );
		if ( slot >= 0 && parent != null && parent.details.isCraftable() )
		{
			for (IAEItemStack fuzz : inv.getItemList().findFuzzy( what, FuzzyMode.IGNORE_ALL ))
			{
				if ( parent.details.isValidItemForSlot( slot, fuzz.getItemStack(), world ) )
				{
					fuzz = fuzz.copy();
					fuzz.setStackSize( l );
					IAEItemStack available = inv.extractItems( fuzz, Actionable.MODULATE, src );

					if ( available != null )
					{
						if ( !exhausted )
						{
							IAEItemStack is = job.checkUse( available );
							if ( is != null )
							{
								thingsUsed.add( is.copy() );
								used.add( is );
							}
						}

						bytes += available.getStackSize();
						l -= available.getStackSize();

						if ( l == 0 )
							return available;
					}
				}
			}
		}
		else
		{
			IAEItemStack available = inv.extractItems( what, Actionable.MODULATE, src );

			if ( available != null )
			{
				if ( !exhausted )
				{
					IAEItemStack is = job.checkUse( available );
					if ( is != null )
					{
						thingsUsed.add( is.copy() );
						used.add( is );
					}
				}

				bytes += available.getStackSize();
				l -= available.getStackSize();

				if ( l == 0 )
					return available;
			}
		}

		if ( canEmit )
		{
			IAEItemStack wat = what.copy();
			wat.setStackSize( l );

			howManyEmitted = wat.getStackSize();
			bytes += wat.getStackSize();

			return wat;
		}

		exhausted = true;

		if ( nodes.size() == 1 )
		{
			CraftingTreeProcess pro = nodes.get( 0 );

			while (pro.possible && l > 0)
			{
				IAEItemStack madeWhat = pro.getAmountCrafted( what );

				pro.request( inv, pro.getTimes( l, madeWhat.getStackSize() ), src );

				madeWhat.setStackSize( l );
				IAEItemStack available = inv.extractItems( madeWhat, Actionable.MODULATE, src );

				if ( available != null )
				{
					bytes += available.getStackSize();
					l -= available.getStackSize();

					if ( l <= 0 )
						return available;
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
						MECraftingInventory subInv = new MECraftingInventory( inv, true, true, true );
						pro.request( subInv, 1, src );

						what.setStackSize( l );
						IAEItemStack available = subInv.extractItems( what, Actionable.MODULATE, src );

						if ( available != null )
						{
							if ( !subInv.commit( src ) )
								throw new CraftBranchFailure( what, l );

							bytes += available.getStackSize();
							l -= available.getStackSize();

							if ( l <= 0 )
								return available;
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

		if ( sim )
		{
			missing += l;
			bytes += l;
			IAEItemStack rv = what.copy();
			rv.setStackSize( l );
			return rv;
		}

		for (IAEItemStack o : thingsUsed)
		{
			job.refund( o.copy() );
			o.setStackSize( -o.getStackSize() );
			used.add( o );
		}

		throw new CraftBranchFailure( what, l );
	}

	public void dive(CraftingJob job)
	{
		if ( missing > 0 )
			job.addMissing( getStack( missing ) );
		// missing = 0;

		job.addBytes( 8 + bytes );

		for (CraftingTreeProcess pro : nodes)
			pro.dive( job );
	}

	public void setSimulate()
	{
		sim = true;
		missing = 0;
		bytes = 0;
		used.resetStatus();
		exhausted = false;

		for (CraftingTreeProcess pro : nodes)
			pro.setSimulate();
	}

	public void setJob(MECraftingInventory storage, CraftingCPUCluster craftingCPUCluster, BaseActionSource src) throws CraftBranchFailure
	{
		for (IAEItemStack i : used)
		{
			IAEItemStack ex = storage.extractItems( i, Actionable.MODULATE, src );

			if ( ex == null || ex.getStackSize() != i.getStackSize() )
				throw new CraftBranchFailure( i, i.getStackSize() );

			craftingCPUCluster.addStorage( ex );
		}

		if ( howManyEmitted > 0 )
		{
			IAEItemStack i = what.copy();
			i.setStackSize( howManyEmitted );
			craftingCPUCluster.addEmitable( i );
		}

		for (CraftingTreeProcess pro : nodes)
			pro.setJob( storage, craftingCPUCluster, src );
	}

	public void getPlan(IItemList<IAEItemStack> plan)
	{
		if ( missing > 0 )
		{
			IAEItemStack o = what.copy();
			o.setStackSize( missing );
			plan.add( o );
		}

		if ( howManyEmitted > 0 )
		{
			IAEItemStack i = what.copy();
			i.setCountRequestable( howManyEmitted );
			plan.addRequestable( i );
		}

		for (IAEItemStack i : used)
			plan.add( i.copy() );

		for (CraftingTreeProcess pro : nodes)
			pro.getPlan( plan );
	}
}
