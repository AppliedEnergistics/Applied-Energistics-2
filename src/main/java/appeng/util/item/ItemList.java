package appeng.util.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import net.minecraftforge.oredict.OreDictionary;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

public final class ItemList<StackType extends IAEStack> implements IItemList<StackType>
{

	private final TreeMap<StackType, StackType> records = new TreeMap<StackType, StackType>();
	private final Class<? extends IAEStack> clz;

	// private int currentPriority = Integer.MIN_VALUE;

	int iteration = Integer.MIN_VALUE;
	public Throwable stacktrace;

	public ItemList(Class<? extends IAEStack> cla) {
		clz = cla;
	}

	private boolean checkStackType(StackType st)
	{
		if ( st == null )
			return true;

		if ( !clz.isInstance( st ) )
			throw new RuntimeException( "WRONG TYPE - got " + st.getClass().getName() + " expected " + clz.getName() );

		return false;
	}

	@Override
	synchronized public void add(StackType option)
	{
		if ( checkStackType( option ) )
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
		if ( checkStackType( option ) )
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
		if ( checkStackType( option ) )
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
		if ( checkStackType( option ) )
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
		for (StackType stackType : this)
		{
			return stackType;
		}
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
	 * i.next(); if ( !AEI.isMeaningful() ) i.remove(); } }
	 */

	@Override
	synchronized public Iterator<StackType> iterator()
	{
		return new MeaningfulIterator<StackType>( records.values().iterator() );
	}

	@Override
	synchronized public StackType findPrecise(StackType i)
	{
		if ( checkStackType( i ) )
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
		return !iterator().hasNext();
	}

	public Collection<StackType> findFuzzyDamage(AEItemStack filter, FuzzyMode fuzzy, boolean ignoreMeta)
	{
		StackType low = (StackType) filter.getLow( fuzzy, ignoreMeta );
		StackType high = (StackType) filter.getHigh( fuzzy, ignoreMeta );
		return records.subMap( low, true, high, true ).descendingMap().values();
	}

	@Override
	public Collection<StackType> findFuzzy(StackType filter, FuzzyMode fuzzy)
	{
		if ( checkStackType( filter ) )
			return new ArrayList<StackType>();

		if ( filter instanceof IAEFluidStack )
			return filter.equals( this ) ? (List<StackType>) Arrays.asList( new IAEFluidStack[] { (IAEFluidStack) filter } ) : (List<StackType>) Arrays
					.asList( new IAEFluidStack[] {} );

		AEItemStack ais = (AEItemStack) filter;
		if ( ais.isOre() )
		{
			OreReference or = ais.def.isOre;
			if ( or.getAEEquivalents().size() == 1 )
			{
				IAEItemStack is = or.getAEEquivalents().get( 0 );
				return findFuzzyDamage( (AEItemStack) is, fuzzy, is.getItemDamage() == OreDictionary.WILDCARD_VALUE );
			}
			else
			{
				Collection<StackType> output = new LinkedList<StackType>();

				for (IAEItemStack is : or.getAEEquivalents())
					output.addAll( findFuzzyDamage( (AEItemStack) is, fuzzy, is.getItemDamage() == OreDictionary.WILDCARD_VALUE ) );

				return output;
			}
		}

		return findFuzzyDamage( ais, fuzzy, false );
	}
}
