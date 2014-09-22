package appeng.util.item;

import java.util.Collection;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemContainer;

public class ItemModList implements IItemContainer<IAEItemStack>
{

	final IItemContainer<IAEItemStack> backingStore;
	final IItemContainer<IAEItemStack> overrides = AEApi.instance().storage().createItemList();

	public ItemModList(IItemContainer<IAEItemStack> backend) {
		backingStore = backend;
	}

	@Override
	public void add(IAEItemStack option)
	{
		IAEItemStack over = overrides.findPrecise( option );
		if ( over == null )
		{
			over = backingStore.findPrecise( option );
			if ( over == null )
				overrides.add( option );
			else
			{
				option.add( over );
				overrides.add( option );
			}
		}
		else
			overrides.add( option );
	}

	@Override
	public IAEItemStack findPrecise(IAEItemStack i)
	{
		IAEItemStack over = overrides.findPrecise( i );
		if ( over == null )
			return backingStore.findPrecise( i );
		return over;
	}

	@Override
	public Collection<IAEItemStack> findFuzzy(IAEItemStack input, FuzzyMode fuzzy)
	{
		return overrides.findFuzzy( input, fuzzy );
	}

	@Override
	public boolean isEmpty()
	{
		return overrides.isEmpty() && backingStore.isEmpty();
	}

}
