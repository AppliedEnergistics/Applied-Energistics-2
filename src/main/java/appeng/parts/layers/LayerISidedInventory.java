package appeng.parts.layers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.LayerBase;

/**
 * Inventory wrapper for parts,
 * 
 * this is considerably more complicated then the other wrappers as it requires creating a "unified inventory".
 * 
 * You must use {@link ISidedInventory} instead of {@link IInventory}.
 * 
 * If your inventory changes in between placement and removal, you must trigger a PartChange on the {@link IPartHost} so
 * it can recalculate the inventory wrapper.
 */
public class LayerISidedInventory extends LayerBase implements ISidedInventory
{

	// a simple empty array for empty stuff..
	private final static int[] nullSides = new int[] {};

	InvLayerData invLayer = null;

	/**
	 * Recalculate inventory wrapper cache.
	 */
	@Override
	public void notifyNeighbors()
	{
		// cache of inventory state.
		int sideData[][] = null;
		List<ISidedInventory> inventories = null;
		List<InvSot> slots = null;

		inventories = new ArrayList<ISidedInventory>();
		int slotCount = 0;

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			IPart bp = getPart( side );
			if ( bp instanceof ISidedInventory )
			{
				ISidedInventory part = (ISidedInventory) bp;
				slotCount += part.getSizeInventory();
				inventories.add( part );
			}
		}

		if ( inventories.isEmpty() || slotCount == 0 )
		{
			inventories = null;
		}
		else
		{
			sideData = new int[][] { nullSides, nullSides, nullSides, nullSides, nullSides, nullSides };
			slots = new ArrayList<InvSot>( Collections.nCopies( slotCount, (InvSot) null ) );

			int offsetForLayer = 0;
			int offsetForPart = 0;
			for (ISidedInventory sides : inventories)
			{
				offsetForPart = 0;
				slotCount = sides.getSizeInventory();

				ForgeDirection currentSide = ForgeDirection.UNKNOWN;
				for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
					if ( getPart( side ) == sides )
					{
						currentSide = side;
						break;
					}

				int cSidesList[] = sideData[currentSide.ordinal()] = new int[slotCount];
				for (int cSlot = 0; cSlot < slotCount; cSlot++)
				{
					cSidesList[cSlot] = offsetForLayer;
					slots.set( offsetForLayer++, new InvSot( sides, offsetForPart++ ) );
				}
			}
		}

		if ( sideData == null || slots == null )
			invLayer = null;
		else
			invLayer = new InvLayerData( sideData, inventories, slots );

		// make sure inventory is updated before we call FMP.
		super.notifyNeighbors();
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		if ( invLayer == null )
			return null;

		return invLayer.decreaseStackSize( slot, amount );
	}

	@Override
	public int getSizeInventory()
	{
		if ( invLayer == null )
			return 0;

		return invLayer.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		if ( invLayer == null )
			return null;

		return invLayer.getStackInSlot( slot );
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack)
	{
		if ( invLayer == null )
			return false;

		return invLayer.isItemValidForSlot( slot, itemstack );
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack)
	{
		if ( invLayer == null )
			return;

		invLayer.setInventorySlotContents( slot, itemstack );
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemstack, int side)
	{
		if ( invLayer == null )
			return false;

		return invLayer.canExtractItem( slot, itemstack, side );
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack itemstack, int side)
	{
		if ( invLayer == null )
			return false;

		return invLayer.canInsertItem( slot, itemstack, side );
	}

	@Override
	public void markDirty()
	{
		if ( invLayer != null )
			invLayer.markDirty();

		super.markForSave();
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if ( invLayer != null )
			return invLayer.getAccessibleSlotsFromSide( side );

		return nullSides;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64; // no options here.
	}

	@Override
	public String getInventoryName()
	{
		return "AEMultiPart";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		return null;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return false;
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public void openInventory()
	{
	}
}
