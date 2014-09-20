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

	private LinkedList<ItemStack> otherOptions = new LinkedList();
	private ArrayList aeotherOptions = null;
	private HashSet<Integer> ores = new HashSet<Integer>();

	public Collection<ItemStack> getEquivilients()
	{
		return otherOptions;
	}

	public List<IAEItemStack> getAEEquivilients()
	{
		if ( aeotherOptions == null )
		{
			aeotherOptions = new ArrayList( otherOptions.size() );

			// SUMMON AE STACKS!
			for (ItemStack is : otherOptions)
				if ( is.getItem() != null )
					aeotherOptions.add( AEItemStack.create( is ) );
		}

		return aeotherOptions;
	}

	public Collection<Integer> getOres()
	{
		return ores;
	}

}
