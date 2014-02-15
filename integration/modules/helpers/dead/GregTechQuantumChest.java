package appeng.integration.modules.helpers.dead;

import gregtechmod.api.interfaces.IDigitalChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.me.storage.MEIInventoryWrapper;
import appeng.util.InventoryAdaptor;
import appeng.util.item.AEItemStack;

public class GregTechQuantumChest extends MEIInventoryWrapper
{

	IDigitalChest qc;

	public GregTechQuantumChest(IInventory m, InventoryAdaptor ia) {
		super( m, ia );
		qc = (IDigitalChest) m;
	}

	private ItemStack getType()
	{
		ItemStack[] array = qc.getStoredItemData();
		if ( array.length > 0 && array[0].itemID > 0 )
			return array[0];
		return null;
	}

	@Override
	public IAEItemStack injectItems(IAEItemStack input, Actionable mode, BaseActionSource src)
	{
		ItemStack type = getType();
		if ( input.hasTagCompound() )
			return input;

		if ( type == null )
		{
			return input;
		}

		if ( (type.getItem() == input.getItem() && type.getItemDamage() == input.getItemDamage()) )
		{
			if ( type.stackSize < qc.getMaxItemCount() )
			{
				int room = (int) ((long) qc.getMaxItemCount() - (long) type.stackSize);
				if ( input.getStackSize() > room )
				{
					IAEItemStack is = input.copy();
					is.setStackSize( is.getStackSize() - room );
					if ( mode == Actionable.MODULATE )
						qc.setItemCount( type.stackSize + room );

					return super.injectItems( is, mode, src );
				}

				if ( mode == Actionable.MODULATE )
					qc.setItemCount( type.stackSize + (int) input.getStackSize() );

				return null;
			}

			return super.injectItems( input, mode, src );
		}

		return input;
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack i, Actionable mode, BaseActionSource src)
	{
		ItemStack type = getType();
		if ( type != null )
		{
			if ( type.getItem() == i.getItem() && type.getItemDamage() == i.getItemDamage() )
			{
				if ( type.stackSize > i.getStackSize() )
				{
					IAEItemStack output = AEItemStack.create( type );
					output.setStackSize( (int) i.getStackSize() );
					if ( mode == Actionable.MODULATE )
						qc.setItemCount( type.stackSize - (int) output.getStackSize() );
					return output;
				}
			}
		}
		return super.extractItems( i, mode, src );
	}

	@Override
	public IItemList getAvailableItems(IItemList out)
	{
		ItemStack type = getType();
		if ( type != null )
		{
			super.getAvailableItems( out );
			if ( type != null && type.stackSize > 0 )
				out.addStorage( AEItemStack.create( type ) );
		}
		return out;
	}
}