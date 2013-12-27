package appeng.util.item;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

public final class ItemList<StackType extends IAEStack> implements IItemList<StackType>
{

	private final TreeMap<StackType, StackType> records = new TreeMap();
	// private int currentPriority = Integer.MIN_VALUE;

	int iteration = Integer.MIN_VALUE;
	public Throwable stacktrace;

	@Override
	synchronized public void add(StackType option)
	{
		if ( option == null )
			return;

		StackType st = records.get( option );

		if ( st != null )
		{
			// st.setPriority( currentPriority );
			st.add( option );
			return;
		}

		StackType opt = (StackType) option.copy();
		// opt.setPriority( currentPriority );
		records.put( opt, opt );
	}

	@Override
	synchronized public void addStorage(StackType option) // adds a stack as
															// stored.
	{
		if ( option == null )
			return;

		StackType st = records.get( option );

		if ( st != null )
		{
			// st.setPriority( currentPriority );
			st.incStackSize( option.getStackSize() );
			return;
		}

		StackType opt = (StackType) option.copy();
		// opt.setPriority( currentPriority );
		records.put( opt, opt );
	}

	@Override
	synchronized public void addCrafting(StackType option) // adds a stack as
															// craftable.
	{
		if ( option == null )
			return;

		StackType st = records.get( option );

		if ( st != null )
		{
			// st.setPriority( currentPriority );
			st.setCraftable( true );
			return;
		}

		StackType opt = (StackType) option.copy();
		// opt.setPriority( currentPriority );
		opt.setStackSize( 0 );
		opt.setCraftable( true );

		records.put( opt, opt );
	}

	@Override
	synchronized public void addRequestable(StackType option) // adds a stack
																// as
																// requestable.
	{
		if ( option == null )
			return;

		StackType st = records.get( option );

		if ( st != null )
		{
			// st.setPriority( currentPriority );
			((IAEItemStack) st).setCountRequestable( ((IAEItemStack) st).getCountRequestable() + ((IAEItemStack) option).getCountRequestable() );
			return;
		}

		StackType opt = (StackType) option.copy();
		// opt.setPriority( currentPriority );
		opt.setStackSize( 0 );
		opt.setCraftable( false );
		opt.setCountRequestable( opt.getCountRequestable() );

		records.put( opt, opt );
	}

	@Override
	synchronized public StackType getFirstItem()
	{
		Iterator<StackType> i = this.iterator();
		while (i.hasNext())
			return i.next();
		return null;
	}

	@Override
	synchronized public void resetStatus()
	{
		for (StackType i : this)
			i.reset();
	}

	/*
	 * synchronized public void clean() { Iterator<StackType> i = iterator(); while (i.hasNext()) { StackType AEI =
	 * i.next(); if ( !AEI.isMeaninful() ) i.remove(); } }
	 */

	@Override
	synchronized public Iterator iterator()
	{
		return new MeanfulIterator( records.values().iterator() );
	}

	@Override
	synchronized public StackType findPrecise(StackType i)
	{
		if ( i == null )
			return null;

		StackType is = records.get( i );
		if ( is != null )
		{
			return is;
		}

		return null;
	}

	@Override
	synchronized public int size()
	{
		return records.values().size();
	}

	@Override
	public boolean isEmpty()
	{
		return records.isEmpty();
	}

	@Override
	public Collection<StackType> findFuzzy(StackType filter, FuzzyMode fuzzy)
	{
		if ( filter instanceof IAEFluidStack )
			return filter.equals( this ) ? (List<StackType>) Arrays.asList( new IAEFluidStack[] { (IAEFluidStack) filter } ) : (List<StackType>) Arrays
					.asList( new IAEFluidStack[] {} );

		AEItemStack ais = (AEItemStack) filter;

		if ( OreHelper.instance.isOre( ais ) )
		{

		}

		StackType low = (StackType) ais.getLow( fuzzy );
		StackType high = (StackType) ais.getHigh( fuzzy );
		return records.subMap( low, high ).values();
	}
}
