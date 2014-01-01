package appeng.parts.automation;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.core.sync.GuiBridge;
import appeng.me.GridAccessException;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;

public class PartExportBus extends PartSharedItemBus implements IGridTickable
{

	public PartExportBus(ItemStack is) {
		super( PartExportBus.class, is );
		settings.registerSetting( Settings.REDSTONE_OUTPUT, RedstoneMode.IGNORE );
		settings.registerSetting( Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
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

	@Override
	protected boolean isSleeping()
	{
		// TODO Auto-generated method stub
		return getHandler() == null;
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		int itemToSend = 1;
		boolean didSomething = false;

		try
		{
			InventoryAdaptor d = getHandler();
			IMEInventory<IAEItemStack> inv = proxy.getStorage().getItemInventory();
			IEnergyGrid energy = proxy.getEnergy();

			if ( d != null )
			{
				for (int x = 0; x < availableSlots(); x++)
				{
					IAEItemStack ais = config.getAEStackInSlot( x );
					if ( ais != null && itemToSend > 0 )
					{
						ItemStack is = ais.getItemStack();
						is.stackSize = itemToSend;

						ItemStack o = d.simulateAdd( ais.getItemStack() );
						int canFit = o == null ? itemToSend : itemToSend - o.stackSize;

						if ( canFit > 0 )
						{
							ais = ais.copy();
							ais.setStackSize( canFit );
							IAEItemStack itemsToAdd = Platform.poweredExtraction( energy, inv, ais );

							if ( itemsToAdd != null )
							{
								itemToSend -= itemsToAdd.getStackSize();

								ItemStack failed = d.addItems( itemsToAdd.getItemStack() );
								if ( failed != null )
								{
									ais.setStackSize( failed.stackSize );
									inv.injectItems( ais, Actionable.MODULATE );
								}
								else
									didSomething = true;
							}

						}
					}
				}
			}

		}
		catch (GridAccessException e)
		{
			// :P
		}

		return didSomething ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
	}

}
