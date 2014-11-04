package appeng.crafting;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.Platform;
import cpw.mods.fml.common.FMLCommonHandler;

public class CraftingTreeProcess
{

	World world;
	final CraftingTreeNode parent;
	final ICraftingPatternDetails details;
	final CraftingJob job;

	long crafts = 0;
	boolean containerItems;
	boolean limitQty;
	boolean fullSimulation;

	private long bytes = 0;
	final private int depth;

	final Map<CraftingTreeNode, Long> nodes = new HashMap<CraftingTreeNode, Long>();
	public boolean possible = true;

	public CraftingTreeProcess(ICraftingGrid cc, CraftingJob job, ICraftingPatternDetails details, CraftingTreeNode craftingTreeNode, int depth, World world) {
		parent = craftingTreeNode;
		this.details = details;
		this.job = job;
		this.depth = depth;
		world = job.getWorld();

		if ( details.isCraftable() )
		{
			IAEItemStack list[] = details.getInputs();

			InventoryCrafting ic = new InventoryCrafting( new ContainerNull(), 3, 3 );
			IAEItemStack[] is = details.getInputs();
			for (int x = 0; x < ic.getSizeInventory(); x++)
				ic.setInventorySlotContents( x, is[x] == null ? null : is[x].getItemStack() );

			FMLCommonHandler.instance().firePlayerCraftingEvent( Platform.getPlayer( (WorldServer) world ), details.getOutput( ic, world ), ic );

			for (int x = 0; x < ic.getSizeInventory(); x++)
			{
				ItemStack g = ic.getStackInSlot( x );
				if ( g != null && g.stackSize > 1 )
					fullSimulation = true;
			}

			for ( IAEItemStack part : details.getCondensedInputs() )
			{
				ItemStack g = part.getItemStack();

				boolean isAnInput = false;
				for ( IAEItemStack a : details.getCondensedOutputs() )
				{
					if ( g != null && a != null && a.equals( g ) )
						isAnInput = true;
				}

				if ( isAnInput )
					limitQty = true;

				if ( g.getItem().hasContainerItem( g ) )
					limitQty = containerItems = true;
			}

			boolean complicated = false;

			if ( containerItems || complicated )
			{
				for (int x = 0; x < list.length; x++)
				{
					IAEItemStack part = list[x];
					if ( part != null )
						nodes.put( new CraftingTreeNode( cc, job, part.copy(), this, x, depth + 1 ), part.getStackSize() );
				}
			}
			else
			{
				// this is minor different then below, this slot uses the pattern, but kinda fudges it.
				for (IAEItemStack part : details.getCondensedInputs())
				{
					for (int x = 0; x < list.length; x++)
					{
						IAEItemStack comparePart = list[x];
						if ( part != null && part.equals( comparePart ) )
						{
							// use the first slot...
							nodes.put( new CraftingTreeNode( cc, job, part.copy(), this, x, depth + 1 ), part.getStackSize() );
							break;
						}
					}
				}
			}
		}
		else
		{
			for ( IAEItemStack part : details.getCondensedInputs() )
			{
				ItemStack g = part.getItemStack();

				boolean isAnInput = false;
				for (IAEItemStack a : details.getCondensedOutputs())
				{
					if ( g != null && a != null && a.equals( g ) )
						isAnInput = true;
				}

				if ( isAnInput )
					limitQty = true;
			}

			for (IAEItemStack part : details.getCondensedInputs())
			{
				nodes.put( new CraftingTreeNode( cc, job, part.copy(), this, -1, depth + 1 ), part.getStackSize() );
			}
		}
	}

	public boolean notRecursive(ICraftingPatternDetails details)
	{
		return parent == null || parent.notRecursive( details );
	}

	long getTimes(long remaining, long stackSize)
	{
		if ( limitQty || fullSimulation )
			return 1;
		return (remaining / stackSize) + (remaining % stackSize != 0 ? 1 : 0);
	}

	IAEItemStack getAmountCrafted(IAEItemStack what2)
	{
		for (IAEItemStack is : details.getCondensedOutputs())
		{
			if ( is.equals( what2 ) )
			{
				what2 = what2.copy();
				what2.setStackSize( is.getStackSize() );
				return what2;
			}
		}

		// more fuzzy!
		for (IAEItemStack is : details.getCondensedOutputs())
		{
			if ( is.getItem() == what2.getItem() && (is.getItem().isDamageable() || is.getItemDamage() == what2.getItemDamage()) )
			{
				what2 = is.copy();
				what2.setStackSize( is.getStackSize() );
				return what2;
			}
		}

		throw new RuntimeException( "Crafting Tree construction failed." );
	}

	public void request(MECraftingInventory inv, long i, BaseActionSource src) throws CraftBranchFailure, InterruptedException
	{
		job.handlePausing();

		if ( fullSimulation )
		{
			InventoryCrafting ic = new InventoryCrafting( new ContainerNull(), 3, 3 );

			for (Entry<CraftingTreeNode, Long> entry : nodes.entrySet())
			{
				IAEItemStack item = entry.getKey().getStack( entry.getValue() );
				IAEItemStack stack = entry.getKey().request( inv, item.getStackSize(), src );

				ic.setInventorySlotContents( entry.getKey().slot, stack.getItemStack() );
			}

			FMLCommonHandler.instance().firePlayerCraftingEvent( Platform.getPlayer( (WorldServer) world ), details.getOutput( ic, world ), ic );

			for (int x = 0; x < ic.getSizeInventory(); x++)
			{
				ItemStack is = ic.getStackInSlot( x );
				is = Platform.getContainerItem( is );

				IAEItemStack o = AEApi.instance().storage().createItemStack( is );
				if ( o != null )
				{
					bytes++;
					inv.injectItems( o, Actionable.MODULATE, src );
				}
			}
		}
		else
		{
			// request and remove inputs...
			for (Entry<CraftingTreeNode, Long> entry : nodes.entrySet())
			{
				IAEItemStack item = entry.getKey().getStack( entry.getValue() );
				IAEItemStack stack = entry.getKey().request( inv, item.getStackSize() * i, src );

				if ( containerItems )
				{
					ItemStack is = Platform.getContainerItem( stack.getItemStack() );
					IAEItemStack o = AEApi.instance().storage().createItemStack( is );
					if ( o != null )
					{
						bytes++;
						inv.injectItems( o, Actionable.MODULATE, src );
					}
				}
			}
		}

		// assume its possible.

		// add crafting results..
		for (IAEItemStack out : details.getCondensedOutputs())
		{
			IAEItemStack o = out.copy();
			o.setStackSize( o.getStackSize() * i );
			inv.injectItems( o, Actionable.MODULATE, src );
		}

		crafts += i;
	}

	public void dive(CraftingJob job)
	{
		job.addTask( getAmountCrafted( parent.getStack( 1 ) ), crafts, details, depth );
		for (CraftingTreeNode pro : nodes.keySet())
			pro.dive( job );

		job.addBytes( 8 + crafts + bytes );
	}

	public void setSimulate()
	{
		crafts = 0;
		bytes = 0;

		for (CraftingTreeNode pro : nodes.keySet())
			pro.setSimulate();
	}

	public void setJob(MECraftingInventory storage, CraftingCPUCluster craftingCPUCluster, BaseActionSource src) throws CraftBranchFailure
	{
		craftingCPUCluster.addCrafting( details, crafts );

		for (CraftingTreeNode pro : nodes.keySet())
			pro.setJob( storage, craftingCPUCluster, src );
	}

	public void getPlan(IItemList<IAEItemStack> plan)
	{
		for (IAEItemStack i : details.getOutputs())
		{
			i = i.copy();
			i.setCountRequestable( i.getStackSize() * crafts );
			plan.addRequestable( i );
		}

		for (CraftingTreeNode pro : nodes.keySet())
			pro.getPlan( plan );
	}
}
