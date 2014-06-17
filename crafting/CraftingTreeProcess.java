package appeng.crafting;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.cache.CraftingCache;

public class CraftingTreeProcess
{

	CraftingTreeNode parent;
	ICraftingPatternDetails details;
	CraftingJob job;

	long crafts = 0;
	boolean damageable;

	final private int depth;

	Map<CraftingTreeNode, Long> nodes = new HashMap();
	public boolean possible = true;

	public CraftingTreeProcess(CraftingCache cc, CraftingJob job, ICraftingPatternDetails details, CraftingTreeNode craftingTreeNode, int depth, World world) {
		parent = craftingTreeNode;
		this.details = details;
		this.job = job;
		this.depth = depth;

		if ( details.isCraftable() )
		{
			IAEItemStack list[] = details.getInputs();

			for (int x = 0; x < list.length; x++)
			{
				IAEItemStack part = list[x];
				if ( part != null )
				{
					ItemStack is = part.getItemStack();
					if ( is.getItem().hasContainerItem( is ) )
						damageable = true;
					nodes.put( new CraftingTreeNode( cc, job, part.copy(), this, x, depth + 1 ), part.getStackSize() );
				}
			}
		}
		else
		{
			for (IAEItemStack part : details.getCondencedInputs())
			{
				nodes.put( new CraftingTreeNode( cc, job, part.copy(), this, -1, depth + 1 ), part.getStackSize() );
			}
		}
	}

	public boolean notRecurive(ICraftingPatternDetails details)
	{
		return parent.notRecurive( details );
	}

	long getTimes(long remaining, long stackSize)
	{
		if ( damageable )
			return 1;
		return (remaining / stackSize) + (remaining % stackSize != 0 ? 1 : 0);
	}

	IAEItemStack getAmountCrafted(IAEItemStack what2)
	{
		for (IAEItemStack is : details.getCondencedOutputs())
		{
			if ( is.equals( what2 ) )
			{
				what2 = what2.copy();
				what2.setStackSize( is.getStackSize() );
				return what2;
			}
		}

		throw new RuntimeException( "Crafting Tree construction failed." );
	}

	public void request(MECraftingInventory inv, long i, BaseActionSource src) throws CraftBranchFailure
	{
		// request and remove inputs...
		for (Entry<CraftingTreeNode, Long> entry : nodes.entrySet())
		{
			IAEItemStack item = entry.getKey().getStack( entry.getValue() );
			IAEItemStack stack = entry.getKey().request( inv, item.getStackSize() * i, src );

			if ( damageable )
			{
				ItemStack is = stack.getItemStack();
				if ( stack.getItem().hasContainerItem( is ) )
				{
					is = stack.getItem().getContainerItem( is );
					if ( is.isItemStackDamageable() && is.getItemDamage() == is.getMaxDamage() )
						is = null;

					IAEItemStack o = AEApi.instance().storage().createItemStack( is );
					if ( o != null )
						inv.injectItems( o, Actionable.MODULATE, src );
				}
			}
		}

		// assume its possible.

		// add crafting results..
		for (IAEItemStack out : details.getCondencedOutputs())
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
	}
}
