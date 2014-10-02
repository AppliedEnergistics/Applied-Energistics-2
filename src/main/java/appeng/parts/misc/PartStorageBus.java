package appeng.parts.misc;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IConfigManager;
import appeng.client.texture.CableBusTextures;
import appeng.core.settings.TickRates;
import appeng.core.stats.Achievements;
import appeng.core.sync.GuiBridge;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.MEMonitorIInventory;
import appeng.parts.automation.PartUpgradeable;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.InvOperation;
import appeng.transformer.annotations.integration.Interface;
import appeng.transformer.annotations.integration.Method;
import appeng.util.Platform;
import appeng.util.prioitylist.FuzzyPriorityList;
import appeng.util.prioitylist.PrecisePriorityList;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile.PipeType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Interface(iname = "BC", iface = "buildcraft.api.transport.IPipeConnection")
public class PartStorageBus extends PartUpgradeable implements IGridTickable, ICellContainer, IMEMonitorHandlerReceiver<IAEItemStack>, IPipeConnection,
		IPriorityHost
{

	int priority = 0;
	final BaseActionSource mySrc;

	final AppEngInternalAEInventory Config = new AppEngInternalAEInventory( this, 63 );

	public PartStorageBus(ItemStack is)
	{
		super( PartStorageBus.class, is );
		getConfigManager().registerSetting( Settings.ACCESS, AccessRestriction.READ_WRITE );
		getConfigManager().registerSetting( Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		getConfigManager().registerSetting( Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY );
		mySrc = new MachineSource( this );
	}

	boolean cached = false;
	MEMonitorIInventory monitor = null;
	MEInventoryHandler handler = null;

	int handlerHash = 0;
	boolean wasActive = false;

	@Override
	@MENetworkEventSubscribe
	public void powerRender(MENetworkPowerStatusChange c)
	{
		updateStatus();
	}

	@MENetworkEventSubscribe
	public void updateChannels(MENetworkChannelsChanged changedChannels)
	{
		updateStatus();
	}

	private void updateStatus()
	{
		boolean currentActive = proxy.isActive();
		if ( wasActive != currentActive )
		{
			wasActive = currentActive;
			try
			{
				proxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );
				host.markForUpdate();
			}
			catch (GridAccessException e)
			{
				// :P
			}
		}
	}

	@Override
	public boolean onPartActivate(EntityPlayer player, Vec3 pos)
	{
		if ( !player.isSneaking() )
		{
			if ( Platform.isClient() )
				return true;

			Platform.openGUI( player, getHost().getTile(), side, GuiBridge.GUI_STORAGEBUS );
			return true;
		}

		return false;
	}

	@Override
	protected int getUpgradeSlots()
	{
		return 5;
	}

	@Override
	public boolean isValid(Object verificationToken)
	{
		return handler == verificationToken;
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		if ( name.equals( "config" ) )
			return Config;

		return super.getInventoryByName( name );
	}

	private byte resetCacheLogic = 0;

	private void resetCache(boolean fullReset)
	{
		if ( host == null || host.getTile() == null || host.getTile().getWorldObj() == null || host.getTile().getWorldObj().isRemote )
			return;

		if ( fullReset )
			resetCacheLogic = 2;
		else
			resetCacheLogic = 1;

		try
		{
			proxy.getTick().alertDevice( proxy.getNode() );
		}
		catch (GridAccessException e)
		{
			// :P
		}
	}

	private void resetCache()
	{
		boolean fullReset = resetCacheLogic == 2;
		resetCacheLogic = 0;

		IMEInventory<IAEItemStack> in = getInternalHandler();
		IItemList<IAEItemStack> before = AEApi.instance().storage().createItemList();
		if ( in != null )
			before = in.getAvailableItems( before );

		cached = false;
		if ( fullReset )
			handlerHash = 0;

		IMEInventory<IAEItemStack> out = getInternalHandler();

		if ( monitor != null )
			monitor.onTick();

		IItemList<IAEItemStack> after = AEApi.instance().storage().createItemList();
		if ( out != null )
			after = out.getAvailableItems( after );

		Platform.postListChanges( before, after, this, mySrc );
	}

	@Override
	public void onNeighborChanged()
	{
		resetCache( false );
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack)
	{
		super.onChangeInventory( inv, slot, mc, removedStack, newStack );

		if ( inv == Config )
			resetCache( true );
	}

	@Override
	public void upgradesChanged()
	{
		super.upgradesChanged();
		resetCache( true );
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
	{
		resetCache( true );
		host.markForSave();
	}

	@Override
	public void setPriority(int newValue)
	{
		priority = newValue;
		host.markForSave();
		resetCache( true );
	}

	public MEInventoryHandler getInternalHandler()
	{
		if ( cached )
			return handler;

		boolean wasSleeping = monitor == null;

		cached = true;
		TileEntity self = getHost().getTile();
		TileEntity target = self.getWorldObj().getTileEntity( self.xCoord + side.offsetX, self.yCoord + side.offsetY, self.zCoord + side.offsetZ );

		int newHandlerHash = Platform.generateTileHash( target );

		if ( handlerHash == newHandlerHash && handlerHash != 0 )
			return handler;

		try
		{
			// force grid to update handlers...
			proxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );
		}
		catch (GridAccessException e)
		{
			// :3
		}

		handlerHash = newHandlerHash;
		handler = null;
		monitor = null;
		if ( target != null )
		{
			IExternalStorageHandler esh = AEApi.instance().registries().externalStorage().getHandler( target, side.getOpposite(), StorageChannel.ITEMS, mySrc );
			if ( esh != null )
			{
				IMEInventory inv = esh.getInventory( target, side.getOpposite(), StorageChannel.ITEMS, mySrc );

				if ( inv instanceof MEMonitorIInventory )
				{
					MEMonitorIInventory h = (MEMonitorIInventory) inv;
					h.mode = (StorageFilter) getConfigManager().getSetting( Settings.STORAGE_FILTER );
					h.mySource = new MachineSource( this );
				}

				if ( inv instanceof MEMonitorIInventory )
					monitor = (MEMonitorIInventory) inv;

				if ( inv != null )
				{
					checkInterfaceVsStorageBus( target, side.getOpposite() );

					handler = new MEInventoryHandler( inv, StorageChannel.ITEMS );

					handler.myAccess = (AccessRestriction) this.getConfigManager().getSetting( Settings.ACCESS );
					handler.myWhitelist = getInstalledUpgrades( Upgrades.INVERTER ) > 0 ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST;
					handler.myPriority = priority;

					IItemList<IAEItemStack> priorityList = AEApi.instance().storage().createItemList();

					int slotsToUse = 18 + getInstalledUpgrades( Upgrades.CAPACITY ) * 9;
					for (int x = 0; x < Config.getSizeInventory() && x < slotsToUse; x++)
					{
						IAEItemStack is = Config.getAEStackInSlot( x );
						if ( is != null )
							priorityList.add( is );
					}

					if ( getInstalledUpgrades( Upgrades.FUZZY ) > 0 )
						handler.myPartitionList = new FuzzyPriorityList( priorityList, (FuzzyMode) this.getConfigManager().getSetting( Settings.FUZZY_MODE ) );
					else
						handler.myPartitionList = new PrecisePriorityList( priorityList );

					if ( inv instanceof IMEMonitor )
						((IMEMonitor) inv).addListener( this, handler );
				}
			}
		}

		// update sleep state...
		if ( wasSleeping != (monitor == null) )
		{
			try
			{
				ITickManager tm = proxy.getTick();
				if ( monitor == null )
					tm.sleepDevice( proxy.getNode() );
				else
					tm.wakeDevice( proxy.getNode() );
			}
			catch (GridAccessException e)
			{
				// :(
			}
		}

		return handler;
	}

	private void checkInterfaceVsStorageBus(TileEntity target, ForgeDirection side)
	{
		IInterfaceHost achievement = null;

		if ( target instanceof IInterfaceHost )
			achievement = (IInterfaceHost) target;

		if ( target instanceof IPartHost )
		{
			Object part = ((IPartHost) target).getPart( side );
			if ( part instanceof IInterfaceHost )
				achievement = (IInterfaceHost) part;
		}

		if ( achievement != null )
		{
			Platform.addStat( achievement.getActionableNode().getPlayerID(), Achievements.Recursive.getAchievement() );
			Platform.addStat( getActionableNode().getPlayerID(), Achievements.Recursive.getAchievement() );
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture( CableBusTextures.PartStorageSides.getIcon(), CableBusTextures.PartStorageSides.getIcon(), CableBusTextures.PartStorageBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartStorageSides.getIcon(), CableBusTextures.PartStorageSides.getIcon() );

		rh.setBounds( 3, 3, 15, 13, 13, 16 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 2, 2, 14, 14, 14, 15 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 5, 5, 12, 11, 11, 14 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		renderCache = rh.useSimplifiedRendering( x, y, z, this, renderCache );
		rh.setTexture( CableBusTextures.PartStorageSides.getIcon(), CableBusTextures.PartStorageSides.getIcon(), CableBusTextures.PartStorageBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartStorageSides.getIcon(), CableBusTextures.PartStorageSides.getIcon() );

		rh.setBounds( 3, 3, 15, 13, 13, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 2, 2, 14, 14, 14, 15 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.PartStorageSides.getIcon(), CableBusTextures.PartStorageSides.getIcon(), CableBusTextures.PartStorageBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartStorageSides.getIcon(), CableBusTextures.PartStorageSides.getIcon() );

		rh.setBounds( 5, 5, 12, 11, 11, 13 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
				CableBusTextures.PartMonitorBack.getIcon(), is.getIconIndex(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
				CableBusTextures.PartMonitorSidesStatus.getIcon() );

		rh.setBounds( 5, 5, 13, 11, 11, 14 );
		rh.renderBlock( x, y, z, renderer );

		renderLights( x, y, z, rh, renderer );
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch)
	{
		bch.addBox( 3, 3, 15, 13, 13, 16 );
		bch.addBox( 2, 2, 14, 14, 14, 15 );
		bch.addBox( 5, 5, 12, 11, 11, 14 );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 4;
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( TickRates.StorageBus.min, TickRates.StorageBus.max, monitor == null, true );
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		if ( resetCacheLogic != 0 )
			resetCache();

		if ( monitor != null )
			return monitor.onTick();

		return TickRateModulation.SLEEP;
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT( data );
		Config.writeToNBT( data, "config" );
		data.setInteger( "priority", priority );
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT( data );
		Config.readFromNBT( data, "config" );
		priority = data.getInteger( "priority" );
	}

	@Override
	public List<IMEInventoryHandler> getCellArray(StorageChannel channel)
	{
		if ( channel == StorageChannel.ITEMS )
		{
			IMEInventoryHandler out = proxy.isActive() ? getInternalHandler() : null;
			if ( out != null )
				return Arrays.asList( out );
		}
		return Arrays.asList( new IMEInventoryHandler[] {} );
	}

	@Override
	public int getPriority()
	{
		return priority;
	}

	@Override
	public void blinkCell(int slot)
	{
	}

	@Override
	public void postChange(IBaseMonitor<IAEItemStack> monitor, Iterable<IAEItemStack> change, BaseActionSource source)
	{
		try
		{
			if ( proxy.isActive() )
				proxy.getStorage().postAlterationOfStoredItems( StorageChannel.ITEMS, change, mySrc );
		}
		catch (GridAccessException e)
		{
			// :(
		}
	}

	@Override
	@Method(iname = "BC")
	public ConnectOverride overridePipeConnection(PipeType type, ForgeDirection with)
	{
		return type == PipeType.ITEM && with == side ? ConnectOverride.CONNECT : ConnectOverride.DISCONNECT;
	}

	@Override
	public void onListUpdate()
	{
		// not used here.
	}

	@Override
	public void saveChanges(IMEInventory cellInventory)
	{
		// nope!
	}

}
