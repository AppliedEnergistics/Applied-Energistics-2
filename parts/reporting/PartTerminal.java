package appeng.parts.reporting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.client.texture.CableBusTextures;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.GuiBridge;
import appeng.me.GridAccessException;
import appeng.util.Platform;

public class PartTerminal extends PartMonitor implements IStorageMonitorable
{

	public PartTerminal(ItemStack is) {
		super( PartTerminal.class, is );
		frontBright = CableBusTextures.PartTerminal_Bright;
		frontColored = CableBusTextures.PartTerminal_Colored;
		frontDark = CableBusTextures.PartTerminal_Dark;
		frontSolid = CableBusTextures.PartTerminal_Solid;
	}

	@Override
	public boolean onActivate(EntityPlayer player, Vec3 pos)
	{
		if ( !player.isSneaking() )
		{
			if ( Platform.isClient() )
				return true;

			if ( proxy.isActive() )
				Platform.openGUI( player, getHost().getTile(), side, GuiBridge.GUI_ME );
			else
			{
				if ( proxy.isPowered() )
					player.sendChatToPlayer( PlayerMessages.CommunicationError.get() );
				else
					player.sendChatToPlayer( PlayerMessages.MachineNotPowered.get() );
			}

			return true;
		}

		return false;
	}

	@Override
	public IMEMonitor getFluidInventory()
	{
		try
		{
			return proxy.getStorage().getFluidInventory();
		}
		catch (GridAccessException e)
		{
			// err nope?
		}
		return null;
	}

	@Override
	public IMEMonitor getItemInventory()
	{
		try
		{
			return proxy.getStorage().getItemInventory();
		}
		catch (GridAccessException e)
		{
			// err nope?
		}
		return null;
	}

}
