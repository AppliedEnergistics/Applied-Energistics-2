package appeng.parts.automation;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
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
import appeng.me.GridAccessException;
import appeng.parts.PartBasicState;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.IInventoryDestination;

public class PartImportBus extends PartBasicState implements IGridTickable, IInventoryDestination
{

	public PartImportBus(ItemStack is) {
		super( PartImportBus.class, is );
	}

	IMEInventory<IAEItemStack> destination = null;

	@Override
	public boolean canInsert(ItemStack stack)
	{
		IAEItemStack out = destination.injectItems( AEApi.instance().storage().createItemStack( stack ), Actionable.SIMULATE );
		if ( out == null )
			return true;
		return out.getStackSize() != stack.stackSize;
	}

	private IInventoryDestination configDest(IMEMonitor<IAEItemStack> itemInventory)
	{
		destination = itemInventory;
		return this;
	}

	boolean cached = false;
	int adaptorHash = 0;
	InventoryAdaptor adaptor;

	InventoryAdaptor getHandler()
	{
		if ( cached )
			return adaptor;

		cached = true;
		TileEntity self = getHost().getTile();
		TileEntity target = self.worldObj.getBlockTileEntity( self.xCoord + side.offsetX, self.yCoord + side.offsetY, self.zCoord + side.offsetZ );

		int newAdaptorHash = Platform.generateTileHash( target );

		if ( adaptorHash == newAdaptorHash )
			return adaptor;

		adaptorHash = newAdaptorHash;
		adaptor = InventoryAdaptor.getAdaptor( target, side.getOpposite() );
		cached = true;

		return adaptor;

	}

	@Override
	public void onNeighborChanged()
	{
		cached = false;
		if ( adaptor == null && getHandler() != null )
		{
			try
			{
				proxy.getTick().wakeDevice( proxy.getNode() );
			}
			catch (GridAccessException e)
			{
				// :P
			}
		}
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
					IAEItemStack failed = destination.injectItems( AEApi.instance().storage().createItemStack( newItems ), Actionable.MODULATE );
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

}
