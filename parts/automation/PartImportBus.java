package appeng.parts.automation;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.core.settings.TickRates;
import appeng.core.sync.GuiBridge;
import appeng.me.GridAccessException;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.IInventoryDestination;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartImportBus extends PartSharedItemBus implements IGridTickable, IInventoryDestination
{

	BaseActionSource mySrc;
	IMEInventory<IAEItemStack> destination = null;
	IAEItemStack lastItemChecked = null;

	public PartImportBus(ItemStack is) {
		super( PartImportBus.class, is );
		settings.registerSetting( Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE );
		settings.registerSetting( Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		mySrc = new MachineSource( this );
	}

	@Override
	public boolean onPartActivate(EntityPlayer player, Vec3 pos)
	{
		if ( !player.isSneaking() )
		{
			if ( Platform.isClient() )
				return true;

			Platform.openGUI( player, getHost().getTile(), side, GuiBridge.GUI_BUS );
			return true;
		}

		return false;
	}

	@Override
	public boolean canInsert(ItemStack stack)
	{
		IAEItemStack out = destination.injectItems( lastItemChecked = AEApi.instance().storage().createItemStack( stack ), Actionable.SIMULATE, mySrc );
		if ( out == null )
			return true;
		return out.getStackSize() != stack.stackSize;
	}

	private IInventoryDestination configDest(IMEMonitor<IAEItemStack> itemInventory)
	{
		destination = itemInventory;
		return this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture( CableBusTextures.PartImportSides.getIcon(), CableBusTextures.PartImportSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartImportSides.getIcon(), CableBusTextures.PartImportSides.getIcon() );

		rh.setBounds( 3, 3, 15, 13, 13, 16 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 4, 4, 14, 12, 12, 15 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 5, 5, 13, 11, 11, 14 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		renderCache = rh.useSimpliedRendering( x, y, z, this, renderCache );
		rh.setTexture( CableBusTextures.PartImportSides.getIcon(), CableBusTextures.PartImportSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartImportSides.getIcon(), CableBusTextures.PartImportSides.getIcon() );

		rh.setBounds( 4, 4, 14, 12, 12, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 5, 5, 13, 11, 11, 14 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 6, 6, 12, 10, 10, 13 );
		rh.renderBlock( x, y, z, renderer );
		rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
				CableBusTextures.PartMonitorBack.getIcon(), is.getIconIndex(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
				CableBusTextures.PartMonitorSidesStatus.getIcon() );

		rh.setBounds( 6, 6, 11, 10, 10, 12 );
		rh.renderBlock( x, y, z, renderer );

		renderLights( x, y, z, rh, renderer );
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox( 6, 6, 11, 10, 10, 13 );
		bch.addBox( 5, 5, 13, 11, 11, 14 );
		bch.addBox( 4, 4, 14, 12, 12, 16 );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 5;
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( TickRates.ImportBus.min, TickRates.ImportBus.max, getHandler() == null, false );
	}

	private int itemToSend; // used in tickingRequest
	private boolean worked; // used in tickingRequest

	TickRateModulation doBusWork()
	{
		if ( !proxy.isActive() )
			return TickRateModulation.IDLE;

		worked = false;

		InventoryAdaptor myAdaptor = getHandler();
		FuzzyMode fzMode = (FuzzyMode) getConfigManager().getSetting( Settings.FUZZY_MODE );

		if ( myAdaptor != null )
		{
			try
			{
				switch (getInstalledUpgrades( Upgrades.SPEED ))
				{
				default:
				case 0:
					itemToSend = 1;
					break;
				case 1:
					itemToSend = 8;
					break;
				case 2:
					itemToSend = 32;
					break;
				case 3:
					itemToSend = 64;
					break;
				case 4:
					itemToSend = 96;
					break;
				}

				itemToSend = Math.min( itemToSend, (int) (0.01 + proxy.getEnergy().extractAEPower( itemToSend, Actionable.SIMULATE, PowerMultiplier.CONFIG )) );
				IMEMonitor<IAEItemStack> inv = proxy.getStorage().getItemInventory();
				IEnergyGrid energy = proxy.getEnergy();

				boolean Configured = false;
				for (int x = 0; x < availableSlots(); x++)
				{
					IAEItemStack ais = config.getAEStackInSlot( x );
					if ( ais != null && itemToSend > 0 )
					{
						Configured = true;
						while (itemToSend > 0)
						{
							if ( importStuff( myAdaptor, ais, inv, energy, fzMode ) )
								break;
						}
					}
				}

				if ( !Configured )
				{
					while (itemToSend > 0)
					{
						if ( importStuff( myAdaptor, null, inv, energy, fzMode ) )
							break;
					}
				}
			}
			catch (GridAccessException e)
			{
				// :3
			}
		}
		else
			return TickRateModulation.SLEEP;

		return worked ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		return doBusWork();
	}

	private boolean importStuff(InventoryAdaptor myAdaptor, IAEItemStack whatToImport, IMEMonitor<IAEItemStack> inv, IEnergySource energy, FuzzyMode fzMode)
	{
		int toSend = this.itemToSend;

		if ( toSend > 64 )
			toSend = 64;

		ItemStack newItems;
		if ( getInstalledUpgrades( Upgrades.FUZZY ) > 0 )
			newItems = myAdaptor.removeSimilarItems( toSend, whatToImport == null ? null : whatToImport.getItemStack(), fzMode, configDest( inv ) );
		else
			newItems = myAdaptor.removeItems( toSend, whatToImport == null ? null : whatToImport.getItemStack(), configDest( inv ) );

		if ( newItems != null )
		{
			newItems.stackSize = (int) (Math.min( newItems.stackSize, energy.extractAEPower( newItems.stackSize, Actionable.SIMULATE, PowerMultiplier.CONFIG ) ) + 0.01);
			itemToSend -= newItems.stackSize;

			if ( lastItemChecked == null || !lastItemChecked.isSameType( newItems ) )
				lastItemChecked = AEApi.instance().storage().createItemStack( newItems );
			else
				lastItemChecked.setStackSize( newItems.stackSize );

			IAEItemStack failed = Platform.poweredInsert( energy, destination, lastItemChecked, mySrc );
			// destination.injectItems( lastItemChecked, Actionable.MODULATE );
			if ( failed != null )
			{
				myAdaptor.addItems( failed.getItemStack() );
				return true;
			}
			else
				worked = true;
		}
		else
			return true;

		return false;
	}

	public RedstoneMode getRSMode()
	{
		return (RedstoneMode) settings.getSetting( Settings.REDSTONE_CONTROLLED );
	}

	@Override
	protected boolean isSleeping()
	{
		return getHandler() == null || super.isSleeping();
	}

}
