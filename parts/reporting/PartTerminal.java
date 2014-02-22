package appeng.parts.reporting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.util.IConfigManager;
import appeng.client.texture.CableBusTextures;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.GuiBridge;
import appeng.me.GridAccessException;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;

public class PartTerminal extends PartMonitor implements ITerminalHost, IConfigManagerHost
{

	IConfigManager cm = new ConfigManager( this );

	public PartTerminal(Class clz, ItemStack is) {
		super( clz, is );

		cm.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		cm.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
		cm.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT( data );
		cm.readFromNBT( data );
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT( data );
		cm.writeToNBT( data );
	}

	public PartTerminal(ItemStack is) {
		super( PartTerminal.class, is );
		frontBright = CableBusTextures.PartTerminal_Bright;
		frontColored = CableBusTextures.PartTerminal_Colored;
		frontDark = CableBusTextures.PartTerminal_Dark;
		// frontSolid = CableBusTextures.PartTerminal_Solid;

		cm.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		cm.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
		cm.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );
	}

	public GuiBridge getGui()
	{
		return GuiBridge.GUI_ME;
	}

	@Override
	public boolean onPartActivate(EntityPlayer player, Vec3 pos)
	{
		if ( !player.isSneaking() )
		{
			if ( Platform.isClient() )
				return true;

			if ( proxy.isActive() )
				Platform.openGUI( player, getHost().getTile(), side, getGui() );
			else
			{
				if ( proxy.isPowered() )
					player.addChatMessage( PlayerMessages.CommunicationError.get() );
				else
					player.addChatMessage( PlayerMessages.MachineNotPowered.get() );
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

	@Override
	public IConfigManager getConfigManager()
	{
		return cm;
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
	{

	}

}
