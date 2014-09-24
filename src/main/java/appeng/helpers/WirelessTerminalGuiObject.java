package appeng.helpers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.tile.networking.TileWireless;

public class WirelessTerminalGuiObject implements IPortableCell, IActionHost
{

	IWirelessTermHandler wth;
	String encryptionKey;

	IGrid targetGrid;
	IStorageGrid sg;
	IMEMonitor<IAEItemStack> itemStorage;
	IWirelessAccessPoint myWap;

	double sqRange = Double.MAX_VALUE;
	double myRange = Double.MAX_VALUE;

	EntityPlayer myPlayer;
	public ItemStack effectiveItem;

	public double getRange()
	{
		return myRange;
	}

	public WirelessTerminalGuiObject(IWirelessTermHandler wh, ItemStack is, EntityPlayer ep, World w, int x, int y, int z) {
		encryptionKey = wh.getEncryptionKey( is );
		effectiveItem = is;
		myPlayer = ep;
		wth = wh;

		Object obj = null;

		try
		{
			long encKey = Long.parseLong( encryptionKey );
			obj = AEApi.instance().registries().locatable().findLocatableBySerial( encKey );
		}
		catch (NumberFormatException err)
		{
			// :P
		}

		if ( obj instanceof IGridHost )
		{
			IGridNode n = ((IGridHost) obj).getGridNode( ForgeDirection.UNKNOWN );
			if ( n != null )
			{
				targetGrid = n.getGrid();
				if ( targetGrid != null )
				{
					sg = targetGrid.getCache( IStorageGrid.class );
					if ( sg != null )
						itemStorage = sg.getItemInventory();
				}
			}
		}
	}

	public boolean rangeCheck()
	{
		sqRange = myRange = Double.MAX_VALUE;

		if ( targetGrid != null && itemStorage != null )
		{
			if ( myWap != null )
			{
				if ( myWap.getGrid() == targetGrid )
				{
					if ( testWap( myWap ) )
						return true;
				}
				return false;
			}

			IMachineSet tw = targetGrid.getMachines( TileWireless.class );

			myWap = null;

			for (IGridNode n : tw)
			{
				IWirelessAccessPoint wap = (IWirelessAccessPoint) n.getMachine();
				if ( testWap( wap ) )
					myWap = wap;
			}

			return myWap != null;
		}
		return false;
	}

	private boolean testWap(IWirelessAccessPoint wap)
	{
		double rangeLimit = wap.getRange();
		rangeLimit *= rangeLimit;

		DimensionalCoord dc = wap.getLocation();

		if ( dc.getWorld() == myPlayer.worldObj )
		{
			double offX = (double) dc.x - myPlayer.posX;
			double offY = (double) dc.y - myPlayer.posY;
			double offZ = (double) dc.z - myPlayer.posZ;

			double r = offX * offX + offY * offY + offZ * offZ;
			if ( r < rangeLimit && sqRange > r )
			{
				if ( wap.isActive() )
				{
					sqRange = r;
					myRange = Math.sqrt( r );
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		if ( sg == null )
			return null;
		return sg.getItemInventory();
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		if ( sg == null )
			return null;
		return sg.getFluidInventory();
	}

	@Override
	public void addListener(IMEMonitorHandlerReceiver<IAEItemStack> l, Object verificationToken)
	{
		if ( itemStorage != null )
			itemStorage.addListener( l, verificationToken );
	}

	@Override
	public void removeListener(IMEMonitorHandlerReceiver<IAEItemStack> l)
	{
		if ( itemStorage != null )
			itemStorage.removeListener( l );
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems(IItemList out)
	{
		if ( itemStorage != null )
			return itemStorage.getAvailableItems( out );
		return out;
	}

	@Override
	public IItemList<IAEItemStack> getStorageList()
	{
		if ( itemStorage != null )
			return itemStorage.getStorageList();
		return null;
	}

	@Override
	public AccessRestriction getAccess()
	{
		if ( itemStorage != null )
			return itemStorage.getAccess();
		return AccessRestriction.NO_ACCESS;
	}

	@Override
	public boolean isPrioritized(IAEItemStack input)
	{
		if ( itemStorage != null )
			return itemStorage.isPrioritized( input );
		return false;
	}

	@Override
	public boolean canAccept(IAEItemStack input)
	{
		if ( itemStorage != null )
			return itemStorage.canAccept( input );
		return false;
	}

	@Override
	public int getPriority()
	{
		if ( itemStorage != null )
			return itemStorage.getPriority();
		return 0;
	}

	@Override
	public int getSlot()
	{
		if ( itemStorage != null )
			return itemStorage.getSlot();
		return 0;
	}

	@Override
	public IAEItemStack injectItems(IAEItemStack input, Actionable type, BaseActionSource src)
	{
		if ( itemStorage != null )
			return itemStorage.injectItems( input, type, src );
		return input;
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src)
	{
		if ( itemStorage != null )
			return itemStorage.extractItems( request, mode, src );
		return null;
	}

	@Override
	public StorageChannel getChannel()
	{
		if ( itemStorage != null )
			return itemStorage.getChannel();
		return StorageChannel.ITEMS;
	}

	@Override
	public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier)
	{
		if ( wth != null && effectiveItem != null )
		{
			if ( mode == Actionable.SIMULATE )
				return wth.hasPower( myPlayer, amt, getItemStack() ) ? amt : 0;
			return wth.usePower( myPlayer, amt, getItemStack() ) ? amt : 0;
		}
		return 0.0;
	}

	@Override
	public ItemStack getItemStack()
	{
		return effectiveItem;
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return wth.getConfigManager( effectiveItem );
	}

	@Override
	public IGridNode getGridNode(ForgeDirection dir)
	{
		return this.getActionableNode();
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.NONE;
	}

	@Override
	public void securityBreak()
	{

	}

	@Override
	public IGridNode getActionableNode()
	{
		rangeCheck();
		if ( myWap != null )
			return myWap.getActionableNode();
		return null;
	}

	@Override
	public boolean validForPass(int i)
	{
		return itemStorage.validForPass( i );
	}

}
