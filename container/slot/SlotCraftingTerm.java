package appeng.container.slot;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.items.storage.ItemViewCell;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorPlayerHand;
import appeng.util.item.AEItemStack;

public class SlotCraftingTerm extends AppEngCraftingSlot
{

	protected final IInventory craftInv;
	protected final IInventory pattern;

	private final BaseActionSource mySrc;
	private final IEnergySource energySrc;
	private final IStorageMonitorable storage;
	private final IContainerCraftingPacket container;

	public SlotCraftingTerm(EntityPlayer player, BaseActionSource mySrc, IEnergySource energySrc, IStorageMonitorable storage, IInventory cMatrix,
			IInventory secondMatrix, IInventory output, int x, int y, IContainerCraftingPacket ccp)
	{
		super( player, cMatrix, output, 0, x, y );
		this.energySrc = energySrc;
		this.storage = storage;
		this.mySrc = mySrc;
		pattern = cMatrix;
		craftInv = secondMatrix;
		container = ccp;
	}

	public IInventory getCraftingMatrix()
	{
		return craftInv;
	}

	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer)
	{
		return false;
	}

	@Override
	public void onPickupFromSlot(EntityPlayer p, ItemStack is)
	{
	}

	public void makeItem(EntityPlayer p, ItemStack is)
	{
		super.onPickupFromSlot( p, is );
	}

	public ItemStack craftItem(EntityPlayer p, ItemStack request, IMEMonitor<IAEItemStack> inv, IItemList all)
	{
		// update crafting matrx...
		ItemStack is = getStack();

		if ( is != null && Platform.isSameItem( request, is ) )
		{
			ItemStack[] set = new ItemStack[pattern.getSizeInventory()];

			// add one of each item to the items on the board...
			if ( Platform.isServer() )
			{
				InventoryCrafting ic = new InventoryCrafting( new ContainerNull(), 3, 3 );
				for (int x = 0; x < 9; x++)
					ic.setInventorySlotContents( x, pattern.getStackInSlot( x ) );

				IRecipe r = Platform.findMatchingRecipe( ic, p.worldObj );

				if ( r == null )
				{
					Item target = request.getItem();
					if ( target.isDamageable() && target.isRepairable() )
					{
						boolean isBad = false;
						for (int x = 0; x < ic.getSizeInventory(); x++)
						{
							ItemStack pis = ic.getStackInSlot( x );
							if ( pis == null )
								continue;
							if ( pis.getItem() != target )
								isBad = true;
						}
						if ( !isBad )
						{
							super.onPickupFromSlot( p, is );
							return request;
						}
					}
					return null;
				}

				is = r.getCraftingResult( ic );

				if ( r != null && inv != null )
				{
					for (int x = 0; x < pattern.getSizeInventory(); x++)
					{
						if ( pattern.getStackInSlot( x ) != null )
						{
							set[x] = Platform.extractItemsByRecipe( energySrc, mySrc, inv, p.worldObj, r, is, ic, pattern.getStackInSlot( x ), x, all,
									Actionable.MODULATE, ItemViewCell.createFilter( container.getViewCells() ) );
							ic.setInventorySlotContents( x, set[x] );
						}
					}
				}
			}

			if ( preCraft( p, inv, set, is ) )
			{
				makeItem( p, is );

				postCraft( p, inv, set, is );
			}

			// shouldn't be necessary...
			p.openContainer.onCraftMatrixChanged( getCraftingMatrix() );

			return is;
		}

		return null;
	}

	public boolean preCraft(EntityPlayer p, IMEMonitor<IAEItemStack> inv, ItemStack[] set, ItemStack result)
	{
		return true;
	}

	public void postCraft(EntityPlayer p, IMEMonitor<IAEItemStack> inv, ItemStack set[], ItemStack result)
	{
		List<ItemStack> drops = new ArrayList();

		// add one of each item to the items on the board...
		if ( Platform.isServer() )
		{
			// set new items onto the crafting table...
			for (int x = 0; x < getCraftingMatrix().getSizeInventory(); x++)
			{
				if ( getCraftingMatrix().getStackInSlot( x ) == null )
					getCraftingMatrix().setInventorySlotContents( x, set[x] );
				else if ( set[x] != null )
				{
					// eek! put it back!
					IAEItemStack fail = inv.injectItems( AEItemStack.create( set[x] ), Actionable.MODULATE, mySrc );
					if ( fail != null )
						drops.add( fail.getItemStack() );
				}
			}
		}

		if ( drops.size() > 0 )
			Platform.spawnDrops( p.worldObj, (int) p.posX, (int) p.posY, (int) p.posZ, drops );
	}

	public void doClick(InventoryAction action, EntityPlayer who)
	{
		if ( getStack() == null )
			return;
		if ( Platform.isClient() )
			return;

		IMEMonitor<IAEItemStack> inv = storage.getItemInventory();
		int howManyPerCraft = getStack().stackSize;
		int maxTimesToCraft = 0;

		InventoryAdaptor ia = null;
		if ( action == InventoryAction.CRAFT_SHIFT ) // craft into player inventory...
		{
			ia = InventoryAdaptor.getAdaptor( who, null );
			maxTimesToCraft = (int) Math.floor( (double) getStack().getMaxStackSize() / (double) howManyPerCraft );
		}
		else if ( action == InventoryAction.CRAFT_STACK ) // craft into hand, full stack
		{
			ia = new AdaptorPlayerHand( who );
			maxTimesToCraft = (int) Math.floor( (double) getStack().getMaxStackSize() / (double) howManyPerCraft );
		}
		else
		// pick up what was crafted...
		{
			ia = new AdaptorPlayerHand( who );
			maxTimesToCraft = 1;
		}

		maxTimesToCraft = CapCraftingAttempts( maxTimesToCraft );

		if ( ia == null )
			return;

		ItemStack rs = Platform.cloneItemStack( getStack() );
		if ( rs == null )
			return;

		for (int x = 0; x < maxTimesToCraft; x++)
		{
			if ( ia.simulateAdd( rs ) == null )
			{
				IItemList<IAEItemStack> all = inv.getStorageList();
				ItemStack extra = ia.addItems( craftItem( who, rs, inv, all ) );
				if ( extra != null )
				{
					List<ItemStack> drops = new ArrayList();
					drops.add( extra );
					Platform.spawnDrops( who.worldObj, (int) who.posX, (int) who.posY, (int) who.posZ, drops );
					return;
				}
			}
		}
	}

	protected int CapCraftingAttempts(int maxTimesToCraft)
	{
		return maxTimesToCraft;
	}

}
