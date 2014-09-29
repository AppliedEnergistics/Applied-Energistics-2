package appeng.util.prioitylist;

import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

import java.util.ArrayList;

public class ModPriorityList<T extends IAEStack<T>> implements IPartitionList<T>
{

	final IItemList<T> list;
	final ArrayList<String> mods = new ArrayList<String>();

	public ModPriorityList(IItemList<T> in) {
		list = in;
		for( T item : in ) {
			String modid = getMod(item);

			if(modid != null && !mods.contains(modid) )
				mods.add(modid);
		}
	}

	public boolean isListed(T input)
	{
		return mods.contains(getMod(input));
	}

	private String getMod(IAEStack input) {
		String modid;
		if ( input.isItem() )
			modid = GameRegistry.findUniqueIdentifierFor(((AEItemStack) input).getItem()).modId;
		else if ( input.isFluid() )
			modid = GameRegistry.findUniqueIdentifierFor(Item.getItemFromBlock(((AEFluidStack) input).getFluid().getBlock())).modId;
		else return null;

		return modid;
	}

	@Override
	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	@Override
	public Iterable<T> getItems()
	{
		return list;
	}

}
