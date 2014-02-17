package appeng.container.implementations;

import java.io.IOException;
import java.nio.BufferOverflowException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigureableObject;
import appeng.container.AEBaseContainer;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketMEInventoryUpdate;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import appeng.util.item.ItemList;

public class ContainerMEMonitorable extends AEBaseContainer implements IConfigManagerHost, IConfigureableObject, IMEMonitorHandlerReceiver<IAEItemStack>
{

	final IMEMonitor<IAEItemStack> monitor;
	final IItemList<IAEItemStack> items = new ItemList<IAEItemStack>();

	IConfigManager serverCM;
	IConfigManager clientCM;

	public IConfigManagerHost gui;

	protected ContainerMEMonitorable(InventoryPlayer ip, ITerminalHost montiorable, boolean bindInventory) {
		super( ip, montiorable instanceof TileEntity ? (TileEntity) montiorable : null, montiorable instanceof IPart ? (IPart) montiorable : null );

		clientCM = new ConfigManager( this );

		clientCM.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		clientCM.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
		clientCM.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );

		if ( Platform.isServer() )
		{
			serverCM = montiorable.getConfigManager();

			monitor = montiorable.getItemInventory();
			if ( monitor != null )
			{
				monitor.addListener( this, null );

				cellInv = monitor;

				if ( montiorable instanceof IPortableCell )
					powerSrc = (IPortableCell) montiorable;
				else if ( montiorable instanceof IMEChest )
					powerSrc = (IMEChest) montiorable;
				else if ( montiorable instanceof IGridHost )
				{
					IGridNode node = ((IGridHost) montiorable).getGridNode( ForgeDirection.UNKNOWN );
					if ( node != null )
					{
						IGrid g = node.getGrid();
						if ( g != null )
							powerSrc = g.getCache( IEnergyGrid.class );
					}
				}
			}
			else
				isContainerValid = false;
		}
		else
			monitor = null;

		if ( bindInventory )
			bindPlayerInventory( ip, 0, 0 );
	}

	public ContainerMEMonitorable(InventoryPlayer ip, ITerminalHost montiorable) {
		this( ip, montiorable, true );
	}

	@Override
	public void detectAndSendChanges()
	{
		if ( Platform.isServer() )
		{
			for (Enum set : serverCM.getSettings())
			{
				Enum sideLocal = serverCM.getSetting( set );
				Enum sideRemote = clientCM.getSetting( set );

				if ( sideLocal != sideRemote )
				{
					clientCM.putSetting( set, sideLocal );
					for (int j = 0; j < this.crafters.size(); ++j)
					{
						try
						{
							NetworkHandler.instance.sendTo( new PacketValueConfig( set.name(), sideLocal.name() ), (EntityPlayerMP) this.crafters.get( j ) );
						}
						catch (IOException e)
						{
							AELog.error( e );
						}
					}
				}
			}

			if ( !items.isEmpty() )
			{
				try
				{
					IItemList<IAEItemStack> monitorCache = monitor.getStorageList();

					PacketMEInventoryUpdate piu = new PacketMEInventoryUpdate();

					for (IAEItemStack is : items)
					{
						IAEItemStack send = monitorCache.findPrecise( is );
						if ( send == null )
						{
							is.setStackSize( 0 );
							piu.appendItem( is );
						}
						else
							piu.appendItem( send );
					}

					if ( !piu.isEmpty() )
					{
						items.resetStatus();

						for (Object c : this.crafters)
						{
							if ( c instanceof EntityPlayer )
								NetworkHandler.instance.sendTo( piu, (EntityPlayerMP) c );
						}
					}
				}
				catch (IOException e)
				{
					AELog.error( e );
				}
			}

			super.detectAndSendChanges();
		}
	}

	@Override
	public void addCraftingToCrafters(ICrafting c)
	{
		super.addCraftingToCrafters( c );

		if ( Platform.isServer() && c instanceof EntityPlayer && monitor != null )
		{
			try
			{
				PacketMEInventoryUpdate piu = new PacketMEInventoryUpdate();
				IItemList<IAEItemStack> monitorCache = monitor.getStorageList();

				for (IAEItemStack send : monitorCache)
				{
					try
					{
						piu.appendItem( send );
					}
					catch (BufferOverflowException boe)
					{
						NetworkHandler.instance.sendTo( piu, (EntityPlayerMP) c );

						piu = new PacketMEInventoryUpdate();
						piu.appendItem( send );
					}
				}

				NetworkHandler.instance.sendTo( piu, (EntityPlayerMP) c );
			}
			catch (IOException e)
			{
				AELog.error( e );
			}

		}
	}

	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		super.onContainerClosed( player );
		if ( monitor != null )
			monitor.removeListener( this );
	}

	@Override
	public void removeCraftingFromCrafters(ICrafting c)
	{
		super.removeCraftingFromCrafters( c );

		if ( this.crafters.isEmpty() && monitor != null )
			monitor.removeListener( this );
	}

	@Override
	public void postChange(IMEMonitor<IAEItemStack> monitor, IAEItemStack change, BaseActionSource source)
	{
		items.add( change );
	}

	@Override
	public boolean isValid(Object verificationToken)
	{
		return true;
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
	{
		if ( gui != null )
			gui.updateSetting( manager, settingName, newValue );
	}

	@Override
	public IConfigManager getConfigManager()
	{
		if ( Platform.isServer() )
			return serverCM;
		return clientCM;
	}

}
