package appeng.api.parts.layers;

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

	// cache of inventory state.
	private int sides[][] = null;
	private List<ISidedInventory> invs = null;
	private List<InvSot> slots = null;

	/**
	 * Recalculate inventory wrapper cache.
	 */
	@Override
	public void partChanged()
	{
		invs = new ArrayList();
		int slotCount = 0;

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			IPart bp = getPart( side );
			if ( bp instanceof ISidedInventory )
			{
				ISidedInventory part = (ISidedInventory) bp;
				slotCount += part.getSizeInventory();
				invs.add( part );
			}
		}

		if ( invs.isEmpty() || slotCount == 0 )
		{
			invs = null;
			sides = null;
			slots = null;
		}
		else
		{
			sides = new int[][] { nullSides, nullSides, nullSides, nullSides, nullSides, nullSides };
			slots = new ArrayList<InvSot>( Collections.nCopies( slotCount, (InvSot) null ) );

			int offsetForLayer = 0;
			int offsetForPart = 0;
			for (ISidedInventory sides : invs)
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

				int cSidesList[] = this.sides[currentSide.ordinal()] = new int[slotCount];
				for (int cSlot = 0; cSlot < slotCount; cSlot++)
				{
					cSidesList[cSlot] = offsetForLayer;
					slots.set( offsetForLayer++, new InvSot( sides, offsetForPart++ ) );
				}
			}
		}

		// make sure inventory is updated before we call FMP.
		super.partChanged();
	}

	/**
	 * check if a slot index is valid, prevent crashes from bad code :)
	 * 
	 * @param slot
	 * @return true, if the slot exists.
	 */
	boolean isSlotValid(int slot)
	{
		return slots != null && slot >= 0 && slot < slots.size();
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		if ( isSlotValid( slot ) )
			return slots.get( slot ).decrStackSize( amount );

		return null;
	}

	@Override
	public int getSizeInventory()
	{
		if ( slots == null )
			return 0;

		return slots.size();
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		if ( isSlotValid( slot ) )
			return slots.get( slot ).getStackInSlot();

		return null;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack)
	{
		if ( isSlotValid( slot ) )
			return slots.get( slot ).isItemValidForSlot( itemstack );

		return false;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack)
	{
		if ( isSlotValid( slot ) )
			slots.get( slot ).setInventorySlotContents( itemstack );
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemstack, int side)
	{
		if ( isSlotValid( slot ) )
			return slots.get( slot ).canExtractItem( itemstack, side );

		return false;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack itemstack, int side)
	{
		if ( isSlotValid( slot ) )
			return slots.get( slot ).canInsertItem( itemstack, side );

		return false;
	}

	@Override
	public void markDirty()
	{
		super.markForSave();

		if ( invs != null )
		{
			for (IInventory inv : invs)
				inv.markDirty();
		}
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if ( sides == null || side < 0 || side > 5 )
			return nullSides;
		return sides[side];
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
