package appeng.container.implementations;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.util.ItemSorters;

public class CraftingCPURecord implements Comparable<CraftingCPURecord>
{

	ICraftingCPU cpu;

	long size;
	int processors;

	public String myName;

	public CraftingCPURecord(long size, int proc, ICraftingCPU server) {
		this.size = size;
		this.processors = proc;
		this.cpu = server;
		myName = server.getName();
	}

	@Override
	public int compareTo(CraftingCPURecord o)
	{
		int a = ItemSorters.compareLong( o.processors, processors );
		if ( a != 0 )
			return a;
		return ItemSorters.compareLong( o.size, size );
	}

}