package appeng.parts.automation;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.core.sync.GuiBridge;
import appeng.me.GridAccessException;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;

public class PartExportBus extends PartSharedItemBus implements IGridTickable
{

	BaseActionSource mySrc;

	public PartExportBus(ItemStack is) {
		super( PartExportBus.class, is );
		settings.registerSetting( Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE );
		settings.registerSetting( Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		mySrc = new MachineSource( this );
	}

	@Override
	public boolean onActivate(EntityPlayer player, Vec3 pos)
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
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{

		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 4, 4, 12, 12, 12, 14 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 6, 6, 15, 10, 10, 16 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.useSimpliedRendering( x, y, z, this );
		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 4, 4, 12, 12, 12, 14 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 6, 6, 15, 10, 10, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
				CableBusTextures.PartMonitorBack.getIcon(), is.getIconIndex(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
				CableBusTextures.PartMonitorSidesStatus.getIcon() );

		rh.setBounds( 6, 6, 11, 10, 10, 12 );
		rh.renderBlock( x, y, z, renderer );

		renderLights( x, y, z, rh, renderer );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 5;
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox( 4, 4, 12, 12, 12, 14 );
		bch.addBox( 5, 5, 14, 11, 11, 15 );
		bch.addBox( 6, 6, 15, 10, 10, 16 );
		bch.addBox( 6, 6, 11, 10, 10, 12 );
	}

	long itemToSend = 1;
	boolean didSomething = false;

	@Override
	TickRateModulation doBusWork()
	{
		itemToSend = 1;
		didSomething = false;

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

		try
		{
			InventoryAdaptor d = getHandler();
			IMEMonitor<IAEItemStack> inv = proxy.getStorage().getItemInventory();
			IEnergyGrid energy = proxy.getEnergy();
			FuzzyMode fzMode = (FuzzyMode) getConfigManager().getSetting( Settings.FUZZY_MODE );

			if ( d != null )
			{
				for (int x = 0; x < availableSlots() && itemToSend > 0; x++)
				{
					IAEItemStack ais = config.getAEStackInSlot( x );
					if ( ais == null || itemToSend <= 0 )
						continue;

					if ( getInstalledUpgrades( Upgrades.FUZZY ) > 0 )
					{
						for (IAEItemStack o : inv.getStorageList().findFuzzy( ais, fzMode ))
						{
							pushItemIntoTarget( d, energy, fzMode, inv, o );
							if ( itemToSend <= 0 )
								break;
						}
					}
					else
						pushItemIntoTarget( d, energy, fzMode, inv, ais );
				}
			}

		}
		catch (GridAccessException e)
		{
			// :P
		}

		return didSomething ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
	}

	private void pushItemIntoTarget(InventoryAdaptor d, IEnergyGrid energy, FuzzyMode fzMode, IMEInventory<IAEItemStack> inv, IAEItemStack ais)
	{
		ItemStack is = ais.getItemStack();
		is.stackSize = (int) itemToSend;

		ItemStack o = d.simulateAdd( is );
		long canFit = o == null ? itemToSend : itemToSend - o.stackSize;

		if ( canFit > 0 )
		{
			ais = ais.copy();
			ais.setStackSize( canFit );
			IAEItemStack itemsToAdd = Platform.poweredExtraction( energy, inv, ais, mySrc );

			if ( itemsToAdd != null )
			{
				itemToSend -= itemsToAdd.getStackSize();

				ItemStack failed = d.addItems( itemsToAdd.getItemStack() );
				if ( failed != null )
				{
					ais.setStackSize( failed.stackSize );
					inv.injectItems( ais, Actionable.MODULATE, mySrc );
				}
				else
					didSomething = true;
			}

		}
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		return doBusWork();
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
