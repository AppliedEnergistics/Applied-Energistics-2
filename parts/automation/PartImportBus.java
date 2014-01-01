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
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
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
import appeng.util.inv.IInventoryDestination;

public class PartImportBus extends PartSharedItemBus implements IGridTickable, IInventoryDestination
{

	public PartImportBus(ItemStack is) {
		super( PartImportBus.class, is );
		settings.registerSetting( Settings.REDSTONE_INPUT, RedstoneMode.IGNORE );
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

	IMEInventory<IAEItemStack> destination = null;
	IAEItemStack lastItemChecked = null;

	@Override
	public boolean canInsert(ItemStack stack)
	{
		IAEItemStack out = destination.injectItems( lastItemChecked = AEApi.instance().storage().createItemStack( stack ), Actionable.SIMULATE );
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
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 3, 3, 15, 13, 13, 16 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 4, 4, 14, 12, 12, 15 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 5, 5, 13, 11, 11, 14 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.useSimpliedRendering( x, y, z, this );
		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

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
		bch.addBox( 6, 6, 11, 10, 10, 12 );
		bch.addBox( 6, 6, 12, 10, 10, 13 );
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
		return new TickingRequest( 5, 40, getHandler() == null, false );
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		boolean worked = false;

		InventoryAdaptor myAdaptor = getHandler();
		if ( myAdaptor != null )
		{
			try
			{
				int howMany = 1;
				howMany = Math.min( howMany, (int) (0.01 + proxy.getEnergy().extractAEPower( howMany, Actionable.SIMULATE, PowerMultiplier.CONFIG )) );

				ItemStack newItems = myAdaptor.removeItems( howMany, null, configDest( proxy.getStorage().getItemInventory() ) );
				if ( newItems != null )
				{
					if ( lastItemChecked == null || !lastItemChecked.isSameType( newItems ) )
						lastItemChecked = AEApi.instance().storage().createItemStack( newItems );
					else
						lastItemChecked.setStackSize( newItems.stackSize );

					IAEItemStack failed = destination.injectItems( lastItemChecked, Actionable.MODULATE );
					if ( failed != null )
						myAdaptor.addItems( failed.getItemStack() );
					else
						worked = true;
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
	protected boolean isSleeping()
	{
		return getHandler() == null;
	}

}
