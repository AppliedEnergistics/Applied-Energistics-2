package appeng.container.implementations;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPart;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.helpers.IPriorityHost;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerPriority extends AEBaseContainer
{

	IPriorityHost priHost;

	@SideOnly(Side.CLIENT)
	public GuiTextField textField;

	@SideOnly(Side.CLIENT)
	public void setTextField(GuiTextField level)
	{
		textField = level;
		textField.setText( "" + PriorityValue );
	}

	public ContainerPriority(InventoryPlayer ip, IPriorityHost te) {
		super( ip, (TileEntity) (te instanceof TileEntity ? te : null), (IPart) (te instanceof IPart ? te : null) );
		priHost = te;
	}

	@GuiSync(2)
	public long PriorityValue = -1;

	public void setPriority(int newValue, EntityPlayer player)
	{
		priHost.setPriority( newValue );
		PriorityValue = newValue;
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		verifyPermissions( SecurityPermissions.BUILD, false );

		if ( Platform.isServer() )
		{
			this.PriorityValue = priHost.getPriority();
		}
	}

	@Override
	public void onUpdate(String field, Object oldValue, Object newValue)
	{
		if ( field.equals( "PriorityValue" ) )
		{
			if ( textField != null )
				textField.setText( "" + PriorityValue );
		}

		super.onUpdate( field, oldValue, newValue );
	}
}
