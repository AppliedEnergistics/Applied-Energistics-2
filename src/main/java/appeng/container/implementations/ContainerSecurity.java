package appeng.container.implementations;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.features.INetworkEncodable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.storage.ITerminalHost;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.tile.misc.TileSecurity;

public class ContainerSecurity extends ContainerMEMonitorable implements IAEAppEngInventory
{

	SlotRestrictedInput configSlot;

	AppEngInternalInventory wirelessEncoder = new AppEngInternalInventory( this, 2 );

	SlotRestrictedInput wirelessIn;
	SlotOutput wirelessOut;

	TileSecurity securityBox;

	public ContainerSecurity(InventoryPlayer ip, ITerminalHost monitorable) {
		super( ip, monitorable, false );

		securityBox = (TileSecurity) monitorable;

		addSlotToContainer( configSlot = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.BIOMETRIC_CARD, securityBox.configSlot, 0, 37, -33, ip ) );

		addSlotToContainer( wirelessIn = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.ENCODABLE_ITEM, wirelessEncoder, 0, 212, 10, ip ) );
		addSlotToContainer( wirelessOut = new SlotOutput( wirelessEncoder, 1, 212, 68, -1 ) );

		bindPlayerInventory( ip, 0, 0 );
	}

	@GuiSync(0)
	public int security = 0;

	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		super.onContainerClosed( player );

		if ( wirelessIn.getHasStack() )
			player.dropPlayerItemWithRandomChoice( wirelessIn.getStack(), false );

		if ( wirelessOut.getHasStack() )
			player.dropPlayerItemWithRandomChoice( wirelessOut.getStack(), false );
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
	public void detectAndSendChanges()
	{
		verifyPermissions( SecurityPermissions.SECURITY, false );

		security = 0;

		ItemStack a = configSlot.getStack();
		if ( a != null && a.getItem() instanceof IBiometricCard )
		{
			IBiometricCard bc = (IBiometricCard) a.getItem();

			for (SecurityPermissions sp : bc.getPermissions( a ))
				security = security | (1 << sp.ordinal());
		}

		updatePowerStatus();

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
				INetworkEncodable networkEncodable = null;

				if ( term.getItem() instanceof INetworkEncodable )
					networkEncodable = (INetworkEncodable) term.getItem();

				IWirelessTermHandler wTermHandler = AEApi.instance().registries().wireless().getWirelessTerminalHandler( term );
				if ( wTermHandler != null )
					networkEncodable = wTermHandler;

				if ( networkEncodable != null )
				{
					networkEncodable.setEncryptionKey( term, "" + securityBox.securityKey, "" );

					wirelessIn.putStack( null );
					wirelessOut.putStack( term );

					// update the two slots in question...
					for (Object crafter : this.crafters)
					{
						ICrafting icrafting = (ICrafting) crafter;
						((EntityPlayerMP) icrafting).sendSlotContents( this, wirelessIn.slotNumber, wirelessIn.getStack() );
						((EntityPlayerMP) icrafting).sendSlotContents( this, wirelessOut.slotNumber, wirelessOut.getStack() );
					}
				}

			}
		}
	}
}
