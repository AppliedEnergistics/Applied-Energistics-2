package appeng.container.implementations;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.storage.IStorageMonitorable;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.tile.misc.TileSecurity;
import appeng.util.Platform;

public class ContainerSecurity extends ContainerMEMonitorable implements IAEAppEngInventory
{

	SlotRestrictedInput configSlot;

	AppEngInternalInventory wirelessEncoder = new AppEngInternalInventory( this, 2 );

	SlotRestrictedInput wirelessIn;
	SlotOutput wirelessOut;

	TileSecurity securityBox;

	public ContainerSecurity(InventoryPlayer ip, IStorageMonitorable montiorable) {
		super( ip, montiorable, false );

		securityBox = (TileSecurity) montiorable;

		addSlotToContainer( configSlot = new SlotRestrictedInput( PlaceableItemType.BIOMETRIC_CARD, securityBox.configSlot, 0, 37, -33 ) );

		addSlotToContainer( wirelessIn = new SlotRestrictedInput( PlaceableItemType.WIRELESS_TERMINAL, wirelessEncoder, 0, 212, 10 ) );
		addSlotToContainer( wirelessOut = new SlotOutput( wirelessEncoder, 1, 212, 68, -1 ) );

		bindPlayerInventory( ip, 0, 0 );
	}

	public int security = 0;

	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		super.onContainerClosed( player );

		if ( wirelessIn.getHasStack() )
			player.dropPlayerItem( wirelessIn.getStack() );

		if ( wirelessOut.getHasStack() )
			player.dropPlayerItem( wirelessOut.getStack() );
	}

	public void toggleSetting(String value, EntityPlayer player)
	{
		try
		{
			SecurityPermissions permission = SecurityPermissions.valueOf( value );

			ItemStack a = configSlot.getStack();
			if ( a != null && a.getItem() instanceof IBiometricCard )
			{
				IBiometricCard bc = (IBiometricCard) a.getItem();
				if ( bc.hasPermission( a, permission ) )
					bc.removePermission( a, permission );
				else
					bc.addPermission( a, permission );
			}
		}
		catch (EnumConstantNotPresentException ex)
		{
			// :(
		}
	}

	@Override
	public void updateProgressBar(int key, int value)
	{
		if ( key == 0 )
			security = value;
	}

	@Override
	public void detectAndSendChanges()
	{
		verifyPermissions( SecurityPermissions.SECURITY, true );

		int newSecurity = 0;

		ItemStack a = configSlot.getStack();
		if ( a != null && a.getItem() instanceof IBiometricCard )
		{
			IBiometricCard bc = (IBiometricCard) a.getItem();

			for (SecurityPermissions sp : bc.getPermissions( a ))
				newSecurity = newSecurity | (1 << sp.ordinal());
		}

		if ( newSecurity != security )
		{
			if ( Platform.isServer() )
			{
				for (int i = 0; i < this.crafters.size(); ++i)
				{
					ICrafting icrafting = (ICrafting) this.crafters.get( i );

					icrafting.sendProgressBarUpdate( this, 0, newSecurity );
				}
			}

			security = newSecurity;
		}

		super.detectAndSendChanges();
	}

	@Override
	public void saveChanges()
	{
		// :P
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack)
	{
		if ( !wirelessOut.getHasStack() )
		{
			if ( wirelessIn.getHasStack() )
			{
				ItemStack term = wirelessIn.getStack().copy();
				IWirelessTermHandler h = AEApi.instance().registries().wireless().getWirelessTerminalHandler( term );

				if ( h != null )
				{
					h.setEncryptionKey( term, "" + securityBox.securityKey, "" );

					wirelessIn.putStack( null );
					wirelessOut.putStack( term );

					// update the two slots in question...
					for (int i = 0; i < this.crafters.size(); ++i)
					{
						ICrafting icrafting = (ICrafting) this.crafters.get( i );
						((EntityPlayerMP) icrafting).sendSlotContents( this, wirelessIn.slotNumber, wirelessIn.getStack() );
						((EntityPlayerMP) icrafting).sendSlotContents( this, wirelessOut.slotNumber, wirelessOut.getStack() );
					}
				}

			}
		}
	}
}
