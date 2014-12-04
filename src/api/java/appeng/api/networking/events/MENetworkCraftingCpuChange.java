package appeng.api.networking.events;

import appeng.api.networking.IGridNode;

public class MENetworkCraftingCpuChange extends MENetworkEvent
{

	public final IGridNode node;

	public MENetworkCraftingCpuChange(IGridNode n) {
		node = n;
	}

}
