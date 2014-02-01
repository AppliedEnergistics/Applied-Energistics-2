package appeng.container.implementations;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.storage.IStorageMonitorable;
import appeng.container.slot.SlotNormal;
import appeng.tile.misc.TileSecurity;
import appeng.util.Platform;

public class ContainerSecurity extends ContainerMEMonitorable
{

	SlotNormal configSlot;

	public ContainerSecurity(InventoryPlayer ip, IStorageMonitorable montiorable) {
		super( ip, montiorable, false );

		addSlotToContainer( configSlot = new SlotNormal( ((TileSecurity) montiorable).configSlot, 0, 37, -32 ) );

		bindPlayerInventory( ip, 0, 0 );
	}

	public int security = 0;

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

}
