package appeng.helpers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReciever;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

public class WirelessTerminalGuiObject implements IPortableCell
{

	IWirelessTermHandler wth;
	String encryptionKey;

	IGrid targetGrid;
	IStorageGrid sg;
	IMEMonitor<IAEItemStack> itemStorage;

	EntityPlayer myPlayer;
	public ItemStack effectiveItem;

	public WirelessTerminalGuiObject(IWirelessTermHandler wh, ItemStack is, EntityPlayer ep, World w, int x, int y, int z) {
		encryptionKey = wh.getEncryptionKey( is );
		effectiveItem = is;
		myPlayer = ep;
		wth = wh;
	}

	boolean rangeCheck()
	{
		return false;
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		return sg.getItemInventory();
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		return sg.getFluidInventory();
	}

	@Override
	public void addListener(IMEMonitorHandlerReciever<IAEItemStack> l, Object verificationToken)
	{
		itemStorage.addListener( l, verificationToken );
	}

	@Override
	public void removeListener(IMEMonitorHandlerReciever<IAEItemStack> l)
	{
		itemStorage.removeListener( l );
	}

	@SuppressWarnings("deprecation")
	@Override
	public IItemList<IAEItemStack> getAvailableItems(IItemList out)
	{
		return itemStorage.getAvailableItems( out );
	}

	@Override
	public IItemList<IAEItemStack> getStorageList()
	{
		return itemStorage.getStorageList();
	}

	@Override
	public AccessRestriction getAccess()
	{
		return itemStorage.getAccess();
	}

	@Override
	public boolean isPrioritized(IAEItemStack input)
	{
		return itemStorage.isPrioritized( input );
	}

	@Override
	public boolean canAccept(IAEItemStack input)
	{
		return itemStorage.canAccept( input );
	}

	@Override
	public int getPriority()
	{
		return itemStorage.getPriority();
	}

	@Override
	public int getSlot()
	{
		return itemStorage.getSlot();
	}

	@Override
	public IAEItemStack injectItems(IAEItemStack input, Actionable type, BaseActionSource src)
	{
		return itemStorage.injectItems( input, type, src );
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src)
	{
		return itemStorage.extractItems( request, mode, src );
	}

	@Override
	public StorageChannel getChannel()
	{
		return itemStorage.getChannel();
	}

	@Override
	public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier)
	{
		if ( mode == Actionable.SIMULATE )
			return wth.hasPower( myPlayer, amt, getItemStack() ) ? amt : 0;
		return wth.usePower( myPlayer, amt, getItemStack() ) ? amt : 0;
	}

	@Override
	public ItemStack getItemStack()
	{
		return effectiveItem;
	}

}
