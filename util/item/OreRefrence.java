package appeng.util.item;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import appeng.api.storage.data.IAEItemStack;

public class OreRefrence
{

	LinkedList<ItemStack> otherOptions = new LinkedList();
	LinkedList<IAEItemStack> aeotherOptions = new LinkedList();
	HashSet<Integer> ores = new HashSet<Integer>();

	public Collection<ItemStack> getEquivilients()
	{
		return otherOptions;
	}

	public Collection<IAEItemStack> getAEEquivilients()
	{
		return aeotherOptions;
	}

	public Collection<Integer> getOres()
	{
		return ores;
	}

}
