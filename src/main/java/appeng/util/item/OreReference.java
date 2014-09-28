package appeng.util.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.item.ItemStack;
import appeng.api.storage.data.IAEItemStack;

public class OreReference
{

	private LinkedList<ItemStack> otherOptions = new LinkedList<ItemStack>();
	private ArrayList<IAEItemStack> aeOtherOptions = null;
	private HashSet<Integer> ores = new HashSet<Integer>();

	public Collection<ItemStack> getEquivalents()
	{
		return otherOptions;
	}

	public List<IAEItemStack> getAEEquivalents()
	{
		if ( aeOtherOptions == null )
		{
			aeOtherOptions = new ArrayList<IAEItemStack>( otherOptions.size() );

			// SUMMON AE STACKS!
			for (ItemStack is : otherOptions)
				if ( is.getItem() != null )
					aeOtherOptions.add( AEItemStack.create( is ) );
		}

		return aeOtherOptions;
	}

	public Collection<Integer> getOres()
	{
		return ores;
	}

}
