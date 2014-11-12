package appeng.container.implementations;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPart;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.helpers.IPriorityHost;
import appeng.tile.misc.TileSecurity;
import appeng.tile.misc.TileTeleporter;
import appeng.util.Platform;

public class ContainerTeleporter extends AEBaseContainer
{
	
	final TileTeleporter tt;

	@SideOnly(Side.CLIENT)
	public GuiTextField textField;

	@SideOnly(Side.CLIENT)
	public void setTextField(GuiTextField level)
	{
		textField = level;
		textField.setText( "" + frequencyValue );
	}

	public ContainerTeleporter(InventoryPlayer ip, TileTeleporter tile) {
		super( ip, tile );
		tt = tile;
	}

	@GuiSync(2)
	public int frequencyValue = 1;

	public void setFrequency(int newValue, EntityPlayer player)
	{
		tt.setFrequency( newValue );
		frequencyValue = newValue;
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		verifyPermissions( SecurityPermissions.BUILD, false );

		if ( Platform.isServer() )
		{
			this.frequencyValue = tt.getFrequency();
		}
	}

	@Override
	public void onUpdate(String field, Object oldValue, Object newValue)
	{
		if ( field.equals( "frequencyValue" ) )
		{
			if ( textField != null )
				textField.setText( "" + frequencyValue );
		}

		super.onUpdate( field, oldValue, newValue );
	}

}
