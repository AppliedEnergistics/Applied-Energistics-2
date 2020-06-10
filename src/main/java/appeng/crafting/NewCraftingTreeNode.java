package appeng.crafting;


import java.util.ArrayList;
import java.util.List;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import net.minecraft.world.World;


public class NewCraftingTreeNode
{

	// what slot!
	private final int slot;
	private final CraftingJob job;
	private final IItemList<IAEItemStack> used = AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createList();
	// parent node.
	private final CraftingTreeProcess parent;
	private final World world;
	// what item is this?
	private final IAEItemStack what;
	// what are the crafting patterns for this?
	private final ArrayList<CraftingTreeProcess> nodes = new ArrayList<>();
	private int bytes = 0;
	private boolean canEmit = false;
	private long missing = 0;
	private long howManyEmitted = 0;
	private boolean exhausted = false;

	private boolean sim;

	public NewCraftingTreeNode( final ICraftingGrid cc, final CraftingJob job, final IAEItemStack wat, final CraftingTreeProcess par, final int slot, final int depth )
	{
      this.what = wat;
      this.parent = par;
      this.slot = slot;
      this.world = job.getWorld();
      this.job = job;
      this.sim = false;

      this.canEmit = cc.canEmitFor( this.what );
	}
}
