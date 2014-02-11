package appeng.container.slot;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.helpers.InventoryAction;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorPlayerHand;
import appeng.util.item.AEItemStack;

public class SlotCraftingTerm extends AppEngCraftingSlot
{

	private final IInventory craftMatrix;

	private final BaseActionSource mySrc;
	private final IEnergySource energySrc;
	private final IStorageMonitorable storage;

	public SlotCraftingTerm(EntityPlayer player, BaseActionSource mySrc, IEnergySource energySrc, IStorageMonitorable storage, IInventory cMatrix,
			IInventory output, int x, int y) {
		super( player, cMatrix, output, 0, x, y );
		this.energySrc = energySrc;
		this.storage = storage;
		this.mySrc = mySrc;
		craftMatrix = cMatrix;
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

	private ItemStack extractItemsByRecipe(IMEMonitor<IAEItemStack> src, World w, IRecipe r, ItemStack output, InventoryCrafting ci,
			ItemStack providedTemplate, int slot, IItemList<IAEItemStack> aitems)
	{
		if ( energySrc.extractAEPower( 1, Actionable.SIMULATE, PowerMultiplier.CONFIG ) > 0.9 )
		{
			if ( providedTemplate == null )
				return null;

			AEItemStack ae_req = AEItemStack.create( providedTemplate );
			ae_req.setStackSize( 1 );

			IAEItemStack ae_ext = src.extractItems( ae_req, Actionable.MODULATE, mySrc );
			if ( ae_ext != null )
			{
				ItemStack extracted = ae_ext.getItemStack();
				if ( extracted != null )
				{
					energySrc.extractAEPower( 1, Actionable.MODULATE, PowerMultiplier.CONFIG );
					return extracted;
				}
			}

			if ( aitems != null && (ae_req.isOre() || providedTemplate.hasTagCompound() || providedTemplate.isItemStackDamageable()) )
			{
				for (IAEItemStack x : aitems)
				{
					ItemStack sh = x.getItemStack();
					if ( (Platform.isSameItemType( providedTemplate, sh ) || ae_req.sameOre( x )) && !Platform.isSameItem( sh, output ) )
					{ // Platform.isSameItemType( sh, providedTemplate )
						ItemStack cp = Platform.cloneItemStack( sh );
						cp.stackSize = 1;
						ci.setInventorySlotContents( slot, cp );
						if ( r.matches( ci, w ) && Platform.isSameItem( r.getCraftingResult( ci ), output ) )
						{
							IAEItemStack ex = src.extractItems( AEItemStack.create( cp ), Actionable.MODULATE, mySrc );
							if ( ex != null )
							{
								energySrc.extractAEPower( 1, Actionable.MODULATE, PowerMultiplier.CONFIG );
								return ex.getItemStack();
							}
						}
						ci.setInventorySlotContents( slot, providedTemplate );
					}
				}
			}

		}
		return null;
	}

	ItemStack craftItem(EntityPlayer p, ItemStack request, IMEMonitor<IAEItemStack> inv, IItemList all)
	{
		// update crafting matrx...
		ItemStack is = getStack();

		if ( is != null && Platform.isSameItem( request, is ) )
		{
			ItemStack[] set = new ItemStack[craftMatrix.getSizeInventory()];

			// add one of each item to the items on the board...
			List<ItemStack> drops = new ArrayList();
			if ( Platform.isServer() )
			{
				InventoryCrafting ic = new InventoryCrafting( new ContainerNull(), 3, 3 );
				for (int x = 0; x < 9; x++)
					ic.setInventorySlotContents( x, craftMatrix.getStackInSlot( x ) );

				IRecipe r = Platform.findMatchingRecipe( ic, p.worldObj );

				if ( r == null )
					return null;

				is = r.getCraftingResult( ic );

				if ( r != null && inv != null )
				{
					for (int x = 0; x < craftMatrix.getSizeInventory(); x++)
					{
						if ( craftMatrix.getStackInSlot( x ) != null )
						{
							set[x] = extractItemsByRecipe( inv, p.worldObj, r, is, ic, craftMatrix.getStackInSlot( x ), x, all );
							ic.setInventorySlotContents( x, set[x] );
						}
					}
				}
			}

			super.onPickupFromSlot( p, is );

			// add one of each item to the items on the board...
			if ( Platform.isServer() )
			{
				// set new items onto the crafting table...
				for (int x = 0; x < craftMatrix.getSizeInventory(); x++)
				{
					if ( craftMatrix.getStackInSlot( x ) == null )
						craftMatrix.setInventorySlotContents( x, set[x] );
					else if ( set[x] != null )
					{
						// eek! put it back!
						IAEItemStack fail = inv.injectItems( AEItemStack.create( set[x] ), Actionable.MODULATE, mySrc );
						if ( fail != null )
							drops.add( fail.getItemStack() );
					}
				}
			}

			// shouldn't be nessiary...
			p.openContainer.onCraftMatrixChanged( craftMatrix );

			if ( drops.size() > 0 )
				Platform.spawnDrops( p.worldObj, (int) p.posX, (int) p.posY, (int) p.posZ, drops );

			return is;
		}

		return null;
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

}
