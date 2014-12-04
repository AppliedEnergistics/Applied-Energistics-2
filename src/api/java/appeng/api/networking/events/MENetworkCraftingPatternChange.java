package appeng.api.networking.events;

import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingProvider;

public class MENetworkCraftingPatternChange extends MENetworkEvent
{

	public final ICraftingProvider provider;
	public final IGridNode node;

	public MENetworkCraftingPatternChange(ICraftingProvider p, IGridNode n) {
		provider = p;
		node = n;
	}

}
